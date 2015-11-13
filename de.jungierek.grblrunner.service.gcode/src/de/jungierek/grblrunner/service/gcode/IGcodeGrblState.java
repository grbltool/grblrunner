package de.jungierek.grblrunner.service.gcode;

public interface IGcodeGrblState {
    
    public EGrblState getGrblState ();
    
    public IGcodePoint getMachineCoordindates ();
    public IGcodePoint getWorkCoordindates ();
    
}
