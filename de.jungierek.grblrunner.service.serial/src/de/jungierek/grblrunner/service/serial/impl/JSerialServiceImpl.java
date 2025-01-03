package de.jungierek.grblrunner.service.serial.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.service.serial.ISerialServiceReceiver;

public class JSerialServiceImpl implements ISerialService {

    private static final Logger LOG = LoggerFactory.getLogger ( JSerialServiceImpl.class );

    @Inject
    private IEventBroker eventBroker;

    private ISerialServiceReceiver listener;

    private boolean detectingSerialPortsIsRunning;

    private String [] cachedPorts;
    private String portName;
    private SerialPort serialPort;

    private SerialReceiverThread serialReceiverThread;
    private InputStream in = null;
    private OutputStream out;

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
        
        LOG.info ( "jserial verion=" + SerialPort.getVersion () );

        detectingSerialPortsIsRunning = true;

        eventBroker.send ( IEvent.SERIAL_PORTS_DETECTING, null );

        portName = null; // deselect


        SerialPort [] commPorts = SerialPort.getCommPorts ();
        String [] portNames = new String [commPorts.length];
        for ( int i = 0; i < commPorts.length; i++ ) {
            // portNames.add ( commPorts [i].getDescriptivePortName () );
            portNames [i] = commPorts [i].getSystemPortName ();
        }

        cachedPorts = portNames;

        String ports = null;
        for ( String port : portNames ) {
            if ( ports == null ) ports = port;
            else ports += "," + port;
        }
        LOG.debug ( "detectSerialPorts: ports=" + ports );

        detectingSerialPortsIsRunning = false;

        LOG.debug ( "detectSerialPorts: posting event" );
        eventBroker.send ( IEvent.SERIAL_PORTS_DETECTED, cachedPorts );

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

    @Override
    public void setPortName ( String portName ) {

        if ( portName != null && portName.length () > 0 && !portName.equals ( this.portName ) && !isOpen () ) {

            this.portName = portName;

            LOG.debug ( "setPortName: posting event" );
            eventBroker.post ( IEvent.SERIAL_PORT_SELECTED, portName );

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

                                try {
                                    if ( listener != null ) listener.received ( line );
                                }
                                catch ( Throwable exc ) {
                                    LOG.error ( THREAD_NAME + " run: exception occured exc=" + exc );
                                }

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

    private void sendErrorMessage ( final String intialMsg ) {

        StringBuilder sb = new StringBuilder ();
        sb.append ( intialMsg );
        sb.append ( "\n\n" );

        eventBroker.send ( IEvent.MESSAGE_ERROR, "" + sb );

    }

    @Override
    public void connect () {

        LOG.debug ( "connect: portName=" + portName + " baudrate=" + baudrate );

        if ( isOpen () ) return;

        serialPort = SerialPort.getCommPort ( portName );
        if ( !serialPort.openPort () ) {
            sendErrorMessage ( "serial port " + portName + " not connected!" );
            serialPort = null;
            return;
        }

        serialPort.setComPortParameters ( baudrate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY );
        serialPort.setComPortTimeouts ( SerialPort.TIMEOUT_SCANNER, IConstant.SERIAL_MAX_WAIT_MS, IConstant.SERIAL_MAX_WAIT_MS );

        in = serialPort.getInputStream ();
        out = serialPort.getOutputStream ();
        
        LOG.debug ( "close: in=" + in + " out=" + out );

        serialReceiverThread = new SerialReceiverThread ();
        serialReceiverThread.start ();

        LOG.debug ( "connect: posting event" );
        eventBroker.post ( IEvent.SERIAL_CONNECTED, portName );

        // a little bit later
        LOG.debug ( "connect: resetting grbl" );
        send ( new byte [] { ISerialService.GRBL_RESET_CODE } );

    }

    @Override
    public boolean isOpen () {

        return serialPort != null;

    }

    private Object closeLock = new Object ();

    @Override
    public void close () {
        
            new Thread ( ( ) -> {
    
                synchronized ( closeLock ) {

                    LOG.debug ( "close: serialReceiverThread=" + serialReceiverThread + in + out );
                    LOG.debug ( "close: in=" + in );
                    LOG.debug ( "close: out=" + out );
                    
                    if ( !isOpen () ) return;
                    if ( serialReceiverThread == null ) return;
                    if ( in == null || out == null ) return;
            
                    serialReceiverThread.interrupt ();
        
                    LOG.debug ( "close: posting event" );
                    eventBroker.send ( IEvent.SERIAL_DISCONNECTED, "-" );
        
                    try {
                        in.close ();
                        out.close ();
                    }
                    catch ( IOException exc ) {
                        LOG.error ( "exc=" + exc );
                        sendErrorMessage ( "closing of streams failed, cause=" + exc );
                    }
                    finally {
                        in = null;
                        out = null;
                    }
        
                    if ( !serialPort.closePort () ) {
                        sendErrorMessage ( "serial port " + portName + " not connected!" );
                    }
                    serialPort = null;
                    
                }
    
            } ).start ();
            
    }

    private Object sendLock = new Object ();

    @Override
    public void send ( char c ) {

        LOG.trace ( "send: c=" + c );

        try {
            synchronized ( sendLock ) {
                if ( out != null ) out.write ( c );
            }

        }
        catch ( IOException exc ) {
            LOG.debug ( "exception in send character", exc );
            close ();
        }

    }

    @Override
    public void send ( byte [] bytes ) {

        LOG.trace ( "send: bytes=" + printBuffer ( bytes, bytes.length ) );

        try {
            synchronized ( sendLock ) {
                if ( out != null ) out.write ( bytes );
            }
        }
        catch ( IOException exc ) {
            LOG.debug ( "exception in send buffer", exc );
            close ();
        }

    }

    @Override
    public void setReceiver ( ISerialServiceReceiver receiver ) {

        this.listener = receiver;

    }

}