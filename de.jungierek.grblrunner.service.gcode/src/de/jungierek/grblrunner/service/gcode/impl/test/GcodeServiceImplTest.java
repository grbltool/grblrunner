package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.service.event.EventHandler;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGrblRequest;
import de.jungierek.grblrunner.service.gcode.IGrblResponse;
import de.jungierek.grblrunner.service.gcode.impl.GcodeGrblStateImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodeServiceImpl;
import de.jungierek.grblrunner.service.gcode.impl.GrblRequestImpl;
import de.jungierek.grblrunner.service.gcode.impl.GrblResponseImpl;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.service.serial.ISerialServiceReceiver;

public class GcodeServiceImplTest implements UncaughtExceptionHandler {

    // for test cast it explicite down
    private GcodeServiceImpl underTest;

    private SerialServiceMock serialServiceMock;
    private EventBrokerMock eventBrokerMock;
    private GcodeProgramMock gcodeProgramMock;

    private volatile Thread uncaughtExceptionThread;
    private volatile Throwable uncaughtException;
    
    private class GrblRequestTestee extends GrblRequestImpl {

        public GrblRequestTestee ( boolean suppressInLine, String message ) {
            super ( suppressInLine, message );
        }

    }

    private class GrblResponseTestee extends GrblResponseImpl {

        public GrblResponseTestee ( boolean suppressInLine, String message ) {
            super ( suppressInLine, message );
        }

    }

    private class GcodeServiceImplTestee extends GcodeServiceImpl {
        

        public final GcodeSenderThread gcodeSenderThread;

        private GcodeServiceImplTestee ( EventBrokerMock eventBrocker, SerialServiceMock serialService, GcodeProgramMock program ) {
            
            super ( eventBrocker, serialService, program );
            gcodeSenderThread = new GcodeSenderThread ();
            
        }
        
        public int getQueueLength () {
            return queue.size ();
        }

        public GrblRequestImpl [] getQueueElements () {
            return queue.toArray ( new GrblRequestImpl [0] );
        }

        public boolean isWaitForOk () {
            return waitForOk;
        }

        public void setWaitForOk ( boolean f ) {
            waitForOk = f;
        }

        public boolean isSkipByAlarm () {
            return skipByAlarm;
        }

        public void setSkipByAlarm ( boolean f ) {
            skipByAlarm = f;
        }

        public boolean isSuppressInTermminal () {
            return suppressInTerminal;
        }

        public boolean isScanRunning () {
            return scanRunning;
        }

        public void setScanRunning ( boolean f ) {
            scanRunning = f;
        }

        public void appendCommand ( boolean suppressInTerminal, String command ) {
            queue.add ( new GrblRequestTestee ( suppressInTerminal, command ) );
        }

    }
    
    private class SerialServiceMock implements ISerialService {
        
        private String explaination;
        private String expectedCommand;

        public ISerialServiceReceiver receiver;

        private void checkCommand ( String cmd ) {

            String _explaination = explaination;
            explaination = null;

            if ( _explaination == null ) _explaination = "---";
            if ( expectedCommand == null ) fail ( "nothing expected for cmd=" + cmd );
            assertEquals ( _explaination, expectedCommand, cmd );

        }

        public void setExpectedCommand ( String explaination, String expectedCommand ) {

            this.explaination = explaination;
            this.expectedCommand = expectedCommand;

        }

        @Override
        public void detectSerialPortsAsync () {
            fail ( "list serial Port async not implemented" );
        }

        @Override
        public String [] getCachedSerialPorts () {
            fail ( "list cached serial Port not implemented" );
            return null;
        }

        @Override
        public void setPortName ( String portName ) {
            fail ( "setPortName serial Port not implemented" );
        }

        @Override
        public String getPortName () {
            fail ( "getPortName serial Port not implemented" );
            return null;
        }

        @Override
        public void setBaudrate ( int baudrate ) {
            fail ( "setBaudrate serial Port not implemented" );
        }

        @Override
        public int getBaudrate () {
            fail ( "getBaudrate serial Port not implemented" );
            return 0;
        }

        @Override
        public void connect () {
            fail ( "connect not implemented" );
        }

        @Override
        public void close () {
            fail ( "close not implemented" );
        }

        @Override
        public boolean isOpen () {
            fail ( "isOpen Port not implemented" );
            return false;
        }

        @Override
        public void send ( char c ) {

            checkCommand ( "" + c );

        }

        @Override
        public void send ( byte [] bytes ) {

            checkCommand ( new String ( bytes ) );

        }

        @Override
        public void setReceiver ( ISerialServiceReceiver receiver ) {
            this.receiver = receiver;
        }

        @Override
        public boolean isDetectingSerialPorts () {
            // TODO Auto-generated method stub
            return false;
        }

    }

    private class EventBrokerMock implements IEventBroker {

        private boolean receivedEventExpected;
        private String expectedTopic;
        
        private IGrblRequest expectedRequest;
        private IGrblResponse expectedResponse;
        private IGcodeGrblState expectedGcodeState;
        private String expectedString;
        private GcodePointImpl expectedPoint;

        private boolean sendCalled;
        private boolean ignoreSendDetails;

        public void setExpectedReceivedEvent ( boolean receivedExpected ) {

            receivedEventExpected = receivedExpected;

        }

        public void setExpectedTopic ( String topic ) {

            expectedTopic = topic;

        }

        public void setExpectedRequest ( boolean expectedSuppressInTerminal, String expectedMessage ) {

            expectedRequest = new GrblRequestTestee ( expectedSuppressInTerminal, expectedMessage );

        }

        public void setExpectedResponse ( boolean expectedSuppressInTerminal, String expectedMessage ) {

            expectedResponse = new GrblResponseTestee ( expectedSuppressInTerminal, expectedMessage );

        }

        public void setExpectedGrblState ( EGrblState state, GcodePointImpl m, GcodePointImpl w ) {

            expectedGcodeState = new GcodeGrblStateImpl ( state, m, w );

        }

        public void setExpectedString ( String s ) {

            expectedString = s;

        }

        public void setExpectedPoint ( GcodePointImpl p ) {

            expectedPoint = p;

        }

        public boolean isSendCalled () {

            return sendCalled;
            
        }
        
        public void resetSendCalled () {

            sendCalled = false;

        }

        public void setIgnoreSendDetails ( boolean f ) {

            ignoreSendDetails = f;

        }

        @Override
        public boolean send ( String topic, Object data ) {

            // sendCalled = true;

            if ( ignoreSendDetails ) return true;

            if ( IEvent.GRBL_RECEIVED.equals ( topic ) && receivedEventExpected ) {
                receivedEventExpected = false;
            }
            else {
                assertEquals ( "topic", expectedTopic, topic );
                expectedTopic = null;
            }

            sendCalled = true;

            switch ( topic ) {

                case IEvent.GRBL_SENT:
                    if ( !(data instanceof IGrblRequest) ) fail ( "data not IGrblRequest " + data.getClass () );
                    IGrblRequest request = (IGrblRequest) data;
                    if ( expectedRequest == null ) fail ( "nothing expected for request=" + request );
                    // TODO_TEST change to equals after implementatioj in class GcodeResponseImpl
                    assertEquals ( "sent suppressInTerminal", expectedRequest.isSuppressInTerminal (), request.isSuppressInTerminal () );
                    assertEquals ( "sent line", expectedRequest.getMessage (), request.getMessage () );
                    expectedRequest = null;
                    break;

                case IEvent.GRBL_RECEIVED:
                    if ( !(data instanceof IGrblResponse) ) fail ( "data not IGrblResponse " + data.getClass () );
                    IGrblResponse response = (IGrblResponse) data;
                    if ( expectedResponse == null ) fail ( "nothing expected for response=" + response );
                    // TODO_TEST change to equals after implementatioj in class GcodeResponseImpl
                    assertEquals ( "received suppressInTerminal", expectedResponse.isSuppressInTerminal (), response.isSuppressInTerminal () );
                    assertEquals ( "received line", expectedResponse.getMessage (), response.getMessage () );
                    expectedResponse = null;
                    break;

                case IEvent.UPDATE_STATE:
                    if ( !(data instanceof IGcodeGrblState) ) fail ( "data not IGcodeGrblState " + data.getClass () );
                    IGcodeGrblState state = (IGcodeGrblState) data;
                    if ( expectedGcodeState == null ) fail ( "nothing expected for state=" + state );
                    assertEquals ( "update state", expectedGcodeState, state );
                    expectedGcodeState = null;
                    break;

                case IEvent.GRBL_ALARM:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String line = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for line=" + line );
                    assertEquals ( "alarm", expectedString, line );
                    expectedString = null;
                    break;
                    
                case IEvent.AUTOLEVEL_UPDATE:
                    if ( !(data instanceof IGcodePoint) ) fail ( "data not Point " + data.getClass () );
                    IGcodePoint p = (IGcodePoint) data;
                    if ( expectedPoint== null ) fail ( "nothing expected for p=" + p);
                    assertEquals ( "point", expectedPoint, p);
                    expectedPoint = null;
                    break;

                case IEvent.UPDATE_FIXTURE_OFFSET:
                    if ( !(data instanceof IGcodePoint) ) fail ( "data not Point " + data.getClass () );
                    IGcodePoint shift = (IGcodePoint) data;
                    if ( expectedPoint == null ) fail ( "nothing expected for shift=" + shift );
                    assertEquals ( "shift", expectedPoint, shift );
                    expectedPoint = null;
                    break;

                case IEvent.UPDATE_MODAL_MODE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String motionMode = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for motionMode=" + motionMode );
                    assertEquals ( "motionMode", expectedString, motionMode );
                    expectedString = null;
                    break;

                case IEvent.UPDATE_FIXTURE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String coord = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for coord=" + coord );
                    assertEquals ( "coord", expectedString, coord );
                    expectedString = null;
                    break;

                case IEvent.UPDATE_PLANE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String plane = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for plane=" + plane );
                    assertEquals ( "plane", expectedString, plane );
                    expectedString = null;
                    break;

                case IEvent.UPDATE_METRIC_MODE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String metricMode = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for metric mode=" + metricMode );
                    assertEquals ( "metric mode", expectedString, metricMode );
                    expectedString = null;
                    break;

                case IEvent.UPDATE_DISTANCE_MODE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String distanceMode = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for distance mode=" + distanceMode );
                    assertEquals ( "distance mode", expectedString, distanceMode );
                    expectedString = null;
                    break;

                case IEvent.AUTOLEVEL_START:
                case IEvent.AUTOLEVEL_STOP:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String timestampStop = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for timestamp=" + timestampStop );
                    assertNotNull ( "timestamp", timestampStop );
                    expectedString = null;
                    break;

                default:
                    fail ( "unknown topic=" + topic );
                    break;
            }

            return true;

        }

        @Override
        public boolean post ( String topic, Object data ) {
            fail ( "EventBroker: post not implemented" );
            return false;
        }

        @Override
        public boolean subscribe ( String topic, EventHandler eventHandler ) {
            fail ( "EventBroker: subscribe not implemented" );
            return false;
        }

        @Override
        public boolean subscribe ( String topic, String filter, EventHandler eventHandler, boolean headless ) {
            fail ( "EventBroker: subscribe not implemented" );
            return false;
        }

        @Override
        public boolean unsubscribe ( EventHandler eventHandler ) {
            fail ( "EventBroker: unsubscribe not implemented" );
            return false;
        }

    }

    private class GcodeProgramMock implements IGcodeProgram {

        private Object expectedProbe;
        private boolean setProbePointCalled;
        
        private boolean setAutoLevelScanCompletedCalled;

        @Override
        public void rotate ( double angle ) {
            fail ( "GcodeModel: setRotation not implemented" );
        }

        @Override
        public void clear () {
            fail ( "GcodeModel: clear not implemented" );
        }

        @Override
        public void appendLine ( String line ) {
            fail ( "GcodeModel: appendGcodeLine not implemented" );
        }

        @Override
        public IGcodePoint getMin () {
            fail ( "GcodeModel: getMin not implemented" );
            return null;
        }

        @Override
        public IGcodePoint getMax () {
            fail ( "GcodeModel: getMax not implemented" );
            return null;
        }

        @Override
        public int getLineCount () {
            fail ( "GcodeModel: getLineCount not implemented" );
            return 0;
        }

        @Override
        public void resetProcessed () {
            fail ( "GcodeModel: resetProcessed not implemented" );
        }

        @Override
        public void parse () {
            fail ( "GcodeModel: parseGcode not implemented" );
        }

        @Override
        public void prepareAutolevelScan ( int xSteps, int ySteps ) {
            fail ( "GcodeModel: prepareAutolevelScan not implemented" );
        }

        public boolean isProbePointCalled () {

            return setProbePointCalled;

        }

        public void setExpectedProbePoint ( IGcodePoint p ) {

            expectedProbe = p;

        }

        @Override
        public void setProbePoint ( IGcodePoint probe ) {

            if ( expectedProbe == null ) fail ( "not expected probe=" + probe );
            setProbePointCalled = true;
            assertEquals ( "setProbePoint", expectedProbe, probe );

        }

        @Override
        public IGcodePoint [] interpolateLine ( IGcodePoint point1, IGcodePoint point2 ) {
            fail ( "GcodeModel: interpolateLine not implemented" );
            return null;
        }

        @Override
        public double getStepWidthX () {
            fail ( "GcodeModel: getStepWidthX not implemented" );
            return 0;
        }

        @Override
        public double getStepWidthY () {
            fail ( "GcodeModel: getStepWidthY not implemented" );
            return 0;
        }

        @Override
        public int getXSteps () {
            fail ( "GcodeModel: getXSteps not implemented" );
            return 0;
        }

        @Override
        public int getYSteps () {
            fail ( "GcodeModel: getYSteps not implemented" );
            return 0;
        }

        @Override
        public int getNumProbePoints () {
            fail ( "GcodeModel: getNumProbePoints not implemented" );
            return 0;
        }

        @Override
        public void clearAutolevelData () {
            fail ( "GcodeModel: clearScan not implemented" );
        }

        public boolean isSetAutoLevelScanCompletedCalled () {

            return setAutoLevelScanCompletedCalled;

        }

        @Override
        public void setAutolevelScanCompleted () {

            setAutoLevelScanCompletedCalled = true;

        }

        @Override
        public boolean isAutolevelScanComplete () {
            fail ( "GcodeModel: isScanDataComplete not implemented" );
            return false;
        }

        @Override
        public boolean isLoaded () {
            fail ( "GcodeModel: isGcodeProgramLoaded not implemented" );
            return false;
        }

        @Override
        public void prepareAutolevelScan () {
            fail ( "GcodeModel: prepareAutolevelScan not implemented" );
        }

        @Override
        public double getRotationAngle () {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public IGcodePoint getProbePointAt ( int ix, int iy ) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isAutolevelScanPrepared () {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public File getGcodeProgramFile () {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void loadGcodeProgram ( File gcodeFile ) {
            // TODO Auto-generated method stub

        }

        @Override
        public File getAutolevelDataFile () {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void loadAutolevelData () {
            // TODO Auto-generated method stub

        }

        @Override
        public void saveAutolevelData () {
            // TODO Auto-generated method stub

        }

        @Override
        public IGcodeLine [] getAllGcodeLines () {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getGcodeProgramName () {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPlayerStart () {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPlayerStop () {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isPlaying () {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getDuration () {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setAutolevelStart () {
            // TODO Auto-generated method stub

        }

        @Override
        public void setAutolevelStop () {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isAutolevelScan () {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void optimize () {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isOptimized () {
            // TODO Auto-generated method stub
            return false;
        }

    }

    @Override
    public void uncaughtException ( Thread thread, Throwable exc ) {
        
        System.err.println ( "uncaught exc=" + exc );
        
        uncaughtExceptionThread = thread;
        uncaughtException = exc;

    }

    @Before
    public void setUp () {
        
        boolean testWithInjectionFramework = false; // with eclipse its run very long :-(
        if ( testWithInjectionFramework ) {
            IEclipseContext context = EclipseContextFactory.create ();
            context.set ( ISerialService.class, new SerialServiceMock () );
            underTest = ContextInjectionFactory.make ( GcodeServiceImpl.class, context );
        }
        else {
            serialServiceMock = new SerialServiceMock ();
            eventBrokerMock = new EventBrokerMock ();
            gcodeProgramMock = new GcodeProgramMock ();
            underTest = new GcodeServiceImplTestee ( eventBrokerMock, serialServiceMock, gcodeProgramMock );
        }

    }

    @Test
    public void testDefaultConstructor () {

        assertNotNull ( "constructor", underTest );
        assertNull ( "receiver set", serialServiceMock.receiver );
        serialServiceMock.setExpectedCommand ( "constructor", new String ( new byte [] { 0x18 } ) );
        underTest.sendReset ();

    }

    @Test
    public void testSendFeedHold () {

        serialServiceMock.setExpectedCommand ( "send feed hold", "!" );
        underTest.sendFeedHold ();

    }

    @Test
    public void testSendReset () {

        serialServiceMock.setExpectedCommand ( "send reset", new String ( new byte [] { 0x18 } ) );
        underTest.sendReset ();

    }

    @Test
    public void testSendStartCycle () {

        serialServiceMock.setExpectedCommand ( "send start cycle", "~" );
        underTest.sendStartCycle ();

    }

    @Test
    public void testSendStatePoll () {

        serialServiceMock.setExpectedCommand ( "send feed hold", "?" );
        underTest.sendStatePoll ();

    }

    @Test
    public void testSendCommand1 () {
        
        GcodeServiceImplTestee t = (GcodeServiceImplTestee) underTest;

        underTest.sendCommand ( "blabla" );
        assertEquals ( "send 1 length", 1, t.getQueueLength () );

        final GrblRequestImpl request = t.getQueueElements ()[0];
        assertEquals ( "send message", "blabla" + IConstant.LF, request.getMessage () );
        assertFalse ( "send suppress", request.suppressInTerminal );
        
    }

    @Test
    public void testSendCommand2 () {

        GcodeServiceImplTestee t = (GcodeServiceImplTestee) underTest;

        underTest.sendCommandSuppressInTerminal ( "G92 blabla" );
        assertEquals ( "send 2 length", 2, t.getQueueLength () );

        final GrblRequestImpl request0 = t.getQueueElements ()[0];
        assertEquals ( "send 1 line", "G92 blabla" + IConstant.LF, request0.message );
        assertTrue ( "send 1 suppress", request0.suppressInTerminal );

        final GrblRequestImpl request1 = t.getQueueElements ()[1];
        assertEquals ( "send 2 line", "$#" + IConstant.LF, request1.message );
        assertTrue ( "send 2 suppress", request1.suppressInTerminal );

    }

    private void waitForDequeuing ( final GcodeServiceImplTestee testee ) throws InterruptedException, Throwable {
    
        while ( testee.getQueueLength () != 0 )
            Thread.sleep ( 100 );
        if ( !testee.gcodeSenderThread.isAlive () ) throw uncaughtException;
    
    }

    @Test
    public void testGcodeSenderThreadStartStop () throws InterruptedException {
        
        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertFalse ( "interrupted", testee.gcodeSenderThread.isAlive () );

    }

    @Test(timeout = 2000)
    public void testGcodeSenderThreadNormalCommand () throws Throwable {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // testee.setWaitForOk ( true ); // uncomment this for timeout failure
        testee.gcodeSenderThread.setUncaughtExceptionHandler ( this );
        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        serialServiceMock.setExpectedCommand ( "normal command", "blabla" );
        eventBrokerMock.setExpectedTopic ( IEvent.GRBL_SENT );
        eventBrokerMock.setExpectedRequest ( false, "blabla" );
        testee.appendCommand ( false, "blabla" );

        waitForDequeuing ( testee );

        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertEquals ( "queue length = 0", 0, testee.getQueueLength () );

        testee.appendCommand ( false, "blabla2" );
        assertEquals ( "queue length = 1", 1, testee.getQueueLength () );

        Thread.sleep ( 1000 );

        testee.appendCommand ( false, "blabla3" );
        assertEquals ( "queue length = 2", 2, testee.getQueueLength () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test
    public void testGcodeSenderThreadSkipByAlarm () throws Throwable {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // testee.setWaitForOk ( true );
        testee.gcodeSenderThread.setUncaughtExceptionHandler ( this );
        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        testee.setSkipByAlarm ( true );

        testee.appendCommand ( false, "blabla" );
        waitForDequeuing ( testee );
        assertFalse ( "waitForOk", testee.isWaitForOk () ); // command was not proceeded, but skipped
        assertEquals ( "queue length = 0", 0, testee.getQueueLength () );

        testee.appendCommand ( false, "blabla2" );
        waitForDequeuing ( testee );
        assertFalse ( "waitForOk2", testee.isWaitForOk () ); // command was not proceeded, but skipped
        assertEquals ( "queue length2 = 0", 0, testee.getQueueLength () );

        testee.appendCommand ( false, "blabla3" );
        waitForDequeuing ( testee );
        assertFalse ( "waitForOk3", testee.isWaitForOk () ); // command was not proceeded, but skipped
        assertEquals ( "queue length3 = 0", 0, testee.getQueueLength () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test(timeout = 2000)
    public void testGcodeSenderThreadScanStart () throws Throwable {

        // Test

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // testee.setWaitForOk ( true ); // uncomment this with @Test (timeout = 2000)
        testee.gcodeSenderThread.setUncaughtExceptionHandler ( this );
        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        testee.setScanRunning ( false );

        eventBrokerMock.setExpectedTopic ( IEvent.AUTOLEVEL_START );
        eventBrokerMock.setExpectedString ( "dummy" );
        testee.appendCommand ( false, IConstant.GCODE_SCAN_START );

        waitForDequeuing ( testee );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertEquals ( "queue length = 0", 0, testee.getQueueLength () );
        assertTrue ( "scan running", testee.isScanRunning () );
        assertFalse ( "scan completed", gcodeProgramMock.isSetAutoLevelScanCompletedCalled () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test(timeout = 2000)
    public void testGcodeSenderThreadScanEnd () throws Throwable {

        // Test

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // testee.setWaitForOk ( true ); // uncomment this with @Test (timeout = 2000)
        testee.gcodeSenderThread.setUncaughtExceptionHandler ( this );
        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        testee.setScanRunning ( true );

        eventBrokerMock.setExpectedTopic ( IEvent.AUTOLEVEL_STOP );
        eventBrokerMock.setExpectedString ( "dummy" );
        testee.appendCommand ( false, IConstant.GCODE_SCAN_END );

        waitForDequeuing ( testee );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertEquals ( "queue length = 0", 0, testee.getQueueLength () );
        assertFalse ( "scan running", testee.isScanRunning () );
        assertTrue ( "scan completed", gcodeProgramMock.isSetAutoLevelScanCompletedCalled () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test
    public void testReceivedState () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String message = "<Alarm,MPos:1.01,2.02,3.03,WPos:4.04,5.05,6.06>";

        testee.setWaitForOk ( false );
        testee.setSkipByAlarm ( false );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, message );

        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_STATE );
        eventBrokerMock.setExpectedGrblState ( EGrblState.ALARM, new GcodePointImpl ( 1.01, 2.02, 3.03 ), new GcodePointImpl ( 4.04, 5.05, 6.06 ) );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        underTest.received ( message );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );

        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, message );

        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_STATE );
        eventBrokerMock.setExpectedGrblState ( EGrblState.ALARM, new GcodePointImpl ( 1.01, 2.02, 3.03 ), new GcodePointImpl ( 4.04, 5.05, 6.06 ) );

        testee.setWaitForOk ( true );
        testee.setSkipByAlarm ( true );
        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertTrue ( "skipByAlarn", testee.isSkipByAlarm () );

        underTest.received ( message );

        assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );

        // TODO_TEST implement suppress test

    }

    @Test
    public void testReceivedOkError () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String [] line = { "ok", "error" };

        for ( int i = 0; i < line.length; i++ ) {

            eventBrokerMock.setExpectedReceivedEvent ( true );
            eventBrokerMock.setExpectedResponse ( false, line[i] );

            testee.setWaitForOk ( true );
            testee.setSkipByAlarm ( true );
            assertTrue ( "waitForOk i=" + i, testee.isWaitForOk () );
            assertTrue ( "skipByAlarn i=" + i, testee.isSkipByAlarm () );

            underTest.received ( line[i] );

            assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
            eventBrokerMock.resetSendCalled ();
            assertFalse ( "waitForOk i=" + i, testee.isWaitForOk () );
            assertFalse ( "skipByAlarn i=" + i, testee.isSkipByAlarm () );

        }

    }

    @Test
    public void testReceivedAlarm () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "ALARM blabla";

        eventBrokerMock.setExpectedTopic ( IEvent.GRBL_ALARM );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );
        eventBrokerMock.setExpectedString ( line );

        testee.setWaitForOk ( true );
        testee.setSkipByAlarm ( false );
        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );

        underTest.received ( line );

        assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertTrue ( "skipByAlarn", testee.isSkipByAlarm () );

    }

    @Test
    public void testReceivedProbeNoScanning () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "[PRB:4.04,5.05,6.06]";

        eventBrokerMock.setExpectedTopic ( IEvent.AUTOLEVEL_UPDATE );
        eventBrokerMock.setExpectedPoint ( new GcodePointImpl ( 4.04, 5.05, 6.06 ) );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );

        testee.setWaitForOk ( false );
        testee.setSkipByAlarm ( false );
        testee.setScanRunning ( false );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertFalse ( "scanRunning", testee.isScanRunning () );

        underTest.received ( line );

        assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
        assertFalse ( gcodeProgramMock.isProbePointCalled () );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertFalse ( "scanRunning", testee.isScanRunning () );

    }

    @Test
    public void testReceivedProbeWithScanning () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "[PRB:4.04,5.05,6.06]";
        final GcodePointImpl p = new GcodePointImpl ( 4.04, 5.05, 6.06 );

        eventBrokerMock.setExpectedTopic ( IEvent.AUTOLEVEL_UPDATE );
        eventBrokerMock.setExpectedPoint ( p );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );

        testee.setWaitForOk ( false );
        testee.setSkipByAlarm ( false );
        testee.setScanRunning ( true );
        
        gcodeProgramMock.setExpectedProbePoint ( p );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertTrue ( "scanRunning", testee.isScanRunning () );

        underTest.received ( line );

        assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
        assertTrue ( gcodeProgramMock.isProbePointCalled () );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertTrue ( "scanRunning", testee.isScanRunning () );

    }

    @Test
    public void testReceivedCoord () { // $#

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line0 = "[G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        final String line1 = "[G54:1.02,2.03,3.04]";
        final String line2 = "[G92:1.02,2.03,3.04]";

        eventBrokerMock.setIgnoreSendDetails ( true );
        underTest.received ( line0 );
        assertFalse ( "line0 event send called", eventBrokerMock.isSendCalled () );

        eventBrokerMock.setIgnoreSendDetails ( false );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line1 );
        underTest.received ( line1 );
        assertTrue ( "line1 event send called", eventBrokerMock.isSendCalled () );

        final GcodePointImpl expectedShift = (GcodePointImpl) new GcodePointImpl ( 1.02, 2.03, 3.04 ).add ( new GcodePointImpl ( 1.02, 2.03, 3.04 ) );
        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_FIXTURE_OFFSET );
        eventBrokerMock.setExpectedPoint ( expectedShift );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line2 );
        underTest.received ( line2 );
        assertTrue ( "line2 event send called", eventBrokerMock.isSendCalled () );
        assertEquals ( "fisture shift", expectedShift, underTest.getFixtureShift () );

    }

    @Test
    public void testReceivedMode () { // $G

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // init
        eventBrokerMock.setIgnoreSendDetails ( true );
        final String line0 = "[G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        underTest.received ( line0 );
        assertFalse ( "line0 event send called", eventBrokerMock.isSendCalled () );
        assertEquals ( "fixture", "G54", underTest.getFixture () );
        assertEquals ( "metric mode", "mm", underTest.getMetricMode () );
        assertEquals ( "distance mode", "absolute", underTest.getDistanceMode () );

        eventBrokerMock.setIgnoreSendDetails ( false );

        // no update event
        final String line1 = "[G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line1 );
        eventBrokerMock.resetSendCalled ();
        underTest.received ( line1 );
        assertTrue ( "line1 event send called", eventBrokerMock.isSendCalled () );

        // update motion mode
        final String line2 = "[G1 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line2 );
        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_MODAL_MODE );
        eventBrokerMock.setExpectedString ( "G1" );
        eventBrokerMock.resetSendCalled ();
        underTest.received ( line2 );
        assertTrue ( "line2 event send called", eventBrokerMock.isSendCalled () );

        // update coord system
        final String line3 = "[G1 G55 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line3 );
        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_FIXTURE );
        eventBrokerMock.setExpectedString ( "G55" );
        eventBrokerMock.resetSendCalled ();
        underTest.received ( line3 );
        assertTrue ( "line3 event send called", eventBrokerMock.isSendCalled () );
        assertEquals ( "fixture", "G55", underTest.getFixture () );

        // update plane
        final String line4 = "[G1 G55 G18 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line4 );
        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_PLANE );
        eventBrokerMock.setExpectedString ( "ZX" );
        eventBrokerMock.resetSendCalled ();
        underTest.received ( line4 );
        assertTrue ( "line4 event send called", eventBrokerMock.isSendCalled () );

        // update metric mode
        final String line5 = "[G1 G55 G18 G20 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line5 );
        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_METRIC_MODE );
        eventBrokerMock.setExpectedString ( "inch" );
        eventBrokerMock.resetSendCalled ();
        underTest.received ( line5 );
        assertTrue ( "line5 event send called", eventBrokerMock.isSendCalled () );
        assertEquals ( "metric mode", "inch", underTest.getMetricMode () );

        // update metric mode
        final String line6 = "[G1 G55 G18 G20 G91 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line6 );
        eventBrokerMock.setExpectedTopic ( IEvent.UPDATE_DISTANCE_MODE );
        eventBrokerMock.setExpectedString ( "relative" );
        eventBrokerMock.resetSendCalled ();
        underTest.received ( line6 );
        assertTrue ( "line6 event send called", eventBrokerMock.isSendCalled () );
        assertEquals ( "distance mode", "relative", underTest.getDistanceMode () );

        // TODO_TEST implement UPDATE_SPINDLE_MODE
        // TODO_TEST implement UPDATE_COOLANT_MODE
        // TODO_TEST implement UPDATE_TOOL
        // TODO_TEST implement UPDATE_FEEDRATE
        // TODO_TEST implement UPDATE_SPINDLESPEED

    }

    @Test
    @Ignore
    public void testPlay () {
        fail ( "Not yet implemented" );
    }

    @Test
    @Ignore
    public void testScan () {
        fail ( "Not yet implemented" );
    }

}
