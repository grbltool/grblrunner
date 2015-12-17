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

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeResponse;
import de.jungierek.grblrunner.service.gcode.impl.GcodeGrblStateImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodeResponseImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodeServiceImpl;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.service.serial.ISerialServiceReceiver;

public class GcodeServiceImplTest implements UncaughtExceptionHandler {

    // for test cast it explicite down
    private GcodeServiceImpl underTest;

    private SerialServiceMock serialServiceMock;
    private EventBrokerMock eventBrokerMock;
    private GcodeModelMock gcodeModelMock;

    private volatile Thread uncaughtExceptionThread;
    private volatile Throwable uncaughtException;

    private class GcodeServiceImplTestee extends GcodeServiceImpl {
        

        public final GcodeSenderThread gcodeSenderThread;

        private GcodeServiceImplTestee ( EventBrokerMock eventBrocker, SerialServiceMock serial, GcodeModelMock model ) {
            
            // super ( eventBrocker, serial, model );
            gcodeSenderThread = new GcodeSenderThread ();
            
        }
        
        public int getQueueLength () {
            return queue.size ();
        }

        public GcodeResponseImpl [] getQueueElements () {
            return queue.toArray ( new GcodeResponseImpl [0] );
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
            queue.add ( new GcodeResponseImpl ( suppressInTerminal, command ) );
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

        public void setExpected ( String explaination, String expectedCommand ) {

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
        
        private IGcodeResponse expectedResponse;
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

        public void setExpectedResponse ( boolean expectedSuppressInTerminal, String expectedLine ) {

            expectedResponse = new GcodeResponseImpl ( expectedSuppressInTerminal, expectedLine );

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

            if ( IEvents.GRBL_RECEIVED.equals ( topic ) && receivedEventExpected ) {
                receivedEventExpected = false;
            }
            else {
                assertEquals ( "topic", expectedTopic, topic );
                expectedTopic = null;
            }

            sendCalled = true;

            switch ( topic ) {

                case IEvents.GRBL_RECEIVED:
                case IEvents.GRBL_SENT:
                    if ( !(data instanceof IGcodeResponse) ) fail ( "data not IGcodeResponse " + data.getClass () );
                    IGcodeResponse response = (IGcodeResponse) data;
                    if ( expectedResponse == null ) fail ( "nothing expected for response=" + response );
                    // TODO_TEST change to equlas after implementatioj in class GcodeResponseImpl
                    assertEquals ( "sent suppressInTerminal", expectedResponse.suppressInTerminal (), response.suppressInTerminal () );
                    assertEquals ( "sent line", expectedResponse.getLine (), response.getLine () );
                    expectedResponse = null;
                    break;

                case IEvents.UPDATE_STATE:
                    if ( !(data instanceof IGcodeGrblState) ) fail ( "data not IGcodeGrblState " + data.getClass () );
                    IGcodeGrblState state = (IGcodeGrblState) data;
                    if ( expectedGcodeState == null ) fail ( "nothing expected for state=" + state );
                    assertEquals ( "update state", expectedGcodeState, state );
                    expectedGcodeState = null;
                    break;

                case IEvents.GRBL_ALARM:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String line = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for line=" + line );
                    assertEquals ( "alarm", expectedString, line );
                    expectedString = null;
                    break;
                    
                case IEvents.AUTOLEVEL_UPDATE:
                    if ( !(data instanceof IGcodePoint) ) fail ( "data not Point " + data.getClass () );
                    IGcodePoint p = (IGcodePoint) data;
                    if ( expectedPoint== null ) fail ( "nothing expected for p=" + p);
                    assertEquals ( "point", expectedPoint, p);
                    expectedPoint = null;
                    break;

                case IEvents.UPDATE_FIXTURE_OFFSET:
                    assertNull ( "data must be null", data );
                    break;

                case IEvents.UPDATE_MOTION_MODE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String motionMode = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for motionMode=" + motionMode );
                    assertEquals ( "motionMode", expectedString, motionMode );
                    expectedString = null;
                    break;

                case IEvents.UPDATE_FIXTURE:
                    if ( !(data instanceof String) ) fail ( "data not String " + data.getClass () );
                    String coord = (String) data;
                    if ( expectedString == null ) fail ( "nothing expected for coord=" + coord );
                    assertEquals ( "motionMode", expectedString, coord );
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

    private class GcodeModelMock implements IGcodeProgram {

        private Object expectedProbe;
        private boolean setProbePointCalled;
        private IGcodePoint expectedShift;

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

        public void setExpectedShift ( IGcodePoint p ) {

            expectedShift = p;

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

        @Override
        public void setAutolevelScanCompleted () {
            fail ( "GcodeModel: setScanDataCompleted not implemented" );
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
            gcodeModelMock = new GcodeModelMock ();
            underTest = new GcodeServiceImplTestee ( eventBrokerMock, serialServiceMock, gcodeModelMock );
        }

    }

    @Test
    public void testDefaultConstructor () {

        assertNotNull ( "constructor", underTest );
        assertNull ( "receiver set", serialServiceMock.receiver );
        serialServiceMock.setExpected ( "constructor", new String ( new byte [] { 0x18 } ) );
        underTest.sendReset ();

    }

    @Test
    public void testSendFeedHold () {

        serialServiceMock.setExpected ( "send feed hold", "!" );
        underTest.sendFeedHold ();

    }

    @Test
    public void testSendReset () {

        serialServiceMock.setExpected ( "send reset", new String ( new byte [] { 0x18 } ) );
        underTest.sendReset ();

    }

    @Test
    public void testSendStartCycle () {

        serialServiceMock.setExpected ( "send start cycle", "~" );
        underTest.sendStartCycle ();

    }

    @Test
    public void testSendStatePoll () {

        serialServiceMock.setExpected ( "send feed hold", "?" );
        underTest.sendStatePoll ();

    }

    @Test
    public void testSendSingleSignCommandChar () {

        serialServiceMock.setExpected ( "send feed hold", "a" );
        // underTest.sendSingleSignCommand ( 'a' );

    }

    @Test
    public void testSendSingleSignCommandCharBoolean () {

        serialServiceMock.setExpected ( "send feed hold", "b" );
        // underTest.sendSingleSignCommand ( 'b', true );

    }

    @Test
    public void testSendCommand1 () {
        
        GcodeServiceImplTestee t = (GcodeServiceImplTestee) underTest;

        underTest.sendCommand ( "blabla" );
        assertEquals ( "send 1 length", 1, t.getQueueLength () );

        final GcodeResponseImpl response = t.getQueueElements ()[0];
        assertEquals ( "send line", "blabla" + IConstants.LF, response.line );
        assertFalse ( "send suppress", response.suppressInTerminal );
        
    }

    @Test
    public void testSendCommand2 () {

        GcodeServiceImplTestee t = (GcodeServiceImplTestee) underTest;

        underTest.sendCommandSuppressInTerminal ( "G92 blabla" );
        assertEquals ( "send 1 length", 2, t.getQueueLength () );

        GcodeResponseImpl response = t.getQueueElements ()[0];
        assertEquals ( "send 1 line", "G92 blabla" + IConstants.LF, response.line );
        assertTrue ( "send 1 suppress", response.suppressInTerminal );

        response = t.getQueueElements ()[1];
        assertEquals ( "send 2 line", "$#" + IConstants.LF, response.line );
        assertTrue ( "send 2 suppress", response.suppressInTerminal );

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
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test(timeout = 2000)
    public void testGcodeSenderThreadNormalCommand () throws Throwable {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // testee.setWaitForOk ( true );
        testee.gcodeSenderThread.setUncaughtExceptionHandler ( this );
        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        serialServiceMock.setExpected ( "normal command", "blabla" );
        eventBrokerMock.setExpectedTopic ( IEvents.GRBL_SENT );
        eventBrokerMock.setExpectedResponse ( false, "blabla" );
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
        assertFalse ( "waitForOk", testee.isWaitForOk () ); // command was not proceded, but skipped
        assertEquals ( "queue lebnth = 0", 0, testee.getQueueLength () );

        testee.appendCommand ( false, "blabla2" );
        waitForDequeuing ( testee );
        assertFalse ( "waitForOk2", testee.isWaitForOk () ); // command was not proceded, but skipped
        assertEquals ( "queue length2 = 0", 0, testee.getQueueLength () );

        testee.appendCommand ( false, "blabla3" );
        waitForDequeuing ( testee );
        assertFalse ( "waitForOk3", testee.isWaitForOk () ); // command was not proceded, but skipped
        assertEquals ( "queue length3 = 0", 0, testee.getQueueLength () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test
    public void testGcodeSenderThreadScanEnd () throws Throwable {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // testee.setWaitForOk ( true );
        testee.gcodeSenderThread.setUncaughtExceptionHandler ( this );
        testee.gcodeSenderThread.start ();
        assertTrue ( "running", testee.gcodeSenderThread.isAlive () );

        testee.setScanRunning ( true );
        // TODO_TEST testee.setScanCompleted ( false );
        testee.appendCommand ( false, IConstants.GCODE_SCAN_END );

        waitForDequeuing ( testee );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertEquals ( "queue lebgth = 0", 0, testee.getQueueLength () );
        assertFalse ( "scan running", testee.isScanRunning () );
        // TODO_TEST assertTrue ( "scan completed", testee.isScanCompleted () );

        testee.gcodeSenderThread.interrupt ();
        testee.gcodeSenderThread.join ();
        assertTrue ( "interrupted", !testee.gcodeSenderThread.isAlive () );

    }

    @Test
    public void testReceivedState () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "<Alarm,MPos:1.01,2.02,3.03,WPos:4.04,5.05,6.06>";

        testee.setWaitForOk ( false );
        testee.setSkipByAlarm ( false );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );

        eventBrokerMock.setExpectedTopic ( IEvents.UPDATE_STATE );
        eventBrokerMock.setExpectedGrblState ( EGrblState.ALARM, new GcodePointImpl ( 1.01, 2.02, 3.03 ), new GcodePointImpl ( 4.04, 5.05, 6.06 ) );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        underTest.received ( line );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );

        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );

        eventBrokerMock.setExpectedTopic ( IEvents.UPDATE_STATE );
        eventBrokerMock.setExpectedGrblState ( EGrblState.ALARM, new GcodePointImpl ( 1.01, 2.02, 3.03 ), new GcodePointImpl ( 4.04, 5.05, 6.06 ) );

        testee.setWaitForOk ( true );
        testee.setSkipByAlarm ( true );
        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertTrue ( "skipByAlarn", testee.isSkipByAlarm () );

        underTest.received ( line );

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

        eventBrokerMock.setExpectedTopic ( IEvents.GRBL_ALARM );
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
    public void testReceivedScanStop () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "SCAN_STOP";

        eventBrokerMock.setExpectedTopic ( IEvents.GRBL_ALARM );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );

        testee.setWaitForOk ( true );
        testee.setSkipByAlarm ( false );
        testee.setScanRunning ( true );
        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertTrue ( "scanRunning", testee.isScanRunning () );

        underTest.received ( line );

        assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
        assertTrue ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertFalse ( "scanRunning", testee.isScanRunning () );

    }

    @Test
    public void testReceivedProbeNoScanning () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "[PRB:4.04,5.05,6.06]";

        eventBrokerMock.setExpectedTopic ( IEvents.AUTOLEVEL_UPDATE );
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
        assertFalse ( gcodeModelMock.isProbePointCalled () );
        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertFalse ( "scanRunning", testee.isScanRunning () );

    }

    @Test
    public void testReceivedProbeWithScanning () {

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;
        final String line = "[PRB:4.04,5.05,6.06]";
        final GcodePointImpl p = new GcodePointImpl ( 4.04, 5.05, 6.06 );

        eventBrokerMock.setExpectedTopic ( IEvents.AUTOLEVEL_UPDATE );
        eventBrokerMock.setExpectedPoint ( p );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line );

        testee.setWaitForOk ( false );
        testee.setSkipByAlarm ( false );
        testee.setScanRunning ( true );
        
        gcodeModelMock.setExpectedProbePoint ( p );

        assertFalse ( "waitForOk", testee.isWaitForOk () );
        assertFalse ( "skipByAlarn", testee.isSkipByAlarm () );
        assertTrue ( "scanRunning", testee.isScanRunning () );

        underTest.received ( line );

        assertTrue ( "line event send called", eventBrokerMock.isSendCalled () );
        assertTrue ( gcodeModelMock.isProbePointCalled () );
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

        eventBrokerMock.setExpectedTopic ( IEvents.UPDATE_FIXTURE_OFFSET );
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line2 );
        gcodeModelMock.setExpectedShift ( new GcodePointImpl ( 1.02, 2.03, 3.04 ).add ( new GcodePointImpl ( 1.02, 2.03, 3.04 ) ) );
        underTest.received ( line2 );
        assertTrue ( "line2 event send called", eventBrokerMock.isSendCalled () );

    }

    @Test
    public void testReceivedMode () { // $G

        final GcodeServiceImplTestee testee = (GcodeServiceImplTestee) underTest;

        // init
        eventBrokerMock.setIgnoreSendDetails ( true );
        final String line0 = "[G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        underTest.received ( line0 );
        assertFalse ( "line0 event send called", eventBrokerMock.isSendCalled () );

        eventBrokerMock.setIgnoreSendDetails ( false );

        // no update event
        final String line1 = "[G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line1 );
        underTest.received ( line1 );
        assertTrue ( "line1 event send called", eventBrokerMock.isSendCalled () );

        // update motion mode
        final String line2 = "[G1 G54 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line2 );
        eventBrokerMock.setExpectedTopic ( IEvents.UPDATE_MOTION_MODE );
        eventBrokerMock.setExpectedString ( "G1" );
        underTest.received ( line2 );
        assertTrue ( "line2 event send called", eventBrokerMock.isSendCalled () );

        // update coord system
        final String line3 = "[G1 G55 G17 G21 G90 G94 M0 M5 M9 T0 F1000. S0.]";
        eventBrokerMock.setExpectedReceivedEvent ( true );
        eventBrokerMock.setExpectedResponse ( false, line3 );
        eventBrokerMock.setExpectedTopic ( IEvents.UPDATE_FIXTURE );
        eventBrokerMock.setExpectedString ( "G55" );
        underTest.received ( line3 );
        assertTrue ( "line2 event send called", eventBrokerMock.isSendCalled () );

        // TODO_TEST implement UPDATE_PLANE
        // TODO_TEST implement UPDATE_METRIC_MODE
        // TODO_TEST implement UPDATE_DISTANCE_MODE
        // TODO_TEST implement UPDATE_FEEDRATE
        // TODO_TEST implement UPDATE_SPINDLESPEED

    }

    @Test
    @Ignore
    public void testLoad () {
        fail ( "Not yet implemented" );
    }

    @Test
    @Ignore
    public void testPlay () {
        fail ( "Not yet implemented" );
    }

    @Test
    @Ignore
    public void testDispose () {
        fail ( "Not yet implemented" );
    }

    @Test
    @Ignore
    public void testScan () {
        fail ( "Not yet implemented" );
    }

    @Test
    @Ignore
    public void testClearScan () {
        fail ( "Not yet implemented" );
    }

}
