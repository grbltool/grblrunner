package de.jungierek.grblrunner.service.gcode;

import java.io.File;

public interface IGcodeService {
    
    public static final char CR = 0x0D;
    public static final char LF = 0x0A;
    public static final char GRBL_RESET_CODE = 0x18;
    public static final String GRBL_RESET_STRING = new String ( new byte [] { GRBL_RESET_CODE } );
    
    // '!', '~', '?'. 0x18
    public void sendStartCycle ();
    public void sendFeedHold ();
    public void sendStatePoll ();
    public void sendReset ();
    public void sendCommand ( String line ); // append here LF
    @Deprecated
    public void sendCommand ( String line, boolean suppressInTerminal );
    public void sendCommandSuppressInTerminal ( String line );
    
    File getGcodeFile ();
    File getProbeDataFile ();

    void load ( File gcodeFile );
    void play ();

    public void scan ( double zMin, double zMax, double zClearance, double probeFedrate );
    void loadProbeData ();
    public void saveProbeData ();
    void clearProbeData ();

    public boolean isScanning ();
    public boolean isPlaying ();
    public IGcodePoint createGcodePoint ( double x, double y, double z );

}
