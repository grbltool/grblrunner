package de.jungierek.grblrunner.service.serial.impl;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.service.serial.ISerialServiceReceiver;

public class SerialServiceImpl implements ISerialService {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialServiceImpl.class );

    @Inject
    private IEventBroker eventBroker;

    private Enumeration<CommPortIdentifier> portEnum;
    private String [] cachedPorts;
    private boolean detectingSerialPortsIsRunning;

    private ISerialServiceReceiver listener;

    private static String getPortTypeName ( int portType ) {
        switch ( portType ) {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    private String printBuffer ( byte [] buffer, int len ) {

        String result = "   hex: ";
        for ( int i = 0; i < len; i++ ) {
            String hex = Integer.toHexString ( buffer[i] );
            if ( hex.length () == 1 ) hex = "0" + hex;
            result += hex + " ";
        }
        return result + "<<<";

    }

    @Override
    public boolean isDetectingSerialPorts () {

        return detectingSerialPortsIsRunning;

    }

    // in case method called twice or more
    private void detectSerialPorts () {

        LOG.debug ( "detectSerialPorts:" );

        detectingSerialPortsIsRunning = true;

        eventBroker.send ( IEvents.SERIAL_PORTS_DETECTING, null );

        portName = null; // deselect

        portEnum = CommPortIdentifier.getPortIdentifiers ();

        ArrayList<String> portNames = new ArrayList<String> ();
        while ( portEnum.hasMoreElements () ) {
            CommPortIdentifier pid = portEnum.nextElement ();
            int portType = pid.getPortType ();
            LOG.trace ( "detectSerialPorts:" + pid.getName () + " - " + getPortTypeName ( portType ) );
            if ( portType == CommPortIdentifier.PORT_SERIAL ) {
                portNames.add ( pid.getName () );
            }
        }

        cachedPorts = portNames.toArray ( new String [portNames.size ()] );

        String ports = null;
        for ( String port : portNames ) {
            if ( ports == null ) ports = port;
            else ports += "," + port;
        }
        LOG.debug ( "detectSerialPorts: ports=" + ports );

        detectingSerialPortsIsRunning = false;

        LOG.debug ( "detectSerialPorts: posting event" );
        eventBroker.send ( IEvents.SERIAL_PORTS_DETECTED, cachedPorts );

    }

    @Override
    public void detectSerialPortsAsync () {
        
        LOG.debug ( "detectSerialPortsAsync: start" );

        new Thread ( ( ) -> detectSerialPorts () ).start ();


    }

    @Override
    public String [] getCachedSerialPorts () {

        return cachedPorts;

    }

    private String portName;

    @Override
    public void setPortName ( String portName ) {

        if ( portName != null && portName.length () > 0 && !portName.equals ( this.portName ) && !isOpen () ) {

            this.portName = portName;

            LOG.debug ( "setPortName: posting event" );
            eventBroker.post ( IEvents.SERIAL_PORT_SELECTED, portName );

        }

    }

    @Override
    public String getPortName () {

        return portName;

    }

    private int baudrate = DEFAULT_BAUDRATE;

    @Override
    public void setBaudrate ( int baudrate ) {

        this.baudrate = baudrate;

    }

    @Override
    public int getBaudrate () {

        return baudrate;

    }

    private class SerialReceiverThread extends Thread {

        private final static String THREAD_NAME = "serial-receiver";
        
        public SerialReceiverThread () {

            super ( THREAD_NAME );

        }

        @Override
        public synchronized void start () {

            LOG.debug ( THREAD_NAME + ": started" );
            super.start ();

        }

        @Override
        public void run () {

            String line = "";
            byte [] buffer = new byte [1024];
            int len = -1;

            try {

                while ( !isInterrupted () && ((len = in.read ( buffer )) > -1) ) {

                    if ( len > 0 ) {

                        String received = new String ( buffer, 0, len - 0 );
                        LOG.trace ( THREAD_NAME + ": len=" + len + " received=#" + received + "#" );
                        LOG.trace ( THREAD_NAME + ": buffer=" + printBuffer ( buffer, len ) );

                        boolean eolFound = false;
                        int startPos = 0;
                        for ( int i = 0; i < len; i++ ) {

                            if ( buffer[i] == '\n' ) { // each GRBL line ends with "\r\n" 0x0D 0x0A

                                eolFound = true;

                                line += new String ( buffer, startPos, i + 1 - startPos );

                                // TODO maybe enclosing the received call with a catch clause for runtime excptions
                                if ( listener != null ) listener.received ( line );

                                startPos = i + 1;
                                line = "";

                            }

                        }

                        LOG.trace ( "### len=" + len + " startPos=" + startPos + " eol=" + eolFound + " line=" + line + "<<<" );

                        if ( eolFound ) {
                            if ( startPos < len ) {
                                line = new String ( buffer, startPos, len - startPos );
                            }
                        }
                        else {
                            line += received;
                        }

                    }
                }
            }
            catch ( IOException exc ) {
                exc.printStackTrace ();
            }

            LOG.debug ( THREAD_NAME + ": stopped" );

        }

    }

    private SerialReceiverThread serialReceiverThread;
    private SerialPort serialPort;
    private InputStream in = null;
    private OutputStream out;

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

    @Override
    public void connect () {

        LOG.debug ( "connect: portName=" + portName + " baudrate=" + baudrate );

        if ( isOpen () ) return;

        CommPortIdentifier commPortIdentifier = null;
        try {

            commPortIdentifier = CommPortIdentifier.getPortIdentifier ( portName );
            serialPort = (SerialPort) commPortIdentifier.open ( "grbl-runner", IPreferences.SERIAL_MAX_WAIT_MS );
            serialPort.setSerialPortParams ( baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
            in = new RXTXInputStream ( serialPort );
            out = serialPort.getOutputStream ();

            serialReceiverThread = new SerialReceiverThread ();
            serialReceiverThread.start ();

            LOG.trace ( "connect: posting event" );
            eventBroker.post ( IEvents.SERIAL_CONNECTED, portName );

            // a little bit later
            send ( new byte [] { ISerialService.GRBL_RESET_CODE } );

        }
        catch ( NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException exc ) {
            LOG.error ( "exc=" + exc );
            sendErrorMessage ( "serial port " + portName + " not connected!", exc );
        }

    }

    @Override
    public boolean isOpen () {

        return serialPort != null;

    }

    @Override
    public void close () {

        LOG.debug ( "close: serialReceiverThread=" + serialReceiverThread );
        
        if ( serialReceiverThread == null ) return;

        new Thread ( ( ) -> {

            serialReceiverThread.interrupt ();

            try {
                in.close ();
                out.close ();
                serialPort.close ();
                in = null;
                out = null;
                serialPort = null;
            }
            catch ( IOException exc ) {
                LOG.error ( "exc=" + exc );
                sendErrorMessage ( "serial port " + portName + " not connected!", exc );
            }

            LOG.debug ( "close: posting event" );
            eventBroker.send ( IEvents.SERIAL_DISCONNECTED, "-" );

        } ).start ();

    }

    private Object lock = new Object ();

    @Override
    public void send ( char c ) {

        LOG.trace ( "send: c=" + c );

        try {
            synchronized ( lock ) {
                if ( out != null ) out.write ( c );
            }

        }
        catch ( IOException exc ) {
            // TODO Auto-generated catch block
            exc.printStackTrace ();
        }

    }

    @Override
    public void send ( byte [] bytes ) {

        LOG.trace ( "send: bytes=" + printBuffer ( bytes, bytes.length ) );

        try {
            synchronized ( lock ) {
                if ( out != null ) out.write ( bytes );
            }
        }
        catch ( IOException exc ) {
            // TODO Auto-generated catch block
            exc.printStackTrace ();
        }

    }

    @Override
    public void setReceiver ( ISerialServiceReceiver receiver ) {

        this.listener = receiver;

    }

}