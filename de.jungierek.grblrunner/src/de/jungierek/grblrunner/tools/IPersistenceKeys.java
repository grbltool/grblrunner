package de.jungierek.grblrunner.tools;

public interface IPersistenceKeys {

    public final static String KEY_GCODE_PATH = "GCODE_PATH";

    public static final String KEY_PART_COLS = "part.cols";
    public static final String KEY_PART_GROUP_COLS = "part.group.cols";
    public static final String KEY_PART_GROUP_ROWS = "part.group.rows";
    public static final String KEY_PART_GROUP_ORIENTATION = "part.group.orientation";

    public static final String KEY_LAST_COORDINATE_SYSTEM = "LAST_COORDINATE_SYSTEM";

    public static final String KEY_VIEW_SCALE = "VIEW_SCALE";
    public static final Object KEY_VIEW_PIXEL_SHIFT = "VIEW_PIXEL_SHIFT_"; // append axis
    public static final Object KEY_VIEW_ROTATION = "VIEW_ROTATION_"; // append axis

    public static final String KEY_AUTO_CONNECT = "AUTO_CONNECT";
    public static final String KEY_AUTO_CONNECT_PORT = "AUTO_CONNECT_PORT";
    public static final String AUTO_CONNECT_ON = "x";

}
