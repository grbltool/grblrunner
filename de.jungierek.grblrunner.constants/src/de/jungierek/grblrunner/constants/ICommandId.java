package de.jungierek.grblrunner.constants;


public interface ICommandId {

    public static final String GCODE_LOAD = "de.jungierek.grblrunner.command.gcode.load";
    public static final String GCODE_REFRESH = "de.jungierek.grblrunner.command.gcode.refresh";
    public static final String GCODE_PLAY = "de.jungierek.grblrunner.command.gcode.play";

    public static final String CYCLE_START = "de.jungierek.grblrunner.command.cycle.start";
    public static final String CYCLE_PAUSE = "de.jungierek.grblrunner.command.cycle.pause";
    public static final String CYCLE_RESET = "de.jungierek.grblrunner.command.cycle.reset";

    public static final String AUTOLEVEL_SAVE = "de.jungierek.grblrunner.command.autolevel.save";
    public static final String AUTOLEVEL_LOAD = "de.jungierek.grblrunner.command.autolevel.load";
    public static final String AUTOLEVEL_CLEAR = "de.jungierek.grblrunner.command.autolevel.clear";
    public static final String AUTOLEVEL_SCAN = "de.jungierek.grblrunner.command.autolevel.scan";
    public static final String AUTOLEVEL_ZMIN_PARAMETER = "de.jungierek.grblrunner.commandparameter.autolevel.zmin";
    public static final String AUTOLEVEL_ZMAX_PARAMETER = "de.jungierek.grblrunner.commandparameter.autolevel.zmax";
    public static final String AUTOLEVEL_ZCLEARANCE_PARAMETER = "de.jungierek.grblrunner.commandparameter.autolevel.zclearance";
    public static final String AUTOLEVEL_PROBEFEEDRATE_PARAMETER = "de.jungierek.grblrunner.commandparameter.autolevel.probefeedrate";

    public static final String SERIAL_CONNECT = "de.jungierek.grblrunner.command.serial.connect";
    public static final String SERIAL_DISCONNECT = "de.jungierek.grblrunner.command.serial.disconnect";
    public static final String SERIAL_UPDATE = "de.jungierek.grblrunner.command.serial.update";
    public static final String SERIAL_SELECT_PORT = "de.jungierek.grblrunner.command.serial.port";
    public static final String SERIAL_SELECT_PORT_PARAMETER = "de.jungierek.grblrunner.commandparameter.serial.port";

    public static final String GRBL_HOME = "de.jungierek.grblrunner.command.grbl.home";
    public static final String GRBL_UNLOCK = "de.jungierek.grblrunner.command.grbl.unlock";
    public static final String GRBL_CHECK = "de.jungierek.grblrunner.command.grbl.check";
    public static final String GRBL_HELP = "de.jungierek.grblrunner.command.grbl.help";
    public static final String GRBL_SETTINGS = "de.jungierek.grblrunner.command.grbl.settings";
    public static final String GRBL_COORIDNATES = "de.jungierek.grblrunner.command.grbl.coordinates";
    public static final String GRBL_MODES = "de.jungierek.grblrunner.command.grbl.modes";
    public static final String GRBL_INFO = "de.jungierek.grblrunner.command.grbl.info";
    public static final String GRBL_STARTUP = "de.jungierek.grblrunner.command.grbl.startup";

    public static final String RESET_COORDINATE_OFFSET = "de.jungierek.grblrunner.command.coordinateoffset.reset";
    public static final String SET_COORDINATE_OFFSET = "de.jungierek.grblrunner.command.coordinateoffset.set";
    public static final String COORDINATE_OFFSET_PARAMETER = "de.jungierek.grblrunner.commandparameter.coordinateoffset.axis";

    public static final String GRBL_MOVE = "de.jungierek.grblrunner.command.grbl.move";
    public static final String GRBL_MOVE_AXIS_PARAMETER = "de.jungierek.grblrunner.commandparameter.grbl.move.axis";
    public static final String GRBL_MOVE_DIRECTION_PARAMETER = "de.jungierek.grblrunner.commandparameter.grbl.move.direction";
    public static final String GRBL_MOVE_DISTANCE_PARAMETER = "de.jungierek.grblrunner.commandparameter.grbl.move.distance";

    public static final String GRBL_MOVE_ZERO = "de.jungierek.grblrunner.command.grbl.movezero";
    public static final String GRBL_MOVE_ZERO_AXIS_PARAMETER = "de.jungierek.grblrunner.commandparameter.grbl.movezero.axis";

    public static final String SPINDLE_START = "de.jungierek.grblrunner.command.spindle.start";
    public static final String SPINDLE_STOP = "de.jungierek.grblrunner.command.spindle.stop";
    public static final String SPINDLE_SPEED = "de.jungierek.grblrunner.command.spindle.speed";
    public final static String SPINDLE_SPEED_PARAMETER = "de.jungierek.grblrunner.commandparameter.spindle.speed";

    public static final String PROBE_ACTION = "de.jungierek.grblrunner.command.probe";
    public static final String PROBE_ACTION_DEPTH_PARAMETER = "de.jungierek.grblrunner.commandparameter.probe.depth";

    public static final String SET_COORDINATE_SYSTEM = "de.jungierek.grblrunner.command.gcode.coordinate.system";
    public static final String SET_COORDINATE_SYSTEM_PARAMETER = "de.jungierek.grblrunner.commandparameter.gcode.coordinate.system";

    public final static String CAMERA_RESOLUTION = "de.jungierek.grblrunner.command.camera.resolution";
    public final static String CAMERA_RESOLUTION_PARAMETER = "de.jungierek.grblrunner.commandparameter.camera.resolution";

    public final static String PERSPECTIVE_SWITCH = "de.jungierek.grblrunner.command.perspective.switch";
    public final static String PERSPECTIVE_SWITCH_PARAMETER = "de.jungierek.grblrunner.commandparameter.perspective.switch";

    public final static String CAMERA_SELECT = "de.jungierek.grblrunner.command.camera.select";
    public final static String CAMERA_SELECT_PARAMETER = "de.jungierek.grblrunner.commandparameter.camera.select";

}
