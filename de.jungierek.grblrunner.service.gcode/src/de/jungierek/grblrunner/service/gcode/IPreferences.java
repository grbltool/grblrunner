package de.jungierek.grblrunner.service.gcode;

import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;

public interface IPreferences {

    // TODO_PREF refactor to preferences
    public final static boolean USE_RANDOM_Z_SIMULATION = false;
    public final static boolean SLOW_Z_SIMULATION = false;

    public final static IGcodePoint DEFAULT_START_POINT = new GcodePointImpl ( 0.0, 0.0, 0.0 );

}
