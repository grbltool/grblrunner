package de.jungierek.grblrunner.constants;


public interface IPersistenceKey {

    // TODO separate persistenc and context keys

    public final static String GCODE_PATH = IConstant.KEY_BASE + ".GCODE_PATH";

    public static final String LAST_COORDINATE_SYSTEM = IConstant.KEY_BASE + ".LAST_COORDINATE_SYSTEM";

    public static final String VIEW_SCALE = IConstant.KEY_BASE + ".VIEW_SCALE";
    public static final Object VIEW_PIXEL_SHIFT = IConstant.KEY_BASE + ".VIEW_PIXEL_SHIFT_"; // append axis
    public static final Object VIEW_ROTATION = IConstant.KEY_BASE + ".VIEW_ROTATION_"; // append axis

    public static final String AUTO_CONNECT = IConstant.KEY_BASE + ".AUTO_CONNECT";
    public static final String AUTO_CONNECT_PORT = IConstant.KEY_BASE + ".AUTO_CONNECT_PORT";
    public static final String AUTO_CONNECT_ON = "x";

    public static final String EDITOR_PATH = IConstant.KEY_BASE + ".EDITOR_PATH";

    public static final String TERMINAL_GRBL_STATE = IConstant.KEY_BASE + ".TERMINAL_GRBL_STATE";
    public static final String TERMINAL_GRBL_MODES = IConstant.KEY_BASE + ".TERMINAL_GRBL_MODES";
    public static final String TERMINAL_SUPPRESS_LINES = IConstant.KEY_BASE + ".TERMINAL_GRBL_SUPPRESS_LINES";

}
