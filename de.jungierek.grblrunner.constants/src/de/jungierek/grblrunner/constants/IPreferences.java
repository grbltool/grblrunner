package de.jungierek.grblrunner.constants;

import org.eclipse.swt.SWT;


// TODO_PREF refactor to preferences
public interface IPreferences {

    public static final String APPLICATION_TITILE = "GrblRunner";

    public static final String INITIAL_GCODE_PATH = "C:\\Users\\Andreas\\Documents\\eagle";
    public static final String [] GCODE_FILE_EXTENSIONS = new String [] { "*.ngc" };
    public static final String AUTOLEVEL_DATA_FILE_EXTENSION = ".probe";

    public static final double WORK_AREA_X = 107.5;
    public static final double WORK_AREA_Y = 84.3;

    public static final int SPINDLE_MIN_RPM = 0;
    public static final int SPINDLE_MAX_RPM = 12000;

    public final static double Z_CLEARANCE = 15.0;

    public static final double PROBE_FEEDRATE = 40;
    public final static double PROBE_Z_MIN = -1.0;
    public final static double PROBE_Z_MAX = +3.0;

    public final static int MACRO_SPINDLE_SPEED = 7000;
    public final static double MACRO_Z_LIFTUP = 2.0;

    public final static int HOBBED_BOLT_FEEDRATE = 10;
    public final static int HOBBED_BOLT_ANGLE = 30;
    public final static int HOBBED_BOLT_BOLT_DIAMETER = 8;
    public final static double HOBBED_BOLT_X_CLEARANCE = 7;
    public static final int HOBBED_BOLT_COUNT_RETRACTION = 3;
    public static final double HOBBED_BOLT_RETRACTION = 1.0;
    public static final int HOBBED_BOLT_WAIT_AT_TARGET = 5;

    public final static int POCKET_MILL_Z_FEEDRATE = 50;
    public final static int POCKET_MILL_XY_FEEDRATE = 150;
    public static final double POCKET_MILL_DIAMETER = 3.0;
    public static final double POCKET_MILL_DIMENSION = 10.0;
    public static final double POCKET_MILL_Z_DEPTH = -1.4;
    public static final int POCKET_MILL_OVERLAP = 30;

    public final static int INITIAL_XSTEPS = 1;
    public final static int INITIAL_YSTEPS = 1;

    public static final boolean SHOW_GCODE_LINE = false;

    public static final boolean INITIAL_VIEW_ALTITUDE = true;
    public static final boolean INITIAL_VIEW_GCODE = true;
    public static final boolean INITIAL_VIEW_GRID = true;
    public static final boolean INITIAL_VIEW_WORKAREA = true;

    public final static double FIT_TO_SIZE_MARGIN = 20.0;
    public final static boolean FIT_TO_SIZE_WITH_Z = false;

    public static final int GCODE_LARGE_FONT_SIZE = 20;
    public static final int GCODE_LARGE_FONT_STYLE = SWT.BOLD;

    public static final int SERIAL_MAX_WAIT_MS = 2000;

    public final static boolean AUTOLEVEL_USE_RANDOM_Z_SIMULATION = false;
    public final static boolean AUTOLEVEL_SLOW_Z_SIMULATION = false;

    public static final int MAX_SEEK_FEEDRATE = 600; // mm/min

}
