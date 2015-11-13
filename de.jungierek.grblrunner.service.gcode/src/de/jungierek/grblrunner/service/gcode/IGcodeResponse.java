package de.jungierek.grblrunner.service.gcode;

public interface IGcodeResponse {
    
    public String getLine ();
    
    public boolean suppressInTerminal ();

    public boolean isReset ();

}
