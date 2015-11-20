/**
 * 
 */
package de.jungierek.grblrunner.service.serial;

/**
 * @author Andreas
 *
 */
public interface ISerialService {
    
    //public static int DEFAULT_BAUDRATE = 256000;
    public static int DEFAULT_BAUDRATE = 115200;

    public static final int GRBL_RESET_CODE = 0x18;
    
    public void detectSerialPortsAsync ();
    public String [] getCachedSerialPorts ();
    public boolean isDetectingSerialPorts ();
    
    public void setPortName ( String portName );
    public String getPortName ();
    
    public void setBaudrate ( int baudrate );
    public int getBaudrate ();
    
    public void connect ();
    public void close ();
    boolean isOpen ();
    
    public void send ( char c );
    public void send ( byte [] bytes );
    
    public void setReceiver ( ISerialServiceReceiver receiver );

}
