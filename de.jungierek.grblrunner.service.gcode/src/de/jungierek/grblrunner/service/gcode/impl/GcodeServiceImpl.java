package de.jungierek.grblrunner.service.gcode.impl;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import jakarta.inject.Inject;

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

@SuppressWarnings("restriction")
public class GcodeServiceImpl implements IGcodeService, ISerialServiceReceiver {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeServiceImpl.class );

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private ISerialService serial;

    // protected for test purposes
    protected ArrayBlockingQueue<GrblRequestImpl> queue = new ArrayBlockingQueue<> ( IConstant.GCODE_QUEUE_LENGTH, true );

    protected volatile boolean waitForOk = false;
    protected volatile boolean skipByAlarm = false;
    protected volatile boolean suppressInTerminal = false;

    private GcodeGrblStateImpl lastGrblState;
    private EGcodeMode lastMotionMode;
    private String lastCoordSelect;
    private String lastPlane;
    private String lastMetricMode;
    private String lastDistanceMode;
    private String lastTool;
    private String lastSpindleMode;
    private String lastCoolantMode;
    private String lastFeedrate;
    private String lastSpindlespeed;

    private Thread gcodePlayerThread;
    private Thread probeScannerThread;
    
    private Thread senderThread;
    private Thread statePollerThread;
    private ParserStatePollerThread parserStatePoller;

    protected volatile boolean playRunning = false;
    protected volatile boolean scanRunning = false;

    private IGcodePoint fixtureSshift = new GcodePointImpl ( 0.0, 0.0, 0.0 );

    private IGcodeProgram gcodeProgram;

    private IGcodePoint toolChangeProbePoint;

    @Inject
    public GcodeServiceImpl () {}

    // only for test
    protected GcodeServiceImpl ( IEventBroker eventBroker, ISerialService serial ) {

        this ( eventBroker, serial, null );

    }

    // only for test
    protected GcodeServiceImpl ( IEventBroker eventBroker, ISerialService serial, IGcodeProgram program ) {

        this.eventBroker = eventBroker;
        this.serial = serial;
        this.gcodeProgram = program;

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

    @Override
    public void sendJogCancel () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x85 ) ).start ();
    }

    @Override
    public void sendFeedOverride100Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x90 ) ).start ();
    }

    @Override
    public void sendFeedOverrideIncrease10Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x91 ) ).start ();
    }

    @Override
    public void sendFeedOverrideDecrease10Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x92 ) ).start ();
    }

    @Override
    public void sendFeedOverrideIncrease1Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x93 ) ).start ();
    }

    @Override
    public void sendFeedOverrideDecrease1Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x94 ) ).start ();
    }

    @Override
    public void sendSpindleOverride100Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x99 ) ).start ();
    }

    @Override
    public void sendSpindleOverrideIncrease10Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x9A ) ).start ();
    }

    @Override
    public void sendSpindleOverrideDecrease10Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x9B ) ).start ();
    }

    @Override
    public void sendSpindleOverrideIncrease1Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x9C ) ).start ();
    }

    @Override
    public void sendSpindleOverrideDecrease1Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x9D ) ).start ();
    }

    @Override
    public void sendToggleSpindleStop () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x9E ) ).start ();
    }

    @Override
    public void sendRapidOverride100Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x95 ) ).start ();
    }

    @Override
    public void sendRapidOverride50Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x96 ) ).start ();
    }

    @Override
    public void sendRapidOverride25Percent () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0x97 ) ).start ();
    }

    @Override
    public void sendToggleFloodCoolant () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0xA0 ) ).start ();
    }

    @Override
    public void sendToggleMistCoolant () {
        new Thread ( () -> sendSingleSignCommand ( (char) 0xA1 ) ).start ();
    }

    private void sendSingleSignCommand ( char c ) {

        LOG.trace ( "sendSingleSignCommand: c=" + c );

        // send Command direct, bypassing queue
        // only for '?', '!', '~'
        serial.send ( c );

    }

    @Override
    public void sendCommand ( String line ) throws InterruptedException {
        
        sendCommand ( line, false );
    }

    @Override
    public void sendCommandSuppressInTerminal ( String line ) throws InterruptedException {

        sendCommand ( line, true );

    }

    private void sendCommand ( String line, boolean suppressInTerminal ) throws InterruptedException {

        try {
            queue.put ( new GrblRequestImpl ( suppressInTerminal, line + IConstant.LF ) );
        }
        catch ( InterruptedException exc ) {
            LOG.info ( "sendCommand: interrupted exception in line=" + line );
            // sendErrorMessage ( "sending gcode command interrupted!\n" + line, exc );
            throw exc;
        }

    }

    @Override
    public void received ( String line ) {

        LOG.trace ( "received: suppressInTerminal=" + suppressInTerminal + " line=" + line );

        boolean releaseWaitForOk = false;
        boolean suppressLine = suppressInTerminal;

        if ( line.startsWith ( "<" ) ) { // response on state request '?'
            releaseWaitForOk = false;
            skipByAlarm = false; // this is also reset, when <Alarm is received!
            suppressLine = true; // suppress ever
        }
        else if ( line.startsWith ( "ok" ) ) {
            releaseWaitForOk = true;
            skipByAlarm = false;
        }
        else if ( line.startsWith ( "error" ) ) {
            releaseWaitForOk = true;
            skipByAlarm = false;
            if ( gcodePlayerThread != null ) gcodePlayerThread.interrupt ();
            if ( probeScannerThread != null ) probeScannerThread.interrupt ();
            queue.clear (); // empty queue
        }
        else if ( line.startsWith ( "Grbl" ) || line.startsWith ( "[MSG:" ) ) {
            // queue.clear (); // empty queue
            // releaseWaitForOk = true;
            suppressLine = false;
        }
        else if ( line.startsWith ( "ALARM" ) ) {
            releaseWaitForOk = true;
            skipByAlarm = true;
            suppressLine = false; // show this line ever
            if ( gcodePlayerThread != null ) gcodePlayerThread.interrupt ();
            if ( probeScannerThread != null ) probeScannerThread.interrupt ();
            queue.clear (); // empty queue
        }

        // erst event senden, dann ...
        String analysedLine = analyseResponse ( line );
        eventBroker.post ( IEvent.GRBL_RECEIVED, new GrblResponseImpl ( suppressLine, analysedLine ) );

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

    private int [] parseIntVector ( String line, int vectorLength, String intro, char closingChar ) {

        int [] value = new int [vectorLength];

        int startPos = line.indexOf ( intro ) + intro.length ();
        int endPos = -1;

        for ( int i = 0; i < vectorLength; i++ ) {
            endPos = line.indexOf ( (i < vectorLength - 1 ? "," : "" + closingChar), startPos );
            value [i] = parseInt ( 99999, line.substring ( startPos, endPos ) );
            startPos = endPos + 1;
        }

        return value;

    }

    private double parseDouble ( double defaultValue, String s ) {

        double result = defaultValue;
        try {
            result = Double.parseDouble ( s );
        }
        catch ( NumberFormatException exc ) {}

        return result;

    }

    private int parseInt ( int defaultValue, String s ) {

        int result = defaultValue;
        try {
            result = Integer.parseInt ( s );
        }
        catch ( NumberFormatException exc ) {}

        return result;

    }

    private static final String SETTING_COLUMN_EMPTY = "                    ";
    
    // see https://github.com/gnea/grbl/wiki/Grbl-v1.1-Interface
    private String analyseResponse ( String line ) {

        if ( line.startsWith ( "ALARM:" ) ) { // alarm message
            
            int index = parseInt ( 0, line.substring ( "ALARM:".length (), line.length () - 2 ) ); // cut the crlf at the end
            final String [] msg = ALARM_CODE_DESCRIPTION [index];
            for ( String s : msg ) {
                line = line + s + "\r\n";
            }
            eventBroker.post ( IEvent.GRBL_ALARM, msg ); // inform about alarm message

            if ( scanRunning ) {
                gcodeProgram.setAutolevelStop ();
                scanRunning = false;
            }

            if ( playRunning ) {
                gcodeProgram.setPlayerStop ();
                playRunning = false;
                parserStatePoller.restart ();
            }

        }
        else if ( line.startsWith ( "error:" ) ) { // error message

            int index = parseInt ( 0, line.substring ( "error:".length (), line.length () - 2 ) ); // cut the crlf at the end
            final String msg = ERROR_CODE_DESCRIPTION [index];
            line = line + msg + "\r\n";
            if ( !suppressInTerminal ) {
                eventBroker.post ( IEvent.MESSAGE_ERROR, msg ); // inform about error message
            }

        }
        else if ( line.startsWith ( "Grbl" ) ) { // welcome message

            eventBroker.post ( IEvent.GRBL_RESTARTED, line ); // inform about grbl restart

        }
        else if ( line.startsWith ( ">" ) ) {} // startup line execution >G54G20:ok
        else if ( line.startsWith ( "<" ) ) { // grbl state message

            String linePiped = line.replace ( '>', '|' );

            EGrblState state = EGrblState.identify ( linePiped );

            GcodePointImpl m = parseCoordinates ( linePiped, "MPos:", '|' );
            if ( linePiped.indexOf ( "WCO:" ) > 0 ) {
                GcodePointImpl wco = parseCoordinates ( linePiped, "WCO:", '|' );
                // update only on change
                if ( !fixtureSshift.equals ( wco ) ) {
                    fixtureSshift = wco;
                    LOG.trace ( "analyseResponse: wco=" + wco );
                    eventBroker.post ( IEvent.UPDATE_FIXTURE_OFFSET, wco );
                }
            }
            GcodePointImpl w = (GcodePointImpl) m.sub ( fixtureSshift );
            GcodeGrblStateImpl grblState = new GcodeGrblStateImpl ( state, m, w );
            
            if ( linePiped.indexOf ( "Bf:" ) > 0 ) {
                int [] n = parseIntVector ( linePiped, 2, "Bf:", '|' );
                grblState.setAvailablePlannerBufferSize ( n [0] ); // planner buffer size
                grblState.setAvailableRxBufferSize ( n [1] ); // rx buffer size
            }

            if ( linePiped.indexOf ( "FS:" ) > 0 ) {
                int [] n = parseIntVector ( linePiped, 2, "FS:", '|' );
                grblState.setFeedRate ( n [0] );
                grblState.setSpindleSpeed ( n [1] );
            }
            else if ( linePiped.indexOf ( "F:" ) > 0 ) {
                int [] n = parseIntVector ( linePiped, 1, "F:", '|' );
                grblState.setFeedRate ( n [0] );
            }
            
            if ( linePiped.indexOf ( "Ov:" ) > 0 ) {
                int [] n = parseIntVector ( linePiped, 3, "Ov:", '|' );
                grblState.setFeedOverride ( n [0] );
                grblState.setRapidOverride ( n [1] );
                grblState.setSpindleOverride ( n [2] );
            }

            String pinState = "no pin";
            if ( linePiped.indexOf ( "Pn:" ) > 0 ) {
                final int pos = linePiped.indexOf ( "Pn:" ) + 3;
                pinState = linePiped.substring ( pos, linePiped.indexOf ( '|', pos ) );
            }
            grblState.setPinState ( pinState );

            // update only on change
            if ( lastGrblState == null || !lastGrblState.equals ( grblState ) ) {
                lastGrblState = grblState;
                eventBroker.post ( IEvent.UPDATE_STATE, grblState );
            }
            
        }
        else if ( line.startsWith ( "[MSG:Pgm End]" ) ) {
            if ( scanRunning ) {
                gcodeProgram.setAutolevelStop ();
                gcodeProgram.setAutolevelScanCompleted ();
                eventBroker.post ( IEvent.AUTOLEVEL_STOP, getTimestamp () );
                scanRunning = false;
            }
            else if ( playRunning ) {
                gcodeProgram.setPlayerStop ();
                eventBroker.post ( IEvent.PLAYER_STOP, getTimestamp () );
                playRunning = false;
                parserStatePoller.restart ();
            }

        }
        else if ( line.startsWith ( "[PRB:" ) ) {
            // probe sends all cooridnates in machine system, transfer probe coordinates from machine to working coordinates
            IGcodePoint probePoint = parseCoordinates ( line, "[PRB:", ':' ).sub ( fixtureSshift );
            char success = line.charAt ( "[PRB:".length () + line.substring ( "[PRB:".length () ).indexOf ( ':' ) + 1 );
            LOG.trace ( "analyseResponse: probe detected scanRunning=" + scanRunning + " line=" + line + " probe=" + probePoint + " success=" + success );
            if ( success == '1' ) {
                if ( scanRunning ) gcodeProgram.setProbePoint ( probePoint );
                eventBroker.post ( IEvent.AUTOLEVEL_UPDATE, probePoint );
            }
            toolChangeProbePoint = probePoint;
        }
        else if ( line.startsWith ( "[G54" ) ) {} // do nothing
        else if ( line.startsWith ( "[G55" ) ) {} // do nothing
        else if ( line.startsWith ( "[G56" ) ) {} // do nothing
        else if ( line.startsWith ( "[G57" ) ) {} // do nothing
        else if ( line.startsWith ( "[G58" ) ) {} // do nothing
        else if ( line.startsWith ( "[G59" ) ) {} // do nothing
        else if ( line.startsWith ( "[G28" ) ) {} // do nothing
        else if ( line.startsWith ( "[G30" ) ) {} // do nothing
        else if ( line.startsWith ( "[G92" ) ) {} // do nothing
        else if ( line.startsWith ( "[GC:" ) ) {

            // v0.9j -> [G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]
            // v1.1f -> [GC:G0 G54 G17 G21 G90 G94 M5 M9 T0 F0 S0]
            int startPos = "[GC:".length (); // without '[GC:'
            int endPos = line.indexOf ( ']' );
            String [] token = line.substring ( startPos, endPos ).split ( "\\s" ); // split with token space

            // token [0] -> motion mode
            EGcodeMode motionMode = EGcodeMode.identify ( token[0] );
            if ( lastMotionMode != motionMode && motionMode != EGcodeMode.GCODE_MODE_UNDEF ) {
                lastMotionMode = motionMode;
                eventBroker.post ( IEvent.UPDATE_MODAL_MODE, motionMode.getCommand () );
            }

            // token [1] -> G54 .. G59
            String coordSelect = token[1];
            if ( lastCoordSelect != coordSelect && !coordSelect.equals ( lastCoordSelect ) ) {
                lastCoordSelect = coordSelect;
                eventBroker.post ( IEvent.UPDATE_FIXTURE, coordSelect );
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
                eventBroker.post ( IEvent.UPDATE_PLANE, plane );
            }

            // token [3] -> inch or mm
            String metricMode = token[3].equals ( "G20" ) ? "inch" : "mm";
            if ( !metricMode.equals ( lastMetricMode ) ) {
                lastMetricMode = metricMode;
                eventBroker.post ( IEvent.UPDATE_METRIC_MODE, metricMode );
            }

            // token [4] absolute or relative
            String distanceMode = token[4].equals ( "G90" ) ? "absolute" : "relative";
            if ( !distanceMode.equals ( lastDistanceMode ) ) {
                lastDistanceMode = distanceMode;
                eventBroker.post ( IEvent.UPDATE_DISTANCE_MODE, distanceMode );
            }

            // token [5] path control mode G93 inverse time mode G94 units per minute mode G95 units per revision mode

            // token [6] spindle mode M3 CW rot M4 CCW rot M5 stop
            String spindleMode = "unknown";
            switch ( token[6] ) {
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
                eventBroker.post ( IEvent.UPDATE_SPINDLE_MODE, spindleMode );
            }

            // token [7] coolant mode M7 mist coolant M8 flood coolant M9 stop coolant
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
                eventBroker.post ( IEvent.UPDATE_COOLANT_MODE, coolantMode );
            }

            // token [8] Tn tool number
            String tool = token[8];
            if ( !tool.equals ( lastTool ) ) {
                lastTool = tool;
                eventBroker.post ( IEvent.UPDATE_TOOL, tool );
            }

            // token [9] -> feedrate
            String feedrate = token[9].substring ( 1 ); // ignore 'F'
            if ( feedrate.endsWith ( "." ) ) feedrate = feedrate.substring ( 0, feedrate.length () - 1 );
            if ( !feedrate.equals ( lastFeedrate ) ) {
                lastFeedrate = feedrate;
                eventBroker.post ( IEvent.UPDATE_FEEDRATE, feedrate );
            }

            // token [10] -> spindle speed
            String spindlespeed = token[10].substring ( 1 ); // ignore 'S'
            if ( spindlespeed.endsWith ( "." ) ) spindlespeed = spindlespeed.substring ( 0, spindlespeed.length () - 1 );
            if ( !spindlespeed.equals ( lastSpindlespeed ) ) {
                lastSpindlespeed = spindlespeed;
                eventBroker.post ( IEvent.UPDATE_SPINDLESPEED, spindlespeed );
            }

        }
        else if ( line.startsWith ( "$0=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Step pulse time, microseconds)\r\n";
        }
        else if ( line.startsWith ( "$1=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Step idle delay, milliseconds)\r\n";
        }
        else if ( line.startsWith ( "$2=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Step pulse invert, mask)\r\n";
        }
        else if ( line.startsWith ( "$3=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Step direction invert, mask)\r\n";
        }
        else if ( line.startsWith ( "$4=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Invert step enable pin, boolean)\r\n";
        }
        else if ( line.startsWith ( "$5=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Invert limit pins, boolean)\r\n";
        }
        else if ( line.startsWith ( "$6=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Invert probe pin, boolean)\r\n";
        }
        else if ( line.startsWith ( "$10=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Status report options, mask)\r\n";
        }
        else if ( line.startsWith ( "$11=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Junction deviation, millimeters)\r\n";
        }
        else if ( line.startsWith ( "$12=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Arc tolerance, millimeters)\r\n";
        }
        else if ( line.startsWith ( "$13=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Report in inches, boolean)\r\n";
        }
        else if ( line.startsWith ( "$20=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Soft limits enable, boolean)\r\n";
        }
        else if ( line.startsWith ( "$21=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Hard limits enable, boolean)\r\n";
        }
        else if ( line.startsWith ( "$22=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Homing cycle enable, boolean)\r\n";
        }
        else if ( line.startsWith ( "$23=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Homing direction invert, mask)\r\n";
        }
        else if ( line.startsWith ( "$24=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Homing locate feed rate, mm/min)\r\n";
        }
        else if ( line.startsWith ( "$25=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Homing search seek rate, mm/min)\r\n";
        }
        else if ( line.startsWith ( "$26=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Homing switch debounce delay, milliseconds)\r\n";
        }
        else if ( line.startsWith ( "$27=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Homing switch pull-off distance, millimeters)\r\n";
        }
        else if ( line.startsWith ( "$30=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Maximum spindle speed, RPM)\r\n";
        }
        else if ( line.startsWith ( "$31=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Minimum spindle speed, RPM)\r\n";
        }
        else if ( line.startsWith ( "$32=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Laser-mode enable, boolean)\r\n";
        }
        else if ( line.startsWith ( "$100=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(X-axis steps per millimeter)\r\n";
        }
        else if ( line.startsWith ( "$101=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Y-axis steps per millimeter)\r\n";
        }
        else if ( line.startsWith ( "$102=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Z-axis steps per millimeter)\r\n";
        }
        else if ( line.startsWith ( "$110=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(X-axis maximum rate, mm/min)\r\n";
        }
        else if ( line.startsWith ( "$111=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Y-axis maximum rate, mm/min)\r\n";
        }
        else if ( line.startsWith ( "$112=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Z-axis maximum rate, mm/min)\r\n";
        }
        else if ( line.startsWith ( "$120=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(X-axis acceleration, mm/sec^2)\r\n";
        }
        else if ( line.startsWith ( "$121=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Y-axis acceleration, mm/sec^2)\r\n";
        }
        else if ( line.startsWith ( "$122=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Z-axis acceleration, mm/sec^2)\r\n";
        }
        else if ( line.startsWith ( "$130=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(X-axis maximum travel, millimeters)\r\n";
        }
        else if ( line.startsWith ( "$131=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Y-axis maximum travel, millimeters)\r\n";
        }
        else if ( line.startsWith ( "$132=" ) ) {
            line = line.substring ( 0, line.length () - 2 ) + SETTING_COLUMN_EMPTY.substring ( line.length () ) + "(Z-axis maximum travel, millimeters)\r\n";
        }
        
        return line;

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
    public String getMetricMode () {

        return lastMetricMode;

    }

    @Override
    public String getDistanceMode () {

        return lastDistanceMode;

    }

    @Override
    public String getFixture () {

        return lastCoordSelect;

    }

    @Override
    public void playGcodeProgram ( IGcodeProgram program, double probeDepth, double probeFeedRate, boolean probeWithError ) {

        if ( isPlaying () || isAutolevelScan () || !isGrblIdle () ) return;

        if ( gcodeProgram != null ) {
            // reset "old" program
            gcodeProgram.resetProcessed ();
        }

        gcodeProgram = program;
        gcodeProgram.resetProcessed ();
        eventBroker.post ( IEvent.REDRAW, "dummy" );

        // decouple from UI thread
        gcodePlayerThread = new GcodePlayerThread ( probeDepth, probeFeedRate, probeWithError );
        gcodePlayerThread.start ();

    }

    @Override
    public void scanAutolevelData ( IGcodeProgram program, double zMin, double zMax, double zClearance, double probeFeedrate, boolean withError ) {

        if ( isPlaying () || isAutolevelScan () || !isGrblIdle () ) return;

        gcodeProgram = program;

        if ( IConstant.AUTOLEVEL_USE_RANDOM_Z_SIMULATION ) {
            
            gcodeProgram.prepareAutolevelScan ();

            new Thread ( ( ) -> {

                LOG.warn ( "scan: randomZSimulation" );

                eventBroker.post ( IEvent.AUTOLEVEL_START, getTimestamp () );

                scanRunning = true;

                final int xlength = gcodeProgram.getXSteps () + 1;
                final int ylength = gcodeProgram.getYSteps () + 1;

                for ( int i = 0; i < xlength; i++ ) {
                    double z = 0.0;
                    if ( IConstant.AUTOLEVEL_UNIFORM_HEIGHT_AT_Y_AXIS ) {
                        z = IConstant.AUTOLEVEL_Z_SIMULATION_SCALE_FACTOR * Math.random ();
                    }
                    for ( int j = 0; j < ylength; j++ ) {
                        if ( IConstant.AUTOLEVEL_SLOW_Z_SIMULATION ) {
                            try {
                                Thread.sleep ( 100 );
                            }
                            catch ( InterruptedException exc ) {}
                        }
                        if ( !IConstant.AUTOLEVEL_UNIFORM_HEIGHT_AT_Y_AXIS ) {
                            z = IConstant.AUTOLEVEL_Z_SIMULATION_SCALE_FACTOR * Math.random ();
                        }
                        IGcodePoint probe = gcodeProgram.getProbePointAt ( i, j ).addAxis ( 'Z', z );
                        gcodeProgram.setProbePoint ( probe );
                        LOG.debug ( "scan: probe=" + probe );
                        eventBroker.post ( IEvent.AUTOLEVEL_UPDATE, probe );
                    }
                }
                scanRunning = false;
                gcodeProgram.setAutolevelScanCompleted ();

                eventBroker.post ( IEvent.AUTOLEVEL_STOP, getTimestamp () );

            } ).start ();

        }
        else {

            // decouple from UI thread
            probeScannerThread = new ProbeScannerThread ( zMin, zMax, zClearance, probeFeedrate, withError );
            probeScannerThread.start ();

        }

    }

    @Override
    public boolean isGrblIdle () {

        return lastGrblState == null || lastGrblState.getGrblState () == EGrblState.IDLE;

    }

    @Override
    public boolean isGrblAlarm () {

        return lastGrblState == null || lastGrblState.getGrblState () == EGrblState.ALARM;

    }

    @Override
    public boolean isGrblJog () {

        return lastGrblState == null || lastGrblState.getGrblState () == EGrblState.JOG;

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

        lastGrblState = null;
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

    @SuppressWarnings("unused")
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

        eventBroker.post ( IEvent.MESSAGE_ERROR, "" + sb );

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
    
        @Override
        public void run () {
    
            gcodeProgram.setAutolevelStart ();
            scanRunning = true;
            eventBroker.post ( IEvent.AUTOLEVEL_START, getTimestamp () );

            final int xlength = gcodeProgram.getXSteps () + 1;
            final int ylength = gcodeProgram.getYSteps () + 1;
    
            IGcodePoint probePoint = gcodeProgram.getProbePointAt ( 0, 0 );

            try {
                sendCommandSuppressInTerminal ( "G21" );
                sendCommandSuppressInTerminal ( "G90" );
                sendCommandSuppressInTerminal ( "G0Z" + zClearance );
                sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
                sendCommandSuppressInTerminal ( "G0Z" + zMax );

                LOOP: for ( int i = 0; i < xlength; i++ ) {

                    for ( int j = 0; j < ylength; j++ ) {

                        if ( isInterrupted () ) break LOOP;

                        int d = i % 2;
                        int jj = d * (ylength - j - 1) + (1 - d) * j;

                        probePoint = gcodeProgram.getProbePointAt ( i, jj );
    
                        sendCommandSuppressInTerminal ( "G0X" + probePoint.getX () + "Y" + probePoint.getY () );
                        final String line = "G90G38." + (withError ? "2" : "3") + "Z" + zMin + "F" + probeFeedrate;
                        sendCommandSuppressInTerminal ( line );
                        sendCommandSuppressInTerminal ( "G0Z" + zMax );

                    }

                }

                sendCommandSuppressInTerminal ( "M2" ); // program ends

            }
            catch ( InterruptedException exc ) {
                LOG.info ( THREAD_NAME + ": cancelled" );
                interrupt ();
            }

            // all state changes on scan completion are made in analyzeReponse () when "[MSG:Pgm End]" (from M2) ist detected

            probeScannerThread = null;
    
            LOG.info ( THREAD_NAME + ": stopped" );
    
        }
    }

    protected class GcodePlayerThread extends Thread {

        private final static String THREAD_NAME = "gcode-player";

        double probeDepth;
        double probeFeedrate;
        boolean probeWithError;

        public GcodePlayerThread ( double probeDepth, double probeFeedRate, boolean probeWithError ) {

            super ( THREAD_NAME );

            this.probeDepth = probeDepth;
            this.probeFeedrate = probeFeedRate;
            this.probeWithError = probeWithError;

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": starting" );
            super.start ();

        }

        @Override
        public void run () {

            parserStatePoller.pause ();

            playRunning = true;

            gcodeProgram.setPlayerStart ();
            eventBroker.post ( IEvent.PLAYER_START, getTimestamp () );

            boolean firstMove = true;
            IGcodePoint lastPoint = null;

            IGcodeLine [] allGcodeLines = gcodeProgram.getAllGcodeLines ();
            for ( IGcodeLine gcodeLine : allGcodeLines ) {

                if ( isInterrupted () ) {
                    LOG.debug ( THREAD_NAME + ": loop break" );
                    break;
                }
                else {
                    
                    try {

                        LOG.trace ( THREAD_NAME + ": line=" + gcodeLine.getLine () + " | gcodeLine=" + gcodeLine );
                        gcodeLine.setProcessed ( true );
                        eventBroker.post ( IEvent.PLAYER_LINE, gcodeLine );
    
                        if ( gcodeProgram.isAutolevelScanComplete () && gcodeLine.isMotionModeLinear () ) {
                            if ( gcodeLine.isMoveInXYZ () ) {
                                final String cmd = gcodeLine.getGcodeMode ().getCommand ();
                                final String feed = "F" + gcodeLine.getFeedrate ();
                                IGcodePoint [] path = gcodeLine.getAutoevelSegmentPath ();
                                for ( int i = 1; i < path.length; i++ ) {
                                    // Attention: eliminate first point in path with index 0, because G0 lines has'nt autoleveled.
                                    // So the last end point (autoleveled or not) is the start point of the next move
                                    String segment = cmd;
                                    segment += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, path [i].getX () );
                                    segment += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, path [i].getY () );
                                    segment += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, path [i].getZ () );
                                    segment += feed;
                                    eventBroker.post ( IEvent.PLAYER_SEGMENT, segment );
                                    sendCommandSuppressInTerminal ( segment );
                                    LOG.trace ( "  segment=" + segment );
                                }
                                lastPoint = path [path.length - 1];
                            }
                        }
                        else if ( gcodeLine.isMotionMode () ) {
                            // after rotation the original line is obsolet for sending as motion commands, generate it
                            String line = "";
                            if ( gcodeLine.isMoveInXYZ () ) {
                                line += gcodeLine.getGcodeMode ().getCommand ();
                                if ( gcodeLine.isMoveInX () || firstMove ) line += "X" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getX () );
                                if ( gcodeLine.isMoveInY () || firstMove ) line += "Y" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getY () );
                                if ( gcodeLine.isMoveInZ () || firstMove ) line += "Z" + String.format ( IGcodePoint.FORMAT_COORDINATE, gcodeLine.getEnd ().getZ () );
                                firstMove = false;
                                lastPoint = gcodeLine.getEnd ();
                            }

                            if ( gcodeLine.isMotionModeArc () ) line += "R" + gcodeLine.getRadius ();
                            if ( gcodeLine.isMotionModeLinear () || gcodeLine.isMotionModeArc () ) line += "F" + gcodeLine.getFeedrate ();
                            sendCommandSuppressInTerminal ( line );
                            LOG.trace ( "line=" + line );
                        }
                        else if ( gcodeLine.getGcodeMode () == EGcodeMode.TOOL_CHANGE ) {
                            sendCommandSuppressInTerminal ( "G49" ); // reset TLO
                            eventBroker.post ( IEvent.MESSAGE_ON_HALT, "Attach probe wires" );
//                          sendCommandSuppressInTerminal ( "G30Z" + lastPoint.getZ () ); // move first on z axis
                            sendCommandSuppressInTerminal ( "G30" ); // go to tool change position
                            sendCommandSuppressInTerminal ( "M0" ); // pause and display attach message
                            sendCommandSuppressInTerminal ( "G0X0Y0" ); // go to [0,0]
                            toolChangeProbePoint = null;
                            sendCommandSuppressInTerminal ( "G38." + (probeWithError ? "2" : "3") + "Z" + probeDepth + "F" + probeFeedrate );
                            while ( toolChangeProbePoint == null ) {
                                sleep ( IConstant.GCODE_PLAYER_PROBE_SLEEP_MS );
                            }
                            if ( toolChangeProbePoint != null ) {
                                LOG.trace ( "set TLO to z=" + toolChangeProbePoint.getZ () );
                                sendCommandSuppressInTerminal ( "G43.1Z" + toolChangeProbePoint.getZ () ); // set TLO with last probe z
                            }
                            sendCommandSuppressInTerminal ( "G0Z" + lastPoint.getZ () ); // go to last z
                            sendCommandSuppressInTerminal ( "G0X" + lastPoint.getX () + "Y" + lastPoint.getY () ); // go to last [x,y]
                            eventBroker.post ( IEvent.MESSAGE_ON_HALT, "Remove probe wires" ); // message for next pause
                        }
                        else {
                            sendCommandSuppressInTerminal ( gcodeLine.getLine () );
                            // if ( gcodeLine.getGcodeMode () == EGcodeMode.TOOL ) {
                            // sendCommandSuppressInTerminal ( "$G" ); // refresh tool state in gui
                            // }
                        }

                        // refresh tool state in gui after each send line
                        // TODO make it more sensitive
                        sendCommandSuppressInTerminal ( "$G" );
                        
                    }
                    catch ( InterruptedException exc ) {
                        LOG.info ( THREAD_NAME + ": cancelled" );
                        interrupt ();
                    }

                }

            }

            gcodePlayerThread = null;

            LOG.info ( THREAD_NAME + ": stopped" );

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
                        sleep ( IConstant.GCODE_SENDER_WAITOK_SLEEP_MS );
                    }

                    GrblRequestImpl cmd = queue.take ();
                    if ( !skipByAlarm || cmd.isReset () || cmd.isHome () || cmd.isUnlock () ) {

                        waitForOk = true;
                        suppressInTerminal = cmd.suppressInTerminal;

                        byte [] buffer = cmd.message.getBytes ( StandardCharsets.US_ASCII );
                        eventBroker.post ( IEvent.GRBL_SENT, cmd );

                        serial.send ( buffer );

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
                    sleep ( IConstant.GRBL_STATE_POLLER_SLEEP_MS );
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

        private volatile boolean paused = false;
        private final Object pauseLock = new Object ();

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
                synchronized ( pauseLock ) {
                    try {
                        if ( paused ) {
                            pauseLock.wait ();
                        }
                        sendCommandSuppressInTerminal ( "$G" );
                        sleep ( IConstant.PARSER_STATE_POLLER_SLEEP_MS );
                    }
                    catch ( InterruptedException exc ) {
                        interrupt ();
                    }
                }
                // TODO catch throwable and restart (if necesarry a new thread (robust implementation)
            }

            LOG.debug ( THREAD_NAME + ": stopped" );

        }

        public void pause () {

            paused = true;

        }

        public void restart () {

            synchronized ( pauseLock ) {

                paused = false;
                pauseLock.notifyAll (); // Unblocks thread

            }
        }
    }

}
