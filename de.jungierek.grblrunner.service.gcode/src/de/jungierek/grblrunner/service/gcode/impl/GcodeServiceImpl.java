package de.jungierek.grblrunner.service.gcode.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.gcode.IPreferences;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.service.serial.ISerialServiceReceiver;

public class GcodeServiceImpl implements IGcodeService, ISerialServiceReceiver {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeServiceImpl.class );

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private ISerialService serial;

    @Inject
    private IGcodeModel gcodeModel;

    public static final String [] AXIS = { "X", "Y", "Z" };

    public static final String EVENT_GCODE_ALL = "TOPIC_GCODE/*";

    public static final String EVENT_GCODE_ALARM = "TOPIC_GCODE/ALARM";
    public static final String EVENT_GCODE_GRBL_RESTARTED = "TOPIC_GCODE/GRBL_RESTARTED";

    public static final String EVENT_GCODE_SENT = "TOPIC_GCODE/SENT";
    public static final String EVENT_GCODE_RECEIVED = "TOPIC_GCODE/RECEIVED";

    public static final String EVENT_GCODE_UPDATE_STATE = "TOPIC_GCODE/UPDATE_STATE";
    public static final String EVENT_GCODE_UPDATE_MOTION_MODE = "TOPIC_GCODE/UPDATE_MOTION_MODE";
    public static final String EVENT_GCODE_UPDATE_COORD_SELECT = "TOPIC_GCODE/UPDATE_COORD_SELECT";
    public static final String EVENT_GCODE_UPDATE_COORD_SELECT_OFFSET = "TOPIC_GCODE/UPDATE_COORD_SELECT_OFFSET";
    public static final String EVENT_GCODE_UPDATE_PLANE = "TOPIC_GCODE/UPDATE_PLANE";
    public static final String EVENT_GCODE_UPDATE_METRIC_MODE = "TOPIC_GCODE/UPDATE_METRIC_MODE";
    public static final String EVENT_GCODE_UPDATE_FEEDRATE = "TOPIC_GCODE/UPDATE_FEEDRATE";
    public static final String EVENT_GCODE_UPDATE_SPINDLESPEED = "TOPIC_GCODE/UPDATE_SPINDLESPEED";
    public static final String EVENT_GCODE_UPDATE_DISTANCE_MODE = "TOPIC_GCODE/UPDATE_DISTANCE_MODE";
    public static final String EVENT_GCODE_UPDATE_TOOL = "TOPIC_GCODE/UPDATE_TOOL";
    public static final String EVENT_GCODE_UPDATE_SPINDLE_MODE = "TOPIC_GCODE/UPDATE_SPINDLE_MODE";
    public static final String EVENT_GCODE_UPDATE_COOLANT_MODE = "TOPIC_GCODE/UPDATE_COOLANT_MODE";
    public static final String EVENT_GCODE_UPDATE_PROBE = "TOPIC_GCODE/UPDATE_PROBE";

    public static final String EVENT_PROBE_DATA_SAVED = "TOPIC_GCODE/PROBE_DATA_SAVED";
    public static final String EVENT_PROBE_DATA_LOADED = "TOPIC_GCODE/PROBE_DATA_LOADED";
    public static final String EVENT_PROBE_DATA_CLEARED = "TOPIC_GCODE/PROBE_DATA_CLEARED";

    public static final String EVENT_GCODE_PLAYER_LOADED = "TOPIC_GCODE/PLAYER_LOADED";
    public static final String EVENT_GCODE_PLAYER_START = "TOPIC_GCODE/PLAYER_START";
    public static final String EVENT_GCODE_PLAYER_STOP = "TOPIC_GCODE/PLAYER_STOP";
    public static final String EVENT_GCODE_PLAYER_LINE = "TOPIC_GCODE/PLAYER_LINE";
    public static final String EVENT_GCODE_PLAYER_LINE_SEGMENT = "TOPIC_GCODE/PLAYER_SEGMENT";
    public static final String EVENT_GCODE_SCAN_START = "TOPIC_GCODE/SCAN_START";
    public static final String EVENT_GCODE_SCAN_STOP = "TOPIC_GCODE/SCAN_STOP";

    public static final String EVENT_SERIAL_CONNECTED = "TOPIC_SERIAL/CONNECTED";
    public static final String EVENT_SERIAL_DISCONNECTED = "TOPIC_SERIAL/DISCONNECTED";

    public static final String SCAN_START = "SCAN_START";
    public static final String SCAN_END = "SCAN_END";

    public static final String EVENT_MSG_ERROR = "TOPIC_MSG/ERROR";
    public static final String EVENT_MSG_INFO = "TOPIC_MSG/INFO";
    public static final String EVENT_MSG_WARNING = "TOPIC_MSG/WARNING";

    // protected for test purposes
    public static final int QUEUE_LENGTH = 20;
    protected ArrayBlockingQueue<GcodeResponseImpl> queue = new ArrayBlockingQueue<GcodeResponseImpl> ( QUEUE_LENGTH, false );

    private File gcodeFile, probeDataFile;

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

    @Inject
    public GcodeServiceImpl () {}

    // only for test
    protected GcodeServiceImpl ( IEventBroker eventBroker, ISerialService serial, IGcodeModel gcodeModel ) {

        this.eventBroker = eventBroker;
        this.serial = serial;
        this.gcodeModel = gcodeModel;

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
        new Thread ( ( ) -> sendSingleSignCommand ( IGcodeService.GRBL_RESET_CODE ) ).start ();
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

    @Override
    public void sendCommand ( String line, boolean suppressInTerminal ) {

        try {

            queue.put ( new GcodeResponseImpl ( suppressInTerminal, line + LF ) );

            if ( line.startsWith ( "G92" ) || line.startsWith ( "G10" ) ) { // detect G92 or G10 to update shift
                queue.put ( new GcodeResponseImpl ( true, "$#" + LF ) );
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
        eventBroker.send ( EVENT_GCODE_RECEIVED, new GcodeResponseImpl ( suppressLine, line ) );

        // ... nächstes Kommando frei geben
        if ( releaseWaitForOk ) waitForOk = false;

    }

    private GcodePointImpl parseCoordinates ( String line, String intro, char closingChar ) {

        return new GcodePointImpl ( parseVector ( line, AXIS.length, intro, closingChar ) );

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

            eventBroker.send ( EVENT_GCODE_ALARM, line ); // inform about alarm message

        }
        else if ( line.startsWith ( "Grbl" ) ) {

            eventBroker.send ( EVENT_GCODE_GRBL_RESTARTED, line ); // inform about grbl restart

        }
        else if ( line.startsWith ( "<" ) ) { // we found state line

            EGrblState state = EGrblState.identify ( line );

            GcodePointImpl m = parseCoordinates ( line, "MPos:", ',' );
            GcodePointImpl w = parseCoordinates ( line, "WPos:", '>' );
            GcodeGrblStateImpl gcodeState = new GcodeGrblStateImpl ( state, m, w );

            // update only on change
            if ( lastState == null || !lastState.equals ( gcodeState ) ) {
                lastState = gcodeState;
                eventBroker.send ( EVENT_GCODE_UPDATE_STATE, gcodeState );
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
                if ( scanRunning ) gcodeModel.setProbePoint ( probePoint );
                if ( ignoreNextProbe ) ignoreNextProbe = false;
                else eventBroker.send ( EVENT_GCODE_UPDATE_PROBE, probePoint );
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
            gcodeModel.setShift ( lastCoordSelectOffset.add ( lastCoordSelectTempOffset ) );
            eventBroker.send ( EVENT_GCODE_UPDATE_COORD_SELECT_OFFSET, null ); // inform all receivers only once, G92 is the last entry
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
                eventBroker.send ( EVENT_GCODE_UPDATE_MOTION_MODE, motionMode.getCommand () );
            }

            // token [1] -> G54 .. G59
            String coordSelect = token[1];
            if ( lastCoordSelect != coordSelect && !coordSelect.equals ( lastCoordSelect ) ) {
                lastCoordSelect = coordSelect;
                eventBroker.send ( EVENT_GCODE_UPDATE_COORD_SELECT, coordSelect );
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
                eventBroker.send ( EVENT_GCODE_UPDATE_PLANE, plane );
            }

            // token [3] -> inch or mm
            String metricMode = token[3].equals ( "G20" ) ? "inch" : "mm";
            if ( !metricMode.equals ( lastMetricMode ) ) {
                lastMetricMode = metricMode;
                eventBroker.send ( EVENT_GCODE_UPDATE_METRIC_MODE, metricMode );
            }

            // token [4] absolute or relative
            String distanceMode = token[4].equals ( "G90" ) ? "absolute" : "relative";
            if ( !distanceMode.equals ( lastDistanceMode ) ) {
                lastDistanceMode = distanceMode;
                eventBroker.send ( EVENT_GCODE_UPDATE_DISTANCE_MODE, distanceMode );
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
                eventBroker.send ( EVENT_GCODE_UPDATE_SPINDLE_MODE, spindleMode );
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
                eventBroker.send ( EVENT_GCODE_UPDATE_COOLANT_MODE, coolantMode );
            }

            // token [9] Tn tool number
            String tool = token[9];
            if ( !tool.equals ( lastTool ) ) {
                lastTool = tool;
                eventBroker.send ( EVENT_GCODE_UPDATE_TOOL, tool );
            }

            // token [10] -> feedrate
            String feedrate = token[10].substring ( 1 ); // ignore 'F'
            if ( feedrate.endsWith ( "." ) ) feedrate = feedrate.substring ( 0, feedrate.length () - 1 );
            if ( !feedrate.equals ( lastFeedrate ) ) {
                lastFeedrate = feedrate;
                eventBroker.send ( EVENT_GCODE_UPDATE_FEEDRATE, feedrate );
            }

            // token [12] -> spindle speed
            String spindlespeed = token[11].substring ( 1 ); // ignore 'S'
            if ( spindlespeed.endsWith ( "." ) ) spindlespeed = spindlespeed.substring ( 0, spindlespeed.length () - 1 );
            if ( !spindlespeed.equals ( lastSpindlespeed ) ) {
                lastSpindlespeed = spindlespeed;
                eventBroker.send ( EVENT_GCODE_UPDATE_SPINDLESPEED, spindlespeed );
            }

        }

    }

    @Override
    public File getGcodeFile () {

        return gcodeFile;

    }

    @Override
    public File getProbeDataFile () {

        return probeDataFile;

    }

    @Override
    public void load ( File gcodeFile ) {

        this.gcodeFile = gcodeFile;
        String fileName = gcodeFile.getPath ();
        probeDataFile = new File ( fileName.substring ( 0, fileName.lastIndexOf ( '.' ) ) + ".probe" );

        // decouple from UI thread
        new GcodeLoaderThread ( gcodeFile ).start ();

    }

    @Override
    public void play () {

        // decouple from UI thread
        new GcodePlayerThread ().start ();

    }

    @Override
    public void scan ( double zMin, double zMax, double zClearance, double probeFeedrate ) {

        if ( IPreferences.USE_RANDOM_Z_SIMULATION ) {
            
            new Thread ( ( ) -> {

                LOG.warn ( "scan: randomZSimulation" );

                eventBroker.send ( EVENT_GCODE_SCAN_START, getTimestamp () );

                scanRunning = true;
                IGcodePoint [][] m = gcodeModel.getScanMatrix ();
                for ( int i = 0; i < m.length; i++ ) {
                    for ( int j = 0; j < m[i].length; j++ ) {
                        if ( IPreferences.SLOW_Z_SIMULATION ) {
                            try {
                                Thread.sleep ( 100 );
                            }
                            catch ( InterruptedException exc ) {}
                        }
                        double z = 3.0 * Math.random ();
                        final GcodePointImpl probe = new GcodePointImpl ( m[i][j].getX (), m[i][j].getY (), z );
                        m[i][j] = probe;
                        LOG.debug ( "scan: probe=" + probe );
                        IGcodePoint p = probe;
                        eventBroker.send ( EVENT_GCODE_UPDATE_PROBE, p );
                    }
                }
                scanRunning = false;
                gcodeModel.setScanDataCompleted ();

                eventBroker.send ( EVENT_GCODE_SCAN_STOP, getTimestamp () );

            } ).start ();

        }
        else {

            // decouple from UI thread
            new ProbeScannerThread ( zMin, zMax, zClearance, probeFeedrate ).start ();

        }

    }

    @Override
    public void loadProbeData () {

        if ( playRunning || scanRunning ) {
            eventBroker.send ( EVENT_MSG_ERROR, "Laden der Höhendaten nicht möglich, da laufende Aktivitäten" );
            return;
        }

        // decouple from UI thread
        new ProbeLoaderThread ( probeDataFile ).start ();

    }

    @Override
    public void saveProbeData () {

        if ( playRunning || scanRunning || !gcodeModel.isScanDataComplete () ) return;

        // decouple from UI thread
        new ProbeSaverThread ( probeDataFile ).start ();

    }

    @Override
    public void clearProbeData () {

        gcodeModel.disposeProbeData ();

        eventBroker.send ( EVENT_PROBE_DATA_CLEARED, probeDataFile.getPath () );

    }

    @Override
    public boolean isScanning () {

        return scanRunning;

    }

    @Override
    public boolean isPlaying () {

        return playRunning;

    }

    @SuppressWarnings("restriction")
    @Inject
    @Optional
    public void connectedNotified ( @EventTopic(EVENT_SERIAL_CONNECTED) String portName ) {

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
    public void disconnectedNotified ( @EventTopic(EVENT_SERIAL_DISCONNECTED) String param ) {
    
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

        eventBroker.send ( EVENT_MSG_ERROR, "" + sb );

    }

    protected class ProbeLoaderThread extends Thread {

        private final static String THREAD_NAME = "probe-file-loader";

        private File file;

        public ProbeLoaderThread ( File file ) {

            super ( THREAD_NAME + " " + file.getName () );
            this.file = file;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            try ( BufferedReader reader = new BufferedReader ( new FileReader ( file ) ) ) {

                String line;
                double [] dim = null;
                GcodePointImpl min = null, max = null;
                double stepWidthX, stepWidthY;
                IGcodePoint [][] matrix = null;

                while ( (line = reader.readLine ()) != null ) {

                    if ( line.startsWith ( "*" ) ) {
                        // igmore comment
                    }
                    else if ( line.startsWith ( "dim" ) ) {
                        dim = parseVector ( line, 2, "=[", ']' );
                        LOG.debug ( "dim_x=" + dim[0] + ", dim_y=" + dim[1] );
                    }
                    else if ( line.startsWith ( "min" ) ) {
                        min = parseCoordinates ( line, "=[", ']' );
                        LOG.debug ( "min=" + min );
                    }
                    else if ( line.startsWith ( "max" ) ) {
                        max = parseCoordinates ( line, "=[", ']' );
                        LOG.debug ( "max=" + max );
                    }
                    else if ( line.startsWith ( "stepWidthX" ) ) {
                        stepWidthX = parseDouble ( 0.0, line.substring ( line.indexOf ( '=' ) + 1 ) );
                        LOG.debug ( "stepWidthX=" + stepWidthX );
                    }
                    else if ( line.startsWith ( "stepWidthY" ) ) {
                        stepWidthY = parseDouble ( 0.0, line.substring ( line.indexOf ( '=' ) + 1 ) );
                        LOG.debug ( "stepWidthY=" + stepWidthY );
                        // check size of gcode area
                        if ( min.zeroAxis ( 'Z' ).equals ( gcodeModel.getMin ().zeroAxis ( 'Z' ) ) && max.zeroAxis ( 'Z' ).equals ( gcodeModel.getMax ().zeroAxis ( 'Z' ) ) ) {
                            gcodeModel.prepareAutolevelScan ( (int) dim[0] - 1, (int) dim[1] - 1 );
                            matrix = gcodeModel.getScanMatrix ();
                        }
                        else {
                            LOG.error ( "gcode area differs" );
                            eventBroker.send ( EVENT_MSG_ERROR,
                                    "GCODE area differs!\nmin1=" + min + " min2=" + gcodeModel.getMin () + "\nmax1=" + max + " max2=" + gcodeModel.getMax () );
                            return;
                        }
                    }
                    else if ( line.startsWith ( "m(" ) ) {
                        double [] ij = parseVector ( line, 2, "m(", ')' );
                        int i = (int) ij[0];
                        int j = (int) ij[1];
                        GcodePointImpl p = parseCoordinates ( line, ")=[", ']' );
                        LOG.debug ( ("i=" + ij[0] + " j=" + ij[1] + " z=" + p.getZ ()) );
                        // matrix[i][j] = matrix[i][j].addAxis ( 'Z', p );
                        matrix[i][j] = new GcodePointImpl ( matrix[i][j].getX (), matrix[i][j].getY (), p.getZ () );
                    }
                    else if ( line.startsWith ( "end of data" ) ) {
                        gcodeModel.setScanDataCompleted ();
                        break;
                        // LOG.debug ( "never be here" );
                    }
                }

                reader.close ();

                eventBroker.send ( EVENT_PROBE_DATA_LOADED, file.getPath () );

            }
            catch ( IOException exc ) {
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

            LOG.debug ( "stopped" );

        }

    }

    protected class ProbeSaverThread extends Thread {

        private final static String THREAD_NAME = "probe-file-saver";

        private File file;

        public ProbeSaverThread ( File file ) {

            super ( THREAD_NAME + " " + file.getName () );
            this.file = file;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            // Vorbedingungen
            // 1. ein Gcode-Model muss da sein
            // 2. eine Scan Matrix muss da sein
            //
            // Speichern
            // a) Abmaße des Gcode Models
            // b) steps
            // c) Dimension der Matrix
            // d) alle Probe Points

            IGcodePoint [][] matrix = gcodeModel.getScanMatrix ();
            IGcodePoint min = gcodeModel.getMin ();
            IGcodePoint max = gcodeModel.getMax ();
            double stepWidthX = gcodeModel.getStepWidthX ();
            double stepWidthY = gcodeModel.getStepWidthY ();

            try ( BufferedWriter writer = new BufferedWriter ( new FileWriter ( file ) ); ) {

                writer.write ( "* generated " + new SimpleDateFormat ( "dd.MM.yyyy HH.mm:ss" ).format ( new Date () ) );
                writer.newLine ();
                writer.write ( "* " + file.getPath () );
                writer.newLine ();
                writer.write ( "dim=[" + matrix.length + "," + matrix[0].length + "]" );
                writer.newLine ();
                writer.write ( "min=" + min );
                writer.newLine ();
                writer.write ( "max=" + max );
                writer.newLine ();
                writer.write ( "stepWidthX=" + stepWidthX );
                writer.newLine ();
                writer.write ( "stepWidthY=" + stepWidthY );
                writer.newLine ();

                for ( int i = 0; i < matrix.length; i++ ) {
                    for ( int j = 0; j < matrix[0].length; j++ ) {
                        writer.write ( "m(" + i + "," + j + ")=" + matrix[i][j] );
                        writer.newLine ();
                    }
                }

                writer.write ( "end of data" );
                // writer.newLine ();

                eventBroker.send ( EVENT_PROBE_DATA_SAVED, file.getPath () );

            }
            catch ( IOException exc ) {
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

            LOG.debug ( "stopped" );

        }

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
    
            IGcodePoint [][] m = gcodeModel.getScanMatrix ();
    
            sendCommand ( SCAN_START );
    
            IGcodePoint probePoint = m[0][0];
            sendCommandSuppressInTerminal ( "G21" );
            sendCommandSuppressInTerminal ( "G90" );
            sendCommandSuppressInTerminal ( "G0Z" + zClearance );
            sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
            sendCommandSuppressInTerminal ( "G0Z" + zMax );
    
            for ( int i = 0; i < m.length; i++ ) {
    
                if ( skipByAlarm ) break;

                // TODO implement mäander
                for ( int j = 0; j < m[i].length; j++ ) {

                    if ( skipByAlarm ) break;
    
                    // progressListener.tick ();
    
                    probePoint = m[i][j];
    
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
    
            sendCommand ( SCAN_END );
    
            LOG.debug ( "stopped" );
    
        }
    }

    protected class GcodeLoaderThread extends Thread {


        private final static String THREAD_NAME = "gcode-file-loader";

        private File file;

        public GcodeLoaderThread ( File file ) {

            super ( THREAD_NAME + " " + file.getName () );
            this.file = file;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        private boolean isLineEmpty ( String line ) {

            boolean result = true;

            for ( int i = 0; i < line.length (); i++ ) {
                if ( line.charAt ( i ) != ' ' ) {
                    result = false;
                    break;
                }
            }

            return result;

        }

        @Override
        public void run () {

            try ( BufferedReader reader = new BufferedReader ( new FileReader ( file ) ) ) {

                gcodeModel.clear ();

                String line;
                while ( (line = reader.readLine ()) != null ) {
                    if ( !isLineEmpty ( line ) ) {
                        gcodeModel.appendGcodeLine ( line );
                    }
                }
                reader.close ();

                gcodeModel.parseGcode ();

                eventBroker.send ( EVENT_GCODE_PLAYER_LOADED, file.getPath () );

            }
            catch ( IOException exc ) { // including FileNotFoundException
                LOG.error ( "exc=" + exc );
                sendErrorMessageFromThread ( THREAD_NAME, exc );
            }

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

            eventBroker.send ( EVENT_GCODE_PLAYER_START, getTimestamp () );

            gcodeModel.resetProcessed ();

            gcodeModel.visit ( new IGcodeModelVisitor () {

                @Override
                public void visit ( IGcodeLine gcodeLine ) {

                    if ( skipByAlarm ) return;

                    LOG.debug ( THREAD_NAME + ": line=" + gcodeLine.getLine () + " | gcodeLine=" + gcodeLine );
                    eventBroker.send ( EVENT_GCODE_PLAYER_LINE, gcodeLine );

                    LOG.debug ( THREAD_NAME + ": line=" + gcodeLine.getLine () );
                    // if ( gcodeModel.isScanDataComplete () && gcodeLine.isMotionMode () ) {
                    if ( gcodeModel.isScanDataComplete () && gcodeLine.getGcodeMode () == EGcodeMode.MOTION_MODE_LINEAR ) {
                        final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                        final String feed = "F" + gcodeLine.getFeedrate ();
                        IGcodePoint [] path = gcodeModel.interpolateLine ( gcodeLine.getStart (), gcodeLine.getEnd () );
                        for ( int i = 1; i < path.length; i++ ) {
                            // TODO eliminate first point?
                            String segment = cmd;
                            segment += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getX () );
                            segment += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getY () );
                            segment += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, path[i].getZ () );
                            segment += feed;
                            eventBroker.send ( EVENT_GCODE_PLAYER_LINE_SEGMENT, segment );
                            sendCommandSuppressInTerminal ( segment );
                        }
                    }
                    else {
                        // after rotation the original line is obsolet for motion commands
                        if ( gcodeLine.isMotionMode () ) {
                            final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                            final String feed = "F" + gcodeLine.getFeedrate ();
                            String line = cmd;
                            line += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getX () );
                            line += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getY () );
                            line += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getZ () );
                            line += feed;
                            sendCommandSuppressInTerminal ( line );
                        }
                        else {
                             sendCommandSuppressInTerminal ( gcodeLine.getLine () );
                        }
                    }
                    gcodeLine.setProcessed ( true );
                }

            } );

            eventBroker.send ( EVENT_GCODE_PLAYER_STOP, getTimestamp () );
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

                        if ( cmd.line.startsWith ( SCAN_START ) ) {
                            scanRunning = true;
                            eventBroker.send ( EVENT_GCODE_SCAN_START, getTimestamp () );
                        }
                        else if ( cmd.line.startsWith ( SCAN_END ) ) {
                            scanRunning = false;
                            gcodeModel.setScanDataCompleted ();
                            eventBroker.send ( EVENT_GCODE_SCAN_STOP, getTimestamp () ); //
                        }
                        else {

                            waitForOk = true;
                            suppressInTerminal = cmd.suppressInTerminal;

                            byte [] buffer = cmd.line.getBytes ( StandardCharsets.US_ASCII );
                            eventBroker.send ( EVENT_GCODE_SENT, cmd );

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
