package de.jungierek.grblrunner.tools;

public interface IEvents {
    
    public static final String BASE = "de/jungierek/grblrunner";

    public static final String ALL = BASE + "/*";

    // *********************************************************************************************

    public static final String SERIAL_BASE = BASE + "/serial";
    public static final String SERIAL_ALL = SERIAL_BASE + "/*";

    public static final String EVENT_SERIAL_PORTS_DETECTING = "TOPIC_SERIAL/PORTS_DETECTING";
    public static final String EVENT_SERIAL_PORTS_DETECTED = "TOPIC_SERIAL/PORTS_DETECTED";
    public static final String EVENT_SERIAL_PORT_SELECTED = "TOPIC_SERIAL/PORTS_SELECTED";
    public static final String EVENT_SERIAL_CONNECTED = "TOPIC_SERIAL/CONNECTED";
    public static final String EVENT_SERIAL_DISCONNECTED = "TOPIC_SERIAL/DISCONNECTED";

    // *********************************************************************************************

    public static final String GCODE_BASE = BASE + "/gcode";
    public static final String GCODE_ALL = GCODE_BASE + "/*";

    public static final String EVENT_GCODE_ALARM = "TOPIC_GCODE/ALARM";
    public static final String EVENT_GCODE_GRBL_RESTARTED = "TOPIC_GCODE/GRBL_RESTARTED";
    public static final String EVENT_GCODE_SENT = "TOPIC_GCODE/SENT";
    public static final String EVENT_GCODE_RECEIVED = "TOPIC_GCODE/RECEIVED";

    // *********************************************************************************************

    public static final String GCODE_PLAYER_BASE = GCODE_BASE + "/player";
    public static final String GCODE_PLAYER_ALL = GCODE_PLAYER_BASE + "/*";

    public static final String EVENT_GCODE_PLAYER_LOADED = "TOPIC_GCODE/PLAYER_LOADED";
    public static final String EVENT_GCODE_PLAYER_START = "TOPIC_GCODE/PLAYER_START";
    public static final String EVENT_GCODE_PLAYER_STOP = "TOPIC_GCODE/PLAYER_STOP";
    public static final String EVENT_GCODE_PLAYER_LINE = "TOPIC_GCODE/PLAYER_LINE";
    public static final String EVENT_GCODE_PLAYER_LINE_SEGMENT = "TOPIC_GCODE/PLAYER_SEGMENT";

    // *********************************************************************************************

    public static final String GCODE_PROBE_BASE = GCODE_BASE + "/probes";
    public static final String GCODE_PROBE_ALL = GCODE_PROBE_BASE + "/*";

    public static final String EVENT_GCODE_SCAN_START = "TOPIC_GCODE/SCAN_START";
    public static final String EVENT_GCODE_SCAN_STOP = "TOPIC_GCODE/SCAN_STOP";
    public static final String EVENT_PROBE_UPDATE = "TOPIC_GCODE/UPDATE_PROBE";
    public static final String EVENT_PROBE_DATA_SAVED = "TOPIC_GCODE/PROBE_DATA_SAVED";
    public static final String EVENT_PROBE_DATA_LOADED = "TOPIC_GCODE/PROBE_DATA_LOADED";
    public static final String EVENT_PROBE_DATA_CLEARED = "TOPIC_GCODE/PROBE_DATA_CLEARED";

    // *********************************************************************************************

    public static final String GCODE_UPDATE_BASE = GCODE_BASE + "/update";
    public static final String GCODE_UPDATE_ALL = GCODE_UPDATE_BASE + "/*";

    public static final String EVENT_GCODE_UPDATE_STATE = "TOPIC_GCODE/UPDATE_STATE";
    public static final String EVENT_GCODE_UPDATE_MOTION_MODE = "TOPIC_GCODE/UPDATE_MOTION_MODE";
    public static final String EVENT_GCODE_UPDATE_COORD_SELECT = "TOPIC_GCODE/UPDATE_COORD_SELECT";
    public static final String EVENT_GCODE_UPDATE_COORD_SELECT_OFFSET = "TOPIC_GCODE/UPDATE_COORD_SELECT_OFFSET";
    public static final String EVENT_GCODE_UPDATE_PLANE = "TOPIC_GCODE/UPDATE_PLANE";
    public static final String EVENT_GCODE_UPDATE_METRIC_MODE = "TOPIC_GCODE/UPDATE_METRIC_MODE";
    public static final String EVENT_GCODE_UPDATE_TOOL = "TOPIC_GCODE/UPDATE_TOOL";
    public static final String EVENT_GCODE_UPDATE_SPINDLE_MODE = "TOPIC_GCODE/UPDATE_SPINDLE_MODE";
    public static final String EVENT_GCODE_UPDATE_COOLANT_MODE = "TOPIC_GCODE/UPDATE_COOLANT_MODE";
    public static final String EVENT_GCODE_UPDATE_FEEDRATE = "TOPIC_GCODE/UPDATE_FEEDRATE";
    public static final String EVENT_GCODE_UPDATE_SPINDLESPEED = "TOPIC_GCODE/UPDATE_SPINDLESPEED";
    public static final String EVENT_GCODE_UPDATE_DISTANCE_MODE = "TOPIC_GCODE/UPDATE_DISTANCE_MODE";

    // *********************************************************************************************

    public static final String REDRAW_BASE = BASE + "/redraw";
    public static final String REDRAW_ALL = REDRAW_BASE + "/*";

    public static final String REDRAW = REDRAW_BASE;

    // *********************************************************************************************

    public static final String GCODE_MSG_BASE = GCODE_BASE + "/message";
    public static final String GCODE_MSG_ALL = GCODE_UPDATE_BASE + "/*";

    public static final String EVENT_MSG_ERROR = "TOPIC_MSG/ERROR";
    public static final String EVENT_MSG_CONFIRM = "TOPIC_MSG/CONFIRM";
    public static final String EVENT_MSG_INFO = "TOPIC_MSG/INFO";
    public static final String EVENT_MSG_QUESTION = "TOPIC_MSG/QUESTION";

    // *********************************************************************************************

    public static final String PROGRESS_BASE = BASE + "/progress";
    public static final String PROGRESS_ALL = PROGRESS_BASE + "/*";

    public static final String EVENT_PROGRESS_ALL = "TOPIC_PROGRESS/*";
    public static final String EVENT_PROGRESS_TOTAL_TICKS = "TOPIC_PROGRESS/TOTAL_TICKS";
    public static final String EVENT_PROGRESS_TICK = "TOPIC_PROGRESS/TICK";

    // *********************************************************************************************

}
