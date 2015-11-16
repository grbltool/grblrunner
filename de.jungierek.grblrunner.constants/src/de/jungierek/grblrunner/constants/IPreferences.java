package de.jungierek.grblrunner.constants;

import org.eclipse.swt.SWT;


// TODO_PREF refactor to preferences
public interface IPreferences {

    public static final String APPLICATION_TITILE = "GrblRunner";

    public static final String [] GCODE_FILE_EXTENSION = new String [] { "*.ngc" };

    public static final int SPINDLE_MIN_RPM = 0;
    public static final int SPINDLE_MAX_RPM = 12000;

    public static final double PROBE_FEEDRATE = 40;
    public final static double PROBE_Z_CLEARANCE = 25.0;
    public final static double PROBE_Z_MIN = -1.0;
    public final static double PROBE_Z_MAX = +3.0;
    public final static int INITIAL_XSTEPS = 1;
    public final static int INITIAL_YSTEPS = 1;

    public static final double WORK_AREA_X = 107.5;
    public static final double WORK_AREA_Y = 84.3;

    public static final boolean SHOW_GCODE_LINE = false;
    public final static boolean SELECT_FILE_ENABLED_FOREVER = false;

    public static final boolean INITIAL_VIEW_ALTITUDE = true;
    public static final boolean INITIAL_VIEW_GCODE = true;
    public static final boolean INITIAL_VIEW_GRID = true;
    public static final boolean INITIAL_VIEW_WORKAREA = true;

    public final static boolean DUMP_PARSED_GCODE_LINE = false;

    public final static double FIT_TO_SIZE_MARGIN = 20.0;
    public final static boolean FIT_TO_SIZE_WITH_Z = false;

    public static final int GCODE_LARGE_FONT_SIZE = 20;
    public static final int GCODE_LARGE_FONT_STYLE = SWT.BOLD;

    public static final boolean BUTTON_GCODE_DIALOG_ON = true;

    public static final int SERIAL_MAX_WAIT_MS = 2000;

}
