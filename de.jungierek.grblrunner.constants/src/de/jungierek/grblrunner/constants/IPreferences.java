package de.jungierek.grblrunner.constants;

import org.eclipse.swt.SWT;


// TODO_PREF refactor to preferences
public interface IPreferences {

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

}
