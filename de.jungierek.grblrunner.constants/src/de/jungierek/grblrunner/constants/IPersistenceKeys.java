package de.jungierek.grblrunner.constants;


public interface IPersistenceKeys {

    // TODO separate persistenc and context keys

    public final static String GCODE_PATH = IConstants.KEY_BASE + ".GCODE_PATH";

    public static final String LAST_COORDINATE_SYSTEM = IConstants.KEY_BASE + ".LAST_COORDINATE_SYSTEM";

    public static final String VIEW_SCALE = IConstants.KEY_BASE + ".VIEW_SCALE";
    public static final Object VIEW_PIXEL_SHIFT = IConstants.KEY_BASE + ".VIEW_PIXEL_SHIFT_"; // append axis
    public static final Object VIEW_ROTATION = IConstants.KEY_BASE + ".VIEW_ROTATION_"; // append axis

    public static final String AUTO_CONNECT = IConstants.KEY_BASE + ".AUTO_CONNECT";
    public static final String AUTO_CONNECT_PORT = IConstants.KEY_BASE + ".AUTO_CONNECT_PORT";
    public static final String AUTO_CONNECT_ON = "x";

    public static final String EDITOR_PATH = IConstants.KEY_BASE + ".EDITOR_PATH";

    public static final String TERMINAL_GRBL_STATE = IConstants.KEY_BASE + ".TERMINAL_GRBL_STATE";
    public static final String TERMINAL_GRBL_MODES = IConstants.KEY_BASE + ".TERMINAL_GRBL_MODES";
    public static final String TERMINAL_SUPPRESS_LINES = IConstants.KEY_BASE + ".TERMINAL_GRBL_SUPPRESS_LINES";

}
