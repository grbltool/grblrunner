package de.jungierek.grblrunner.service.serial.impl;

//package org.jergometer.communication;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

/**
 * Because the serial port of RXTX has a problem with losing if there is a BufferedReader listening on input stream of the port, we use this special RXTX reader which reads only as
 * many bytes as available on the input stream. By doing so, it is still possible to close the serial port.
 */
public class RXTXInputStream extends InputStream implements SerialPortEventListener {

    /** Source of all problems. */
    private InputStream in;

    // private LinkedList<Integer> queue = new LinkedList<Integer>();

    public RXTXInputStream ( SerialPort serialPort ) throws IOException {

        this.in = serialPort.getInputStream ();

        // add me as listener
        try {
            serialPort.addEventListener ( this );
            serialPort.notifyOnDataAvailable ( true );
        }
        catch ( TooManyListenersException exc ) {
            exc.printStackTrace ();
        }

    }

    @Override
    public synchronized int read () throws IOException {

        try {
            while ( true ) {
                if ( Thread.interrupted () ) { throw new IOException ( "interrupted" ); }

                if ( in.available () == 0 ) {
                    wait ();
                }

                return in.read ();
            }
        }
        catch ( InterruptedException exc ) {
            throw new IOException ( "interrupted" );
        }

    }

    @Override
    public int available () throws IOException {
        return in.available ();
    }

    @Override
    public synchronized int read ( byte b [], int off, int len ) throws IOException {
        if ( b == null ) {
            throw new NullPointerException ();
        }
        else if ( (off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0) ) {
            throw new IndexOutOfBoundsException ();
        }
        else if ( len == 0 ) { return 0; }

        try {
            if ( available () == 0 ) {
                wait ();
            }

            len = Math.min ( len, off + available () );

            int c = read ();
            if ( c == -1 ) { return -1; }
            b[off] = (byte) c;

            int i = 1;
            try {
                for ( ; i < len; i++ ) {
                    c = read ();
                    if ( c == -1 ) {
                        break;
                    }
                    b[off + i] = (byte) c;
                }
            }
            catch ( IOException ee ) {
                ee.printStackTrace ();
            }

            return i;

        }
        catch ( InterruptedException e ) {
            return -1;
        }
    }

    @Override
    public void serialEvent ( SerialPortEvent serialPortEvent ) {
        if ( serialPortEvent.getEventType () == SerialPortEvent.DATA_AVAILABLE ) {
            try {
                synchronized ( this ) {
                    // while (true) {
                    if ( available () <= 0 ) return;
                    notify ();
                }
                // }
            }
            catch ( IOException e ) {
                e.printStackTrace ();
            }
        }
    }
}
