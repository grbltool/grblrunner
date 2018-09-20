package de.jungierek.grblrunner.service.gcode;

public interface IGcodeGrblState {
    
    public EGrblState getGrblState ();
    
    public IGcodePoint getMachineCoordindates ();
    public IGcodePoint getWorkCoordindates ();
    
    public void setAvailablePlannerBufferSize ( int size );

    public int getAvailablePlannerBufferSize ();

    public void setAvailableRxBufferSize ( int size );

    public int getAvailableRxBufferSize ();

    public void setFeedRate ( double feedRate );
    public double getFeedRate ();

    public void setSpindleSpeed ( double spindleSpeed );
    public double getSpindleSpeed ();

    public void setPinState ( String state );
    public String getPinState ();

    public void setFeedOverride ( int percent );
    public int getFeedOverride ();

    public void setRapidOverride ( int percent );
    public int getRapidOverride ();

    public void setSpindleOverride ( int percent );
    public int getSpindleOverride ();


}
