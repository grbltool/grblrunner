package de.jungierek.grblrunner.service.gcode;


public interface IGcodeService {
    
    // '!', '~', '?'. 0x18
    public void sendStartCycle ();
    public void sendFeedHold ();
    public void sendStatePoll ();
    public void sendReset ();

    public void sendCommand ( String line ); // append here LF
    public void sendCommandSuppressInTerminal ( String line );
    
    public void setFixtureShift ( IGcodePoint shift );
    public IGcodePoint getFixtureShift ();

    public void playGcodeProgram ( IGcodeProgram program );
    public void scanAutolevelData ( IGcodeProgram program, double zMin, double zMax, double zClearance, double probeFedrate, boolean withError );

    public boolean isAutolevelScan ();
    public boolean isPlaying ();
    public boolean isAlarm ();

    public boolean isGrblIdle ();
    public boolean isGrblAlarm ();

    public String getFixture ();
    public String getMetricMode ();
    public String getDistanceMode ();

    public IGcodePoint createGcodePoint ( double x, double y, double z );

}
