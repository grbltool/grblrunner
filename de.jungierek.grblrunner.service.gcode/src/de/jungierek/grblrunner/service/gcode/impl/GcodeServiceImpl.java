package de.jungierek.grblrunner.service.gcode.impl;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.service.serial.ISerialServiceReceiver;

public class GcodeServiceImpl implements IGcodeService, ISerialServiceReceiver {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeServiceImpl.class );

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private ISerialService serial;

    // protected for test purposes
    protected ArrayBlockingQueue<GrblRequestImpl> queue = new ArrayBlockingQueue<GrblRequestImpl> ( IConstant.GCODE_QUEUE_LENGTH, false );

    protected volatile boolean waitForOk = false;
    protected volatile boolean skipByAlarm = false;
    protected volatile boolean suppressInTerminal = false;
    protected volatile boolean suppressSingleByteCommandInTerminal = false;

    GcodeGrblStateImpl lastState;
    private EGcodeMode lastMotionMode;
    private String lastCoordSelect;
    private GcodePointImpl lastCoordSelectOffset;
    private GcodePointImpl lastCoordSelectTempOffset;
    private String lastPlane;
    private String lastMetricMode;
    private Object lastDistanceMode;
    private Object lastTool;
    private String lastSpindleMode;
    private String lastCoolantMode;
    private String lastFeedrate;
    private String lastSpindlespeed;

    public static final int GRBL_STATE_POLLER_SLEEP_MS = 200;
    public static final int PARSER_STATE_POLLER_SLEEP_MS = 1003;

    private Thread senderThread;
    private Thread statePollerThread;
    private Thread parserStatePoller;

    protected volatile boolean playRunning = false;
    protected volatile boolean scanRunning = false;

    private IGcodePoint fixtureSshift = new GcodePointImpl ( 0.0, 0.0, 0.0 );

    private IGcodeProgram gcodeProgram;

    @Inject
    public GcodeServiceImpl () {}

    // only for test
    protected GcodeServiceImpl ( IEventBroker eventBroker, ISerialService serial ) {

        this.eventBroker = eventBroker;
        this.serial = serial;

    }

    @Override
    public IGcodePoint createGcodePoint ( double x, double y, double z ) {

        return new GcodePointImpl ( x, y, z );

    }

    @Override
    public void sendFeedHold () {
        new Thread ( ( ) -> sendSingleSignCommand ( '!' ) ).start ();
    }

    @Override
    public void sendReset () {
        new Thread ( ( ) -> sendSingleSignCommand ( IConstant.GRBL_RESET_CODE ) ).start ();
    }

    @Override
    public void sendStartCycle () {
        new Thread ( ( ) -> sendSingleSignCommand ( '~' ) ).start ();
    }

    @Override
    public void sendStatePoll () {
        new Thread ( ( ) -> sendSingleSignCommand ( '?' ) ).start ();
    }

    private void sendSingleSignCommand ( char c ) {

        LOG.trace ( "sendSingleSignCommand: c=" + c + " suppressInTerminal=" + suppressInTerminal );

        // send Command direct, bypassing queue
        // only for '?', '!', '~'
        this.suppressSingleByteCommandInTerminal = true;
        serial.send ( c );

    }

    @Override
    public void sendCommand ( String line ) {
        
        sendCommand ( line, false );
    }

    @Override
    public void sendCommandSuppressInTerminal ( String line ) {

        sendCommand ( line, true );

    }

    private void sendCommand ( String line, boolean suppressInTerminal ) {

        try {

            queue.put ( new GrblRequestImpl ( suppressInTerminal, line + IConstant.LF ) );

            if ( line.startsWith ( "G92" ) || line.startsWith ( "G10" ) ) { // detect G92 or G10 to update shift
                queue.put ( new GrblRequestImpl ( true, "$#" + IConstant.LF ) );
            }

        }
        catch ( InterruptedException exc ) {
            LOG.error ( "sendCommand: exception in line=" + line );
            sendErrorMessage ( "sending gcode command interrupted!\n" + line, exc );
        }

    }

    @Override
    public void received ( String line ) {

        boolean releaseWaitForOk = false;
        boolean suppressLine = suppressInTerminal;

        if ( line.startsWith ( "<" ) ) { // response on state request '?'
            releaseWaitForOk = false;
            skipByAlarm = false;
            if ( suppressSingleByteCommandInTerminal ) {
                suppressLine = true;
                suppressSingleByteCommandInTerminal = false;
            }
        }
        else if ( line.startsWith ( "ok" ) || line.startsWith ( "error" ) ) {
            releaseWaitForOk = true;
            skipByAlarm = false;
        }
        else if ( line.startsWith ( "Grbl" ) ) {
            suppressLine = false;
        }
        else if ( line.startsWith ( "ALARM" ) ) {
            // queue.clear (); // empty queue
            LOG.info ( "received: ALARM" );
            releaseWaitForOk = true;
            skipByAlarm = true;
            suppressInTerminal = false; // show this line ever
        }

        // erst event senden, dann ...
        // System.out.println ( logName () + "received: posting event waitForOk=" + waitForOk + " skiByAlarm=" + skipByAlarm + " suppressInTerminl=" + suppressInTerminal + " line="
        // + line );
        analyseResponse ( line );
        eventBroker.send ( IEvent.GRBL_RECEIVED, new GrblResponseImpl ( suppressLine, line ) );

        // ... n�chstes Kommando frei geben
        if ( releaseWaitForOk ) waitForOk = false;

    }

    private GcodePointImpl parseCoordinates ( String line, String intro, char closingChar ) {

        return new GcodePointImpl ( parseVector ( line, IConstant.AXIS.length, intro, closingChar ) );

    }

    private double [] parseVector ( String line, int vectorLength, String intro, char closingChar ) {

        double [] coord = new double [vectorLength];

        int startPos = line.indexOf ( intro ) + intro.length ();
        int endPos = -1;

        for ( int i = 0; i < vectorLength; i++ ) {
            endPos = line.indexOf ( (i < vectorLength - 1 ? "," : "" + closingChar), startPos );
            coord[i] = parseDouble ( 99999.999, line.substring ( startPos, endPos ) );
            startPos = endPos + 1;
        }

        return coord;

    }

    private double parseDouble ( double defaultValue, String s ) {

        double result = defaultValue;
        try {
            result = Double.parseDouble ( s );
        }
        catch ( NumberFormatException exc ) {}

        return result;

    }

    private boolean ignoreNextProbe = false;
    private final static String PROBE_PATTERN = "[PRB:";
    private final static int PROBE_PATTERN_LEN = PROBE_PATTERN.length ();

    private void analyseResponse ( String line ) {

        if ( line.startsWith ( "ALARM" ) ) {

            eventBroker.send ( IEvent.GRBL_ALARM, line ); // inform about alarm message

        }
        else if ( line.startsWith ( "Grbl" ) ) {

            eventBroker.send ( IEvent.GRBL_RESTARTED, line ); // inform about grbl restart

        }
        else if ( line.startsWith ( "<" ) ) { // we found state line

            EGrblState state = EGrblState.identify ( line );

            GcodePointImpl m = parseCoordinates ( line, "MPos:", ',' );
            GcodePointImpl w = parseCoordinates ( line, "WPos:", '>' );
            GcodeGrblStateImpl gcodeState = new GcodeGrblStateImpl ( state, m, w );

            // update only on change
            if ( lastState == null || !lastState.equals ( gcodeState ) ) {
                lastState = gcodeState;
                eventBroker.send ( IEvent.UPDATE_STATE, gcodeState );
            }

        }
        else if ( line.startsWith ( "[PRB:" ) ) {
            // System.out.println ( logName () + "receivedNotified: send update probe line=" + line );
            // probe sends all cooridnates in machine system
            GcodePointImpl probePoint = null;
            final int probePatternLength = "[PRB:".length ();
            if ( line.substring ( probePatternLength ).indexOf ( ':' ) > 0 ) { // test after "[PRB:"
                // we are in v0.9j
                probePoint = parseCoordinates ( line, "PRB:", ':' );
                char success = line.charAt ( probePatternLength + line.substring ( probePatternLength ).indexOf ( ':' ) + 1 );
                ignoreNextProbe = success == '0';
                LOG.trace ( "analyseResponse: probe detected scanRunning=" + scanRunning + " line=" + line + " probe=" + probePoint + " success=" + success );
            }
            else {
                // v0.9g
                probePoint = parseCoordinates ( line, "PRB:", ']' ); // before v0.9j
                LOG.trace ( "analyseResponse: probe detected scanRunning=" + scanRunning + " line=" + line + " probe=" + probePoint );
            }

            if ( probePoint == null ) {
                LOG.error ( "analyseResponse: probe is null!" );
            }
            else {
                // transfer probe coordinates from machine to working coordinates
                if ( ignoreNextProbe ) {
                    ignoreNextProbe = false;
                }
                else {
                    if ( scanRunning ) gcodeProgram.setProbePoint ( probePoint.sub ( fixtureSshift ) );
                    eventBroker.send ( IEvent.AUTOLEVEL_UPDATE, probePoint );
                }
            }
        }
        else if ( line.startsWith ( "[" + lastCoordSelect ) ) {
            lastCoordSelectOffset = parseCoordinates ( line, lastCoordSelect + ":", ']' );
        }
        else if ( line.startsWith ( "[G54" ) ) {} // do nothing
        else if ( line.startsWith ( "[G55" ) ) {} // do nothing
        else if ( line.startsWith ( "[G56" ) ) {} // do nothing
        else if ( line.startsWith ( "[G57" ) ) {} // do nothing
        else if ( line.startsWith ( "[G58" ) ) {} // do nothing
        else if ( line.startsWith ( "[G59" ) ) {} // do nothing
        else if ( line.startsWith ( "[G28" ) ) {} // do nothing
        else if ( line.startsWith ( "[G30" ) ) {} // do nothing
        else if ( line.startsWith ( "[G92" ) ) {
            lastCoordSelectTempOffset = parseCoordinates ( line, "G92:", ']' );
            // System.out.println ( logName () + "receivedNotified: " + lastCoordSelect + "=" + lastCoordSelectOffset + " G92=" + lastCoordSelectTempOffset );
            fixtureSshift = lastCoordSelectOffset.add ( lastCoordSelectTempOffset );
            eventBroker.send ( IEvent.UPDATE_FIXTURE_OFFSET, fixtureSshift ); // inform all receivers only once, G92 is the last entry
            ignoreNextProbe = true;
        }
        else if ( line.startsWith ( "[G" ) ) {

            // System.out.println ( logName () + "receivedNotified: $G found" );

            // [G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]
            int startPos = 1; // without '['
            int endPos = line.indexOf ( ']' );
            String [] token = line.substring ( startPos, endPos ).split ( "\\s" );

            // if ( token.length != 12 ) {
            // // does not called
            // LOG.error ( "analyseResponse: NOT 12 TOKEN: line=" + line );
            // return;
            // }

            // token [0] -> motion mode
            EGcodeMode motionMode = EGcodeMode.identify ( token[0] );
            if ( lastMotionMode != motionMode && motionMode != EGcodeMode.GCODE_MODE_UNDEF ) {
                lastMotionMode = motionMode;
                eventBroker.send ( IEvent.UPDATE_MODAL_MODE, motionMode.getCommand () );
            }

            // token [1] -> G54 .. G59
            String coordSelect = token[1];
            if ( lastCoordSelect != coordSelect && !coordSelect.equals ( lastCoordSelect ) ) {
                lastCoordSelect = coordSelect;
                eventBroker.send ( IEvent.UPDATE_FIXTURE, coordSelect );
                sendCommandSuppressInTerminal ( "$#" );
            }

            // token [2] -> Plane XY ZY YZ
            String plane = "";
            switch ( token[2] ) {
                case "G17":
                    plane = "XY";
                    break;
                case "G18":
                    plane = "ZX";
                    break;
                case "G19":
                    plane = "YZ";
                    break;
                default:
                    break;
            }

            if ( !plane.equals ( lastPlane ) ) {
                lastPlane = plane;
                eventBroker.send ( IEvent.UPDATE_PLANE, plane );
            }

            // token [3] -> inch or mm
            String metricMode = token[3].equals ( "G20" ) ? "inch" : "mm";
            if ( !metricMode.equals ( lastMetricMode ) ) {
                lastMetricMode = metricMode;
                eventBroker.send ( IEvent.UPDATE_METRIC_MODE, metricMode );
            }

            // token [4] absolute or relative
            String distanceMode = token[4].equals ( "G90" ) ? "absolute" : "relative";
            if ( !distanceMode.equals ( lastDistanceMode ) ) {
                lastDistanceMode = distanceMode;
                eventBroker.send ( IEvent.UPDATE_DISTANCE_MODE, distanceMode );
            }

            // TODO token [5] path control mode G93 inverse time mode G94 units per minute mode G95 units per revision mode
            // TODO token [6] program control M0 temp stop M1 temp stop M2 return M30 return and "reset"

            // token [7] spindle mode M3 CW rot M4 CCW rot M5 stop
            String spindleMode = "unknown";
            switch ( token [7]) {
                case "M3":
                    spindleMode = "CW";
                    break;
                case "M4":
                    spindleMode = "CCW";
                    break;
                case "M5":
                    spindleMode = "";
                    break;
                default:
                    break;
            }
            if ( !spindleMode.equals ( lastSpindleMode ) ) {
                lastSpindleMode = spindleMode;
                eventBroker.send ( IEvent.UPDATE_SPINDLE_MODE, spindleMode );
            }

            // TODO token [8] coolant mode M7 mist coolant M8 flood coolant M9 stop coolant
            String coolantMode = "unknown";
            switch ( token[8] ) {
                case "M7":
                    coolantMode = "mist";
                    break;
                case "M8":
                    coolantMode = "flood";
                    break;
                case "M9":
                    coolantMode = "stop";
                    break;
                default:
                    break;
            }
            if ( !coolantMode.equals ( lastCoolantMode ) ) {
                lastCoolantMode = coolantMode;
                eventBroker.send ( IEvent.UPDATE_COOLANT_MODE, coolantMode );
            }

            // token [9] Tn tool number
            String tool = token[9];
            if ( !tool.equals ( lastTool ) ) {
                lastTool = tool;
                eventBroker.send ( IEvent.UPDATE_TOOL, tool );
            }

            // token [10] -> feedrate
            String feedrate = token[10].substring ( 1 ); // ignore 'F'
            if ( feedrate.endsWith ( "." ) ) feedrate = feedrate.substring ( 0, feedrate.length () - 1 );
            if ( !feedrate.equals ( lastFeedrate ) ) {
                lastFeedrate = feedrate;
                eventBroker.send ( IEvent.UPDATE_FEEDRATE, feedrate );
            }

            // token [12] -> spindle speed
            String spindlespeed = token[11].substring ( 1 ); // ignore 'S'
            if ( spindlespeed.endsWith ( "." ) ) spindlespeed = spindlespeed.substring ( 0, spindlespeed.length () - 1 );
            if ( !spindlespeed.equals ( lastSpindlespeed ) ) {
                lastSpindlespeed = spindlespeed;
                eventBroker.send ( IEvent.UPDATE_SPINDLESPEED, spindlespeed );
            }

        }

    }

    @Override
    public void setFixtureShift ( IGcodePoint shift ) {

        if ( shift != null ) fixtureSshift = shift;

    }

    @Override
    public IGcodePoint getFixtureShift () {

        return fixtureSshift;

    }

    @Override
    public void playGcodeProgram ( IGcodeProgram program ) {

        if ( isPlaying () || isAutolevelScan () ) return;

        if ( gcodeProgram != null ) {
            gcodeProgram.resetProcessed ();
        }

        gcodeProgram = program;
        gcodeProgram.resetProcessed ();

        // decouple from UI thread
        new GcodePlayerThread ().start ();

    }

    // TODO eliminiate extra parameters, hold this in the service as gui model
    @Override
    public void scanAutolevelData ( IGcodeProgram program, double zMin, double zMax, double zClearance, double probeFeedrate, boolean withError ) {

        if ( isPlaying () || isAutolevelScan () ) return;

        gcodeProgram = program;

        if ( IConstant.AUTOLEVEL_USE_RANDOM_Z_SIMULATION ) {
            
            gcodeProgram.prepareAutolevelScan ();

            new Thread ( ( ) -> {

                LOG.warn ( "scan: randomZSimulation" );

                eventBroker.send ( IEvent.AUTOLEVEL_START, getTimestamp () );

                scanRunning = true;

                final int xlength = gcodeProgram.getXSteps () + 1;
                final int ylength = gcodeProgram.getYSteps () + 1;

                for ( int i = 0; i < xlength; i++ ) {
                    for ( int j = 0; j < ylength; j++ ) {
                        if ( IConstant.AUTOLEVEL_SLOW_Z_SIMULATION ) {
                            try {
                                Thread.sleep ( 100 );
                            }
                            catch ( InterruptedException exc ) {}
                        }
                        double z = 3.0 * Math.random ();
                        IGcodePoint probe = gcodeProgram.getProbePointAt ( i, j ).addAxis ( 'Z', z );
                        gcodeProgram.setProbePoint ( probe );
                        LOG.debug ( "scan: probe=" + probe );
                        eventBroker.send ( IEvent.AUTOLEVEL_UPDATE, probe );
                    }
                }
                scanRunning = false;
                gcodeProgram.setAutolevelScanCompleted ();

                eventBroker.send ( IEvent.AUTOLEVEL_STOP, getTimestamp () );

            } ).start ();

        }
        else {

            // decouple from UI thread
            new ProbeScannerThread ( zMin, zMax, zClearance, probeFeedrate, withError ).start ();

        }

    }

    @Override
    public boolean isAutolevelScan () {

        return scanRunning;

    }

    @Override
    public boolean isPlaying () {

        return playRunning;

    }

    @Override
    public boolean isAlarm () {

        return skipByAlarm;

    }

    @SuppressWarnings("restriction")
    @Inject
    @Optional
    public void connectedNotified ( @EventTopic(IEvent.SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        playRunning = false;
        scanRunning = false;

        serial.setReceiver ( this );

        senderThread = new GcodeSenderThread ();
        senderThread.start ();

        statePollerThread = new GrblStatePollerThread ();
        statePollerThread.start ();

        parserStatePoller = new ParserStatePollerThread ();
        parserStatePoller.start ();

    }

    @SuppressWarnings("restriction")
    @Inject
    @Optional
    public void disconnectedNotified ( @EventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {
    
        LOG.debug ( "disconnectedNotified: param=" + param );
    
        senderThread.interrupt ();
        statePollerThread.interrupt ();
        parserStatePoller.interrupt ();
    
        try {
            senderThread.join ();
            statePollerThread.join ();
            parserStatePoller.join ();
        }
        catch ( InterruptedException exc ) {}

        LOG.debug ( "disconnectedNotified: all threads stopped" );
    
        senderThread = null;
        statePollerThread = null;
        parserStatePoller = null;
    
        resetLastVars ();
    
    }

    private void resetLastVars () {

        lastState = null;
        lastCoordSelectOffset = null;
        lastCoordSelectTempOffset = null;
        lastMotionMode = null;
        lastCoordSelect = null;
        lastPlane = null;
        lastMetricMode = null;
        lastDistanceMode = null;
        lastTool = null;
        lastSpindleMode = null;
        lastCoolantMode = null;
        lastFeedrate = null;
        lastSpindlespeed = null;

    }

    private String getTimestamp () {

        return new SimpleDateFormat ( "dd.MM.yyyy HH.mm:ss" ).format ( new Date () );

    }

    private void sendErrorMessageFromThread ( final String threadName, Exception exc ) {

        final String intialMsg = "Thread " + threadName + " not executed!";

        sendErrorMessage ( intialMsg, exc );

    }

    private void sendErrorMessage ( final String intialMsg, Exception exc ) {

        StringBuilder sb = new StringBuilder ();
        sb.append ( intialMsg );
        sb.append ( "\n\n" );
        sb.append ( "Cause:\n" );
        sb.append ( exc + "\n\n" );

        eventBroker.send ( IEvent.MESSAGE_ERROR, "" + sb );

    }

    protected class ProbeScannerThread extends Thread {
    
        private final static String THREAD_NAME = "gcode-scanner";
    
        private double zMin;
        private double zMax;
        private double zClearance;
        private double probeFeedrate;
        private boolean withError;
    
        public ProbeScannerThread ( double zMin, double zMax, double zClearance, double probeFeedrate, boolean withError ) {
    
            super ( THREAD_NAME );
    
            this.zMin = zMin;
            this.zMax = zMax;
            this.zClearance = zClearance;
            this.probeFeedrate = probeFeedrate;
            this.withError = withError;
    
        }
    
        @Override
        public synchronized void start () {
    
            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();
    
        }
    
        @SuppressWarnings("unused")
        @Override
        public void run () {
    
            gcodeProgram.setAutolevelStart ();
            sendCommand ( IConstant.GCODE_SCAN_START );

            final int xlength = gcodeProgram.getXSteps () + 1;
            final int ylength = gcodeProgram.getYSteps () + 1;
    
            IGcodePoint probePoint = gcodeProgram.getProbePointAt ( 0, 0 );
            sendCommandSuppressInTerminal ( "G21" );
            sendCommandSuppressInTerminal ( "G90" );
            sendCommandSuppressInTerminal ( "G0Z" + zClearance );
            sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
            sendCommandSuppressInTerminal ( "G0Z" + zMax );

            LOOP: for ( int i = 0; i < xlength; i++ ) {
    
                // TODO implement m�ander
                for ( int j = 0; j < ylength; j++ ) {

                    if ( skipByAlarm ) break LOOP;
    
                    probePoint = gcodeProgram.getProbePointAt ( i, j );

                    sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
                    if ( withError ) {
                        sendCommandSuppressInTerminal ( "G38.2Z" + zMin + "F" + probeFeedrate ); // MOTION_MODE_PROBE_TOWARD
                    }
                    else {
                        sendCommandSuppressInTerminal ( "G38.3Z" + zMin + "F" + probeFeedrate ); // MOTION_MODE_PROBE_TOWARD_NO_ERROR
                    }
                    sendCommandSuppressInTerminal ( "G0Z" + zMax );
    
                }
    
            }

            gcodeProgram.setAutolevelStop ();
            sendCommand ( IConstant.GCODE_SCAN_END );
    
            LOG.debug ( THREAD_NAME + ": stopped" );
    
        }
    }

    protected class GcodePlayerThread extends Thread {

        private final static String THREAD_NAME = "gcode-player";

        public GcodePlayerThread () {

            super ( THREAD_NAME );

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            playRunning = true;

            gcodeProgram.setPlayerStart ();
            eventBroker.send ( IEvent.PLAYER_START, getTimestamp () );

            boolean firstMove = true;
            IGcodeLine [] allGcodeLines = gcodeProgram.getAllGcodeLines ();
            for ( IGcodeLine gcodeLine : allGcodeLines ) {

                if ( skipByAlarm ) break;

                LOG.trace ( THREAD_NAME + ": line=" + gcodeLine.getLine () + " | gcodeLine=" + gcodeLine );
                eventBroker.send ( IEvent.PLAYER_LINE, gcodeLine );

                if ( gcodeProgram.isAutolevelScanComplete () && gcodeLine.isMotionModeLinear () ) {
                    final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                    final String feed = "F" + gcodeLine.getFeedrate ();
                    if ( gcodeLine.isMoveInXYZ () ) {
                        IGcodePoint [] path = gcodeProgram.interpolateLine ( gcodeLine.getStart (), gcodeLine.getEnd () );
                        for ( int i = 1; i < path.length; i++ ) {
                            // Attention: eliminate first point in path with index 0, because G0 lines has'nt autoleveled.
                            // So the last end point (autoleveled or not) is the start point of the next move
                            String segment = cmd;
                            segment += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getX () );
                            segment += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getY () );
                            segment += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getZ () );
                            segment += feed;
                            eventBroker.send ( IEvent.PLAYER_SEGMENT, segment );
                            sendCommandSuppressInTerminal ( segment );
                            LOG.trace ( "  segment=" + segment );
                        }
                    }
                }
                else {
                    // after rotation the original line is obsolet for motion commands
                    if ( gcodeLine.isMotionMode () ) {
                        final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                        String line = "";
                        if ( gcodeLine.isMoveInXYZ () ) {
                            line += cmd;
                            if ( gcodeLine.isMoveInX () || firstMove ) line += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getX () );
                            if ( gcodeLine.isMoveInY () || firstMove ) line += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getY () );
                            if ( gcodeLine.isMoveInZ () || firstMove ) line += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getZ () );
                            firstMove = false;
                        }
                        if ( gcodeLine.isMotionModeArc () ) line += "R" + gcodeLine.getRadius ();
                        if ( gcodeLine.isMotionModeLinear () || gcodeLine.isMotionModeArc () ) line += "F" + gcodeLine.getFeedrate ();
                        sendCommandSuppressInTerminal ( line );
                        LOG.trace ( "line=" + line );
                    }
                    else {
                        sendCommandSuppressInTerminal ( gcodeLine.getLine () );
                    }
                }
                gcodeLine.setProcessed ( true );

            }

            gcodeProgram.setPlayerStop ();
            eventBroker.send ( IEvent.PLAYER_STOP, getTimestamp () );
            // TODO
            // if ( skipByAlarm ) {
            // eventBroker.send ( EVENT_GCODE_PLAYER_CANCELED, getTimestamp () );
            // }

            playRunning = false;

            LOG.debug ( THREAD_NAME + ": stopped" );

        }

    }

    public class GcodeSenderThread extends Thread {

        private final static String THREAD_NAME = "gcode-sender";

        public GcodeSenderThread () {

            super ( THREAD_NAME );

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            while ( !isInterrupted () ) {

                try {

                    while ( waitForOk ) {
                        sleep ( 100 );
                    }

                    GrblRequestImpl cmd = queue.take ();
                    if ( !skipByAlarm || cmd.isReset () || cmd.isHome () || cmd.isUnlock () || cmd.message.startsWith ( IConstant.GCODE_SCAN_END ) ) {

                        if ( cmd.message.startsWith ( IConstant.GCODE_SCAN_START ) ) {
                            scanRunning = true;
                            eventBroker.send ( IEvent.AUTOLEVEL_START, getTimestamp () );
                        }
                        else if ( cmd.message.startsWith ( IConstant.GCODE_SCAN_END ) ) {
                            scanRunning = false;
                            if ( !skipByAlarm ) gcodeProgram.setAutolevelScanCompleted ();
                            eventBroker.send ( IEvent.AUTOLEVEL_STOP, getTimestamp () ); //
                        }
                        else {

                            waitForOk = true;
                            suppressInTerminal = cmd.suppressInTerminal;

                            byte [] buffer = cmd.message.getBytes ( StandardCharsets.US_ASCII );
                            eventBroker.send ( IEvent.GRBL_SENT, cmd );

                            serial.send ( buffer );

                        }

                    }

                }
                catch ( InterruptedException exc ) {
                    interrupt (); // this is necessary to reach the join
                }
                // TODO catch throwable and restart (if necesarry a new thread (robust implementation)

            }

            LOG.debug ( THREAD_NAME + ": stopped" );

        }

    }

    protected class GrblStatePollerThread extends Thread {

        private final static String THREAD_NAME = "grbl-state-poller";

        public GrblStatePollerThread () {

            super ( THREAD_NAME );

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            while ( !isInterrupted () ) {
                try {
                    sleep ( GRBL_STATE_POLLER_SLEEP_MS );
                    sendSingleSignCommand ( '?' );
                }
                catch ( InterruptedException exc ) {
                    interrupt ();
                }
                // TODO catch throwable and restart (if necesarry a new thread (robust implementation)
            }

            LOG.debug ( THREAD_NAME + ": stopped" );

        }

    }

    protected class ParserStatePollerThread extends Thread {

        private final static String THREAD_NAME = "parser-state-poller";

        public ParserStatePollerThread () {

            super ( THREAD_NAME );

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            while ( !isInterrupted () ) {
                try {
                    sendCommandSuppressInTerminal ( "$G" );
                    sleep ( PARSER_STATE_POLLER_SLEEP_MS );
                }
                catch ( InterruptedException exc ) {
                    interrupt ();
                }
                // TODO catch throwable and restart (if necesarry a new thread (robust implementation)
            }

            LOG.debug ( THREAD_NAME + ": stopped" );

        }
    }

}
