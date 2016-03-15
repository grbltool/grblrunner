package de.jungierek.grblrunner.constants;

import org.eclipse.swt.SWT;


// TODO_PREF refactor to preferences
public interface IPreferences {

    public static final String INITIAL_GCODE_PATH = "C:\\Users\\Andreas\\Documents\\eagle";
    public static final String [] GCODE_FILE_EXTENSIONS = new String [] { "*.ngc" };
    public static final String AUTOLEVEL_DATA_FILE_EXTENSION = ".probe";

    public static final int SPINDLE_MIN_RPM = 0;
    public static final int SPINDLE_MAX_RPM = 12000;

    public static final double Z_CLEARANCE = 15.0;

    public static final double PROBE_FEEDRATE = 40;
    public static final double PROBE_Z_MIN = -1.0;
    public static final double PROBE_Z_MAX = +3.0;

    public static final double PROBE_DEPTH = -3.0;
    public static final boolean PROBE_WITH_ERROR = false;

    public static final int MACRO_SPINDLE_SPEED = 7000;
    public static final double MACRO_Z_LIFTUP = 2.0;

    public static final int HOBBED_BOLT_FEEDRATE = 10;
    public static final int HOBBED_BOLT_ANGLE = 30;
    public static final int HOBBED_BOLT_BOLT_DIAMETER = 8;
    public static final double HOBBED_BOLT_X_CLEARANCE = 7;
    public static final int HOBBED_BOLT_COUNT_RETRACTION = 3;
    public static final double HOBBED_BOLT_RETRACTION = 1.0;
    public static final int HOBBED_BOLT_WAIT_AT_TARGET = 5;

    public static final int POCKET_MILL_Z_FEEDRATE = 50;
    public static final int POCKET_MILL_XY_FEEDRATE = 150;
    public static final double POCKET_MILL_DIAMETER = 3.0;
    public static final double POCKET_MILL_DIMENSION = 10.0;
    public static final double POCKET_MILL_Z_DEPTH = -1.4;
    public static final int POCKET_MILL_OVERLAP = 30;

    public static final int GCODE_LARGE_FONT_SIZE = 20;
    public static final int GCODE_LARGE_FONT_STYLE = SWT.BOLD;

    public static final int SERIAL_MAX_WAIT_MS = 2000;

    public static final boolean AUTOLEVEL_USE_RANDOM_Z_SIMULATION = false;
    public static final boolean AUTOLEVEL_SLOW_Z_SIMULATION = false;

    public static final int MAX_SEEK_FEEDRATE = 600; // mm/min, 10 mm/sec
    public static final int AVG_SEEK_FEEDRATE = MAX_SEEK_FEEDRATE / 12; // mm/min, factor is determined experimentally

}
