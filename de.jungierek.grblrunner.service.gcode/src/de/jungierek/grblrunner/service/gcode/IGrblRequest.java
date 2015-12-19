package de.jungierek.grblrunner.service.gcode;

public interface IGrblRequest {

    public String getMessage ();

    public boolean isSuppressInTerminal ();

    public boolean isReset ();
    public boolean isHome ();
    public boolean isUnlock ();

}
