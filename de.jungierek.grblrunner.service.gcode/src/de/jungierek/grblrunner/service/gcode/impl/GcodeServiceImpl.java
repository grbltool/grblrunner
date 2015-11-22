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

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
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
    protected ArrayBlockingQueue<GcodeResponseImpl> queue = new ArrayBlockingQueue<GcodeResponseImpl> ( IConstants.GCODE_QUEUE_LENGTH, false );

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
        new Thread ( ( ) -> sendSingleSignCommand ( IConstants.GRBL_RESET_CODE ) ).start ();
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

            queue.put ( new GcodeResponseImpl ( suppressInTerminal, line + IConstants.LF ) );

            if ( line.startsWith ( "G92" ) || line.startsWith ( "G10" ) ) { // detect G92 or G10 to update shift
                queue.put ( new GcodeResponseImpl ( true, "$#" + IConstants.LF ) );
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
        eventBroker.send ( IEvents.GRBL_RECEIVED, new GcodeResponseImpl ( suppressLine, line ) );

        // ... nächstes Kommando frei geben
        if ( releaseWaitForOk ) waitForOk = false;

    }

    private GcodePointImpl parseCoordinates ( String line, String intro, char closingChar ) {

        return new GcodePointImpl ( parseVector ( line, IConstants.AXIS.length, intro, closingChar ) );

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

    private void analyseResponse ( String line ) {

        if ( line.startsWith ( "ALARM" ) ) {

            eventBroker.send ( IEvents.GRBL_ALARM, line ); // inform about alarm message

        }
        else if ( line.startsWith ( "Grbl" ) ) {

            eventBroker.send ( IEvents.GRBL_RESTARTED, line ); // inform about grbl restart

        }
        else if ( line.startsWith ( "<" ) ) { // we found state line

            EGrblState state = EGrblState.identify ( line );

            GcodePointImpl m = parseCoordinates ( line, "MPos:", ',' );
            GcodePointImpl w = parseCoordinates ( line, "WPos:", '>' );
            GcodeGrblStateImpl gcodeState = new GcodeGrblStateImpl ( state, m, w );

            // update only on change
            if ( lastState == null || !lastState.equals ( gcodeState ) ) {
                lastState = gcodeState;
                eventBroker.send ( IEvents.UPDATE_STATE, gcodeState );
            }

        }
        else if ( line.startsWith ( "[PRB:" ) ) {
            // System.out.println ( logName () + "receivedNotified: send update probe line=" + line );
            // probe sends all cooridnates in machine system
            GcodePointImpl probePoint = null;
            if ( line.substring ( 5 ).indexOf ( ':' ) > 0 ) { // test after "[PRB:"
                // we are in v0.9j
                probePoint = parseCoordinates ( line, "PRB:", ':' );
                char success = line.charAt ( line.indexOf ( ':' ) + 1 );
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
                if ( scanRunning ) gcodeProgram.setProbePoint ( probePoint.sub ( fixtureSshift ) );
                if ( ignoreNextProbe ) ignoreNextProbe = false;
                else eventBroker.send ( IEvents.AUTOLEVEL_UPDATE, probePoint );
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
            eventBroker.send ( IEvents.UPDATE_FIXTURE_OFFSET, fixtureSshift ); // inform all receivers only once, G92 is the last entry
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
                eventBroker.send ( IEvents.UPDATE_MOTION_MODE, motionMode.getCommand () );
            }

            // token [1] -> G54 .. G59
            String coordSelect = token[1];
            if ( lastCoordSelect != coordSelect && !coordSelect.equals ( lastCoordSelect ) ) {
                lastCoordSelect = coordSelect;
                eventBroker.send ( IEvents.UPDATE_FIXTURE, coordSelect );
                sendCommandSuppressInTerminal ( "$#" );
            }

            // token [2] -> Plane XY ZY YZ
            String plane = "";
            switch ( token[2] ) {
                case "G17":
                    plane = "XY";
                    break;
                case "G18":
                    plane = "ZY";
                    break;
                case "G19":
                    plane = "YZ";
                    break;
                default:
                    break;
            }

            if ( !plane.equals ( lastPlane ) ) {
                lastPlane = plane;
                eventBroker.send ( IEvents.UPDATE_PLANE, plane );
            }

            // token [3] -> inch or mm
            String metricMode = token[3].equals ( "G20" ) ? "inch" : "mm";
            if ( !metricMode.equals ( lastMetricMode ) ) {
                lastMetricMode = metricMode;
                eventBroker.send ( IEvents.UPDATE_METRIC_MODE, metricMode );
            }

            // token [4] absolute or relative
            String distanceMode = token[4].equals ( "G90" ) ? "absolute" : "relative";
            if ( !distanceMode.equals ( lastDistanceMode ) ) {
                lastDistanceMode = distanceMode;
                eventBroker.send ( IEvents.UPDATE_DISTANCE_MODE, distanceMode );
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
                eventBroker.send ( IEvents.UPDATE_SPINDLE_MODE, spindleMode );
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
                eventBroker.send ( IEvents.UPDATE_COOLANT_MODE, coolantMode );
            }

            // token [9] Tn tool number
            String tool = token[9];
            if ( !tool.equals ( lastTool ) ) {
                lastTool = tool;
                eventBroker.send ( IEvents.UPDATE_TOOL, tool );
            }

            // token [10] -> feedrate
            String feedrate = token[10].substring ( 1 ); // ignore 'F'
            if ( feedrate.endsWith ( "." ) ) feedrate = feedrate.substring ( 0, feedrate.length () - 1 );
            if ( !feedrate.equals ( lastFeedrate ) ) {
                lastFeedrate = feedrate;
                eventBroker.send ( IEvents.UPDATE_FEEDRATE, feedrate );
            }

            // token [12] -> spindle speed
            String spindlespeed = token[11].substring ( 1 ); // ignore 'S'
            if ( spindlespeed.endsWith ( "." ) ) spindlespeed = spindlespeed.substring ( 0, spindlespeed.length () - 1 );
            if ( !spindlespeed.equals ( lastSpindlespeed ) ) {
                lastSpindlespeed = spindlespeed;
                eventBroker.send ( IEvents.UPDATE_SPINDLESPEED, spindlespeed );
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

        if ( isPlaying () || isScanning () ) return;

        gcodeProgram = program;

        // decouple from UI thread
        new GcodePlayerThread ().start ();

    }

    // TODO eliminiate extra parameters, hold this in the service as gui model
    @Override
    public void scanAutolevelData ( IGcodeProgram program, double zMin, double zMax, double zClearance, double probeFeedrate ) {

        if ( isPlaying () || isScanning () ) return;

        gcodeProgram = program;

        if ( IPreferences.AUTOLEVEL_USE_RANDOM_Z_SIMULATION ) {
            
            gcodeProgram.prepareAutolevelScan ();

            new Thread ( ( ) -> {

                LOG.warn ( "scan: randomZSimulation" );

                eventBroker.send ( IEvents.AUTOLEVEL_START, getTimestamp () );

                scanRunning = true;

                final int xlength = gcodeProgram.getXSteps () + 1;
                final int ylength = gcodeProgram.getYSteps () + 1;

                for ( int i = 0; i < xlength; i++ ) {
                    for ( int j = 0; j < ylength; j++ ) {
                        if ( IPreferences.AUTOLEVEL_SLOW_Z_SIMULATION ) {
                            try {
                                Thread.sleep ( 100 );
                            }
                            catch ( InterruptedException exc ) {}
                        }
                        double z = 3.0 * Math.random ();
                        IGcodePoint probe = gcodeProgram.getProbePointAt ( i, j ).addAxis ( 'Z', z );
                        gcodeProgram.setProbePoint ( probe );
                        LOG.debug ( "scan: probe=" + probe );
                        eventBroker.send ( IEvents.AUTOLEVEL_UPDATE, probe );
                    }
                }
                scanRunning = false;
                gcodeProgram.setAutolevelScanCompleted ();

                eventBroker.send ( IEvents.AUTOLEVEL_STOP, getTimestamp () );

            } ).start ();

        }
        else {

            // decouple from UI thread
            new ProbeScannerThread ( zMin, zMax, zClearance, probeFeedrate ).start ();

        }

    }

    @Override
    public boolean isScanning () {

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
    public void connectedNotified ( @EventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

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
    public void disconnectedNotified ( @EventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {
    
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

        eventBroker.send ( IEvents.MESSAGE_ERROR, "" + sb );

    }

    protected class ProbeScannerThread extends Thread {
    
        private final static String THREAD_NAME = "gcode-scanner";
    
        private double zMin;
        private double zMax;
        private double zClearance;
        private double probeFeedrate;
    
        public ProbeScannerThread ( double zMin, double zMax, double zClearance, double probeFeedrate ) {
    
            super ( THREAD_NAME );
    
            this.zMin = zMin;
            this.zMax = zMax;
            this.zClearance = zClearance;
            this.probeFeedrate = probeFeedrate;
    
        }
    
        @Override
        public synchronized void start () {
    
            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();
    
        }
    
        @SuppressWarnings("unused")
        @Override
        public void run () {
    
            sendCommand ( IConstants.GCODE_SCAN_START );

            final int xlength = gcodeProgram.getXSteps () + 1;
            final int ylength = gcodeProgram.getYSteps () + 1;
    
            IGcodePoint probePoint = gcodeProgram.getProbePointAt ( 0, 0 );
            sendCommandSuppressInTerminal ( "G21" );
            sendCommandSuppressInTerminal ( "G90" );
            sendCommandSuppressInTerminal ( "G0Z" + zClearance );
            sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
            sendCommandSuppressInTerminal ( "G0Z" + zMax );

            for ( int i = 0; i < xlength; i++ ) {
    
                if ( skipByAlarm ) break;

                // TODO implement mäander
                for ( int j = 0; j < ylength; j++ ) {

                    if ( skipByAlarm ) break;
    
                    // progressListener.tick ();
    
                    probePoint = gcodeProgram.getProbePointAt ( i, j );
    
                    if ( true ) {
                        sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
                        sendCommandSuppressInTerminal ( "G38.2Z" + zMin + "F" + probeFeedrate );
                        sendCommandSuppressInTerminal ( "G0Z" + zMax );
                    }
                    else {
                        sendCommand ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
                        sendCommand ( "G38.2Z" + zMin + "F" + probeFeedrate );
                        sendCommand ( "G0Z" + zMax );
                    }
    
                }
    
            }
    
            sendCommand ( IConstants.GCODE_SCAN_END );
    
            LOG.debug ( "stopped" );
    
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

            eventBroker.send ( IEvents.PLAYER_START, getTimestamp () );

            gcodeProgram.resetProcessed ();

            gcodeProgram.visit ( new IGcodeModelVisitor () {

                @Override
                public void visit ( IGcodeLine gcodeLine ) {

                    if ( skipByAlarm ) return;

                    LOG.debug ( THREAD_NAME + ": line=" + gcodeLine.getLine () + " | gcodeLine=" + gcodeLine );
                    eventBroker.send ( IEvents.PLAYER_LINE, gcodeLine );

                    LOG.debug ( THREAD_NAME + ": line=" + gcodeLine.getLine () );
                    LOG.info ( "orig=" + gcodeLine.getLine () );
                    // if ( gcodeModel.getTheProgram ().isScanDataComplete () && gcodeLine.isMotionMode () ) {
                    if ( gcodeProgram.isAutolevelScanComplete () && gcodeLine.getGcodeMode () == EGcodeMode.MOTION_MODE_LINEAR ) {
                        final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                        final String feed = "F" + gcodeLine.getFeedrate ();
                        if ( gcodeLine.isMoveInXYZ () ) {
                            IGcodePoint [] path = gcodeProgram.interpolateLine ( gcodeLine.getStart (), gcodeLine.getEnd () );
                            for ( int i = 1; i < path.length; i++ ) {
                                // TODO eliminate first point?
                                String segment = cmd;
                                segment += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getX () );
                                segment += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getY () );
                                segment += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getZ () );
                                segment += feed;
                                eventBroker.send ( IEvents.PLAYER_SEGMENT, segment );
                                sendCommandSuppressInTerminal ( segment );
                                LOG.info ( "  segment=" + segment );
                            }
                        }
                    }
                    else {
                        // after rotation the original line is obsolet for motion commands
                        if ( gcodeLine.isMotionMode () ) {
                            final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                            final String feed = "F" + gcodeLine.getFeedrate ();
                            String line = "";
                            if ( gcodeLine.isMoveInXYZ () ) {
                                line += cmd;
                                if ( gcodeLine.isMoveInX () ) line += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getX () );
                                if ( gcodeLine.isMoveInY () ) line += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getY () );
                                if ( gcodeLine.isMoveInZ () ) line += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getZ () );
                            }
                            line += feed;
                            sendCommandSuppressInTerminal ( line );
                            LOG.info ( "line=" + line );
                        }
                        else {
                             sendCommandSuppressInTerminal ( gcodeLine.getLine () );
                        }
                    }
                    gcodeLine.setProcessed ( true );
                }

            } );

            eventBroker.send ( IEvents.PLAYER_STOP, getTimestamp () );
            // TODO
            // if ( skipByAlarm ) {
            // eventBroker.send ( EVENT_GCODE_PLAYER_CANCELED, getTimestamp () );
            // }

            playRunning = false;

            LOG.debug ( "stopped" );

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

                    GcodeResponseImpl cmd = queue.take ();
                    if ( !skipByAlarm || cmd.isReset () || cmd.isHome () || cmd.isUnlock () ) {

                        if ( cmd.line.startsWith ( IConstants.GCODE_SCAN_START ) ) {
                            scanRunning = true;
                            eventBroker.send ( IEvents.AUTOLEVEL_START, getTimestamp () );
                        }
                        else if ( cmd.line.startsWith ( IConstants.GCODE_SCAN_END ) ) {
                            scanRunning = false;
                            // TODO think about this
                            gcodeProgram.setAutolevelScanCompleted ();
                            eventBroker.send ( IEvents.AUTOLEVEL_STOP, getTimestamp () ); //
                        }
                        else {

                            waitForOk = true;
                            suppressInTerminal = cmd.suppressInTerminal;

                            byte [] buffer = cmd.line.getBytes ( StandardCharsets.US_ASCII );
                            eventBroker.send ( IEvents.GRBL_SENT, cmd );

                            serial.send ( buffer );

                        }

                    }

                }
                catch ( InterruptedException exc ) {
                    interrupt (); // this is necessary to reach the join
                }
                // TODO catch throwable and restart (if necesarry a new thread (robust implementation)

            }

            LOG.debug ( "stopped" );

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

            LOG.debug ( "stopped" );

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

            LOG.debug ( "stopped" );

        }
    }

}
