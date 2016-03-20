package de.jungierek.grblrunner.constants;


public interface IPreferenceKey {

    // --- Mill ---
    public static final String GCODE_PATH = IConstants.KEY_BASE + ".gcode.path";
    public static final String BAUDRATE = IConstants.KEY_BASE + ".baudrate";
    public static final String WORK_AREA_MAX_X = IConstants.KEY_BASE + ".workarea.max.x";
    public static final String WORK_AREA_MAX_Y = IConstants.KEY_BASE + ".workarea.max.y";
    public static final String SPINDLE_MIN = IConstants.KEY_BASE + ".spindle.min";
    public static final String SPINDLE_MAX = IConstants.KEY_BASE + ".spindle.max";
    public static final String MAX_SEEK_FEEDRATE = IConstants.KEY_BASE + ".seekrate.max";
    public static final String ACCELARATION = IConstants.KEY_BASE + ".accelaration";
    public static final String AVG_SEEK_FEEDRATE_CORRECTOR = IConstants.KEY_BASE + ".seekrate.avg.corrector";
    public static final String Z_CLEARANCE = IConstants.KEY_BASE + ".clearance.z";

    // --- GcodeViewGroup ---
    public static final String FIT_TO_SIZE_MARGIN = IConstants.KEY_BASE + ".fittosize.margin";
    public static final String FIT_TO_SIZE_WITH_Z = IConstants.KEY_BASE + ".fittosize.withz";

    public static final String COLOR_AUTOLEVEL_GRID = IConstants.KEY_BASE + ".autolevel.grid.color";
    public static final String COLOR_GANTRY = IConstants.KEY_BASE + ".gantry.color";
    public static final String COLOR_PROCESSED = IConstants.KEY_BASE + ".processed.color";
    public static final String COLOR_SEEK = IConstants.KEY_BASE + ".seek.color";
    public static final String COLOR_LINEAR = IConstants.KEY_BASE + ".linear.color";
    public static final String COLOR_ARC = IConstants.KEY_BASE + ".arc.color";
    public static final String COLOR_PROBE = IConstants.KEY_BASE + ".probe.color";
    public static final String COLOR_MACHINE_ORIGIN = IConstants.KEY_BASE + ".origin.machine.color";
    public static final String COLOR_WORK_ORIGIN = IConstants.KEY_BASE + ".origin.work.color";
    public static final String COLOR_WORKAREA_BORDER = IConstants.KEY_BASE + ".workarea.border.color";
    public static final String COLOR_WORKAREA_MIDCROSS = IConstants.KEY_BASE + ".workarea.midcross.color";

    // --- SerialPart ---
    public static final String COLOR_CONNECT = IConstants.KEY_BASE + ".connect.color";
    public static final String COLOR_DISCONNECT = IConstants.KEY_BASE + ".disconnect.color";

    // --- TerminalPart ---
    public static final String COLOR_TERMINAL_FOREGROUND = IConstants.KEY_BASE + ".terminal.foreground.color";
    public static final String COLOR_TERMINAL_BACKGROUND = IConstants.KEY_BASE + ".terminal.background.color";
    public static final String COLOR_ALARM_FOREGROUND = IConstants.KEY_BASE + ".alarm.foreground.color";
    public static final String COLOR_ALARM_BACKGROUND = IConstants.KEY_BASE + ".alarm.background.color";
    public static final String COLOR_TIMESTAMP_BACKGROUND = IConstants.KEY_BASE + ".timestamp.background.color";
    public static final String COLOR_SUPPRESSED_LINE_FOREGROUND = IConstants.KEY_BASE + ".suppressed.foreground.color";
    public static final String COLOR_OK_FOREGROUND = IConstants.KEY_BASE + ".ok.foreground.color";
    public static final String COLOR_ERROR_FOREGROUND = IConstants.KEY_BASE + ".error.foreground.color";
    public static final String COLOR_GRBL_FOREGROUND = IConstants.KEY_BASE + ".grbl.foreground.color";
    public static final String COLOR_GRBL_BACKGROUND = IConstants.KEY_BASE + ".grbl.background.color";
    public static final String COLOR_OK_SUPPRESSED_FOREGROUND = IConstants.KEY_BASE + ".ok.suppressed.foreground.color";
    public static final String COLOR_ERROR_SUPPRESSED_FOREGROUND = IConstants.KEY_BASE + ".error.suppressed.foreground.color";
    public static final String COLOR_GRBL_SUPPRESSED_FOREGROUND = IConstants.KEY_BASE + ".grbl.suppressed.foreground.color";
    public static final String COLOR_GRBL_SUPPRESSED_BACKGROUND = IConstants.KEY_BASE + ".grbl.suppressed.background.color";
    public static final String TERMINAL_FONT_DATA = IConstants.KEY_BASE + ".terminal.fontdata";

    // --- Probe/Autolevel ---
    public static final String PROBE_FEEDRATE = IConstants.KEY_BASE + ".probe.feedrate";
    public static final String PROBE_DEPTH = IConstants.KEY_BASE + ".probe.depth";
    public static final String PROBE_WITH_ERROR = IConstants.KEY_BASE + ".probe.witherror";
    public static final String PROBE_Z_MAX = IConstants.KEY_BASE + ".probe.zmax";

}
