package de.jungierek.grblrunner.constants;

public interface IEvents {
    
    // *********************************************************************************************

    public static final String SEPARATOR = "/";

    // *********************************************************************************************

    public static final String BASE = "de/jungierek/grblrunner";
    
    public static final String ALL = BASE + SEPARATOR + "*";

    // *********************************************************************************************

    public static final String SERIAL_BASE = BASE + SEPARATOR + "serial";
    public static final String SERIAL_ALL = SERIAL_BASE + SEPARATOR + "*";

    public static final String SERIAL_CONNECTED = SERIAL_BASE + SEPARATOR + "connected";
    public static final String SERIAL_DISCONNECTED = SERIAL_BASE + SEPARATOR + "disconnected";

    public static final String SERIAL_PORTS_BASE = SERIAL_BASE + SEPARATOR + "ports";
    public static final String SERIAL_PORTS_ALL = SERIAL_PORTS_BASE + SEPARATOR + "*";

    public static final String SERIAL_PORTS_DETECTING = SERIAL_PORTS_BASE + SEPARATOR + "detecting";
    public static final String SERIAL_PORTS_DETECTED = SERIAL_PORTS_BASE + SEPARATOR + "detected";
    public static final String SERIAL_PORT_SELECTED = SERIAL_PORTS_BASE + SEPARATOR + "selected";

    // *********************************************************************************************

    // rename to grbl
    public static final String GRBL_BASE = BASE + SEPARATOR + "grbl";
    public static final String GRBL_ALL = GRBL_BASE + SEPARATOR + "*";

    public static final String GRBL_ALARM = GRBL_BASE + SEPARATOR + "alarm";
    public static final String GRBL_RESTARTED = GRBL_BASE + SEPARATOR + "restarted";
    public static final String GRBL_SENT = GRBL_BASE + SEPARATOR + "sent";
    public static final String GRBL_RECEIVED = GRBL_BASE + SEPARATOR + "received";

    // *********************************************************************************************

    public static final String PLAYER_BASE = BASE + SEPARATOR + "player";
    public static final String PLAYER_ALL = PLAYER_BASE + SEPARATOR + "*";

    public static final String PLAYER_START = PLAYER_BASE + SEPARATOR + "start";
    public static final String PLAYER_STOP = PLAYER_BASE + SEPARATOR + "stop";
    public static final String PLAYER_LINE = PLAYER_BASE + SEPARATOR + "line";
    public static final String PLAYER_SEGMENT = PLAYER_BASE + SEPARATOR + "segment";

    // *********************************************************************************************

    public static final String AUTOLEVEL_BASE = BASE + SEPARATOR + "autolevel";
    public static final String AUTOLEVEL_ALL = AUTOLEVEL_BASE + SEPARATOR + "*";

    public static final String AUTOLEVEL_START = AUTOLEVEL_BASE + SEPARATOR + "start";
    public static final String AUTOLEVEL_STOP = AUTOLEVEL_BASE + SEPARATOR + "stop";
    public static final String AUTOLEVEL_UPDATE = AUTOLEVEL_BASE + SEPARATOR + "update"; // TODO prove this moving to update section
    public static final String AUTOLEVEL_DATA_SAVED = AUTOLEVEL_BASE + SEPARATOR + "saved";
    public static final String AUTOLEVEL_DATA_LOADED = AUTOLEVEL_BASE + SEPARATOR + "saved";
    public static final String AUTOLEVEL_DATA_CLEARED = AUTOLEVEL_BASE + SEPARATOR + "cleared";

    // *********************************************************************************************

    public static final String UPDATE_BASE = BASE + SEPARATOR + "update";
    public static final String UPDATE_ALL = UPDATE_BASE + SEPARATOR + "*";

    public static final String UPDATE_STATE = UPDATE_BASE + SEPARATOR + "state";
    public static final String UPDATE_MODAL_MODE = UPDATE_BASE + SEPARATOR + "motionmode";
    public static final String UPDATE_FIXTURE = UPDATE_BASE + SEPARATOR + "fixture";
    public static final String UPDATE_FIXTURE_OFFSET = UPDATE_BASE + SEPARATOR + "fixture" + SEPARATOR + "offset";
    public static final String UPDATE_PLANE = UPDATE_BASE + SEPARATOR + "plane";
    public static final String UPDATE_METRIC_MODE = UPDATE_BASE + SEPARATOR + "metrixmode";
    public static final String UPDATE_TOOL = UPDATE_BASE + SEPARATOR + "tool";
    public static final String UPDATE_SPINDLE_MODE = UPDATE_BASE + SEPARATOR + "spindlemode";
    public static final String UPDATE_COOLANT_MODE = UPDATE_BASE + SEPARATOR + "coolanrmode";
    public static final String UPDATE_FEEDRATE = UPDATE_BASE + SEPARATOR + "feedrate";
    public static final String UPDATE_SPINDLESPEED = UPDATE_BASE + SEPARATOR + "spindlespeed";
    public static final String UPDATE_DISTANCE_MODE = UPDATE_BASE + SEPARATOR + "distancemode";

    // *********************************************************************************************

    public static final String REDRAW_BASE = BASE + SEPARATOR + "redraw";
    public static final String REDRAW_ALL = REDRAW_BASE + SEPARATOR + "*";

    public static final String REDRAW = REDRAW_BASE;

    // *********************************************************************************************

    public static final String GCODE_BASE = BASE + SEPARATOR + "gcode";
    public static final String GCODE_ALL = GCODE_BASE + SEPARATOR + "*";

    public static final String GCODE_PROGRAM_LOADED = GCODE_BASE + SEPARATOR + "loaded";
    public static final String GCODE_MACRO_GENERATED = GCODE_BASE + SEPARATOR + "generated";
    public static final String GCODE_CLOSED = GCODE_BASE + SEPARATOR + "closed";

    // *********************************************************************************************

    public static final String MESSAGE_BASE = BASE + SEPARATOR + "message";
    public static final String MESSAGE_ALL = MESSAGE_BASE + SEPARATOR + "*";

    public static final String MESSAGE_ERROR = MESSAGE_BASE + SEPARATOR + "error";
    public static final String MESSAGE_CONFIRM = MESSAGE_BASE + SEPARATOR + "confirm";
    public static final String MESSAGE_INFO = MESSAGE_BASE + SEPARATOR + "info";
    public static final String MESSAGE_QUESTION = MESSAGE_BASE + SEPARATOR + "question";

    // *********************************************************************************************

}
