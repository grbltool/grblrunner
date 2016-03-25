package de.jungierek.grblrunner.constants;


public interface IPreferenceKey {

    // --- Mill ---
    public static final String GCODE_PATH = IConstant.KEY_BASE + ".gcode.path";
    public static final String BAUDRATE = IConstant.KEY_BASE + ".baudrate";
    public static final String WORK_AREA_MAX_X = IConstant.KEY_BASE + ".workarea.max.x";
    public static final String WORK_AREA_MAX_Y = IConstant.KEY_BASE + ".workarea.max.y";
    public static final String SPINDLE_MIN = IConstant.KEY_BASE + ".spindle.min";
    public static final String SPINDLE_MAX = IConstant.KEY_BASE + ".spindle.max";
    public static final String MAX_SEEK_FEEDRATE = IConstant.KEY_BASE + ".seekrate.max";
    public static final String ACCELARATION = IConstant.KEY_BASE + ".accelaration";
    public static final String AVG_SEEK_FEEDRATE_CORRECTOR = IConstant.KEY_BASE + ".seekrate.avg.corrector";
    public static final String Z_CLEARANCE = IConstant.KEY_BASE + ".clearance.z";

    // --- GcodeViewGroup ---
    public static final String FIT_TO_SIZE_MARGIN = IConstant.KEY_BASE + ".fittosize.margin";
    public static final String FIT_TO_SIZE_WITH_Z = IConstant.KEY_BASE + ".fittosize.withz";

    public static final String COLOR_AUTOLEVEL_GRID = IConstant.KEY_BASE + ".autolevel.grid.color";
    public static final String COLOR_GANTRY = IConstant.KEY_BASE + ".gantry.color";
    public static final String COLOR_PROCESSED = IConstant.KEY_BASE + ".processed.color";
    public static final String COLOR_SEEK = IConstant.KEY_BASE + ".seek.color";
    public static final String COLOR_LINEAR = IConstant.KEY_BASE + ".linear.color";
    public static final String COLOR_ARC = IConstant.KEY_BASE + ".arc.color";
    public static final String COLOR_PROBE = IConstant.KEY_BASE + ".probe.color";
    public static final String COLOR_MACHINE_ORIGIN = IConstant.KEY_BASE + ".origin.machine.color";
    public static final String COLOR_WORK_ORIGIN = IConstant.KEY_BASE + ".origin.work.color";
    public static final String COLOR_WORKAREA_BORDER = IConstant.KEY_BASE + ".workarea.border.color";
    public static final String COLOR_WORKAREA_MIDCROSS = IConstant.KEY_BASE + ".workarea.midcross.color";

    // --- SerialPart ---
    public static final String COLOR_CONNECT = IConstant.KEY_BASE + ".connect.color";
    public static final String COLOR_DISCONNECT = IConstant.KEY_BASE + ".disconnect.color";

    // --- TerminalPart ---
    public static final String COLOR_TERMINAL_FOREGROUND = IConstant.KEY_BASE + ".terminal.foreground.color";
    public static final String COLOR_TERMINAL_BACKGROUND = IConstant.KEY_BASE + ".terminal.background.color";
    public static final String COLOR_ALARM_FOREGROUND = IConstant.KEY_BASE + ".alarm.foreground.color";
    public static final String COLOR_ALARM_BACKGROUND = IConstant.KEY_BASE + ".alarm.background.color";
    public static final String COLOR_TIMESTAMP_BACKGROUND = IConstant.KEY_BASE + ".timestamp.background.color";
    public static final String COLOR_SUPPRESSED_LINE_FOREGROUND = IConstant.KEY_BASE + ".suppressed.foreground.color";
    public static final String COLOR_OK_FOREGROUND = IConstant.KEY_BASE + ".ok.foreground.color";
    public static final String COLOR_ERROR_FOREGROUND = IConstant.KEY_BASE + ".error.foreground.color";
    public static final String COLOR_GRBL_FOREGROUND = IConstant.KEY_BASE + ".grbl.foreground.color";
    public static final String COLOR_GRBL_BACKGROUND = IConstant.KEY_BASE + ".grbl.background.color";
    public static final String COLOR_OK_SUPPRESSED_FOREGROUND = IConstant.KEY_BASE + ".ok.suppressed.foreground.color";
    public static final String COLOR_ERROR_SUPPRESSED_FOREGROUND = IConstant.KEY_BASE + ".error.suppressed.foreground.color";
    public static final String COLOR_GRBL_SUPPRESSED_FOREGROUND = IConstant.KEY_BASE + ".grbl.suppressed.foreground.color";
    public static final String COLOR_GRBL_SUPPRESSED_BACKGROUND = IConstant.KEY_BASE + ".grbl.suppressed.background.color";
    public static final String TERMINAL_FONT_DATA = IConstant.KEY_BASE + ".terminal.fontdata";

    // --- Probe/Autolevel ---
    public static final String PROBE_FEEDRATE = IConstant.KEY_BASE + ".probe.feedrate";
    public static final String PROBE_DEPTH = IConstant.KEY_BASE + ".probe.depth";
    public static final String PROBE_WITH_ERROR = IConstant.KEY_BASE + ".probe.witherror";
    public static final String PROBE_Z_MAX = IConstant.KEY_BASE + ".probe.zmax";

    // --- Macro ---
    public static final String MACRO_SPINDLE_SPEED = IConstant.KEY_BASE + ".macro.spindlespeed";
    public static final String MACRO_Z_LIFTUP = IConstant.KEY_BASE + ".macro.zliftup";

    public static final String HOBBED_BOLT_FEEDRATE = IConstant.KEY_BASE + ".macro.hobbedbolt.feedrate";
    public static final String HOBBED_BOLT_ANGLE = IConstant.KEY_BASE + ".macro.hobbedbolt.angle";
    public static final String HOBBED_BOLT_BOLT_DIAMETER = IConstant.KEY_BASE + ".macro.hobbedbolt.boltdiameter";
    public static final String HOBBED_BOLT_X_CLEARANCE = IConstant.KEY_BASE + ".macro.hobbedbolt.xclearance";
    public static final String HOBBED_BOLT_COUNT_RETRACTION = IConstant.KEY_BASE + ".macro.hobbedbolt.countretraction";
    public static final String HOBBED_BOLT_RETRACTION = IConstant.KEY_BASE + ".macro.hobbedbolt.retraction";
    public static final String HOBBED_BOLT_WAIT_AT_TARGET = IConstant.KEY_BASE + ".macro.hobbedbolt.waitattarget";

    public static final String POCKET_MILL_Z_FEEDRATE = IConstant.KEY_BASE + ".macro.pocket.zfeedrate";
    public static final String POCKET_MILL_XY_FEEDRATE = IConstant.KEY_BASE + ".macro.pocket.xyfeedrate";
    public static final String POCKET_MILL_DIAMETER = IConstant.KEY_BASE + ".macro.pocket.milldiameter";
    public static final String POCKET_MILL_DIMENSION = IConstant.KEY_BASE + ".macro.pocket.milldimension";
    public static final String POCKET_MILL_Z_DEPTH = IConstant.KEY_BASE + ".macro.pocket.zdepth";
    public static final String POCKET_MILL_OVERLAP = IConstant.KEY_BASE + ".macro.pocket.overlap";

    // --- Large Gcode Coordinates
    public static final String GCODE_LARGE_FONT_DATA = IConstant.KEY_BASE + ".gcode.large.fontdata";

}
