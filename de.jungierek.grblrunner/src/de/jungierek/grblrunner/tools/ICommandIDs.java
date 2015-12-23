package de.jungierek.grblrunner.tools;

public interface ICommandIDs {

    public static final String COMMAND_GCODE_LOAD = "de.jungierek.grblrunner.command.gcode.load";
    public static final String COMMAND_GCODE_REFRESH = "de.jungierek.grblrunner.command.gcode.refresh";
    public static final String COMMAND_GCODE_PLAY = "de.jungierek.grblrunner.command.gcode.play";

    public static final String COMMAND_GCODE_$ = "de.jungierek.grblrunner.command.gcode.$";
    public static final String COMMAND_GCODE_$$ = "de.jungierek.grblrunner.command.gcode.$$";
    public static final String COMMAND_GCODE_$sharp = "de.jungierek.grblrunner.command.gcode.$#";
    public static final String COMMAND_GCODE_$G = "de.jungierek.grblrunner.command.gcode.$G";
    public static final String COMMAND_GCODE_$I = "de.jungierek.grblrunner.command.gcode.$I";
    public static final String COMMAND_GCODE_$N = "de.jungierek.grblrunner.command.gcode.$N";

    public static final String COMMAND_CYCLE_START = "de.jungierek.grblrunner.command.cycle.start";
    public static final String COMMAND_CYCLE_PAUSE = "de.jungierek.grblrunner.command.cycle.pause";
    public static final String COMMAND_CYCLE_RESET = "de.jungierek.grblrunner.command.cycle.reset";

    public static final String COMMAND_AUTOLEVEL_SAVE = "de.jungierek.grblrunner.command.autolevel.save";
    public static final String COMMAND_AUTOLEVEL_LOAD = "de.jungierek.grblrunner.command.autolevel.load";
    public static final String COMMAND_AUTOLEVEL_CLEAR = "de.jungierek.grblrunner.command.autolevel.clear";

    public static final String COMMAND_SERIAL_CONNECT = "de.jungierek.grblrunner.command.serial.connect";
    public static final String COMMAND_SERIAL_DISCONNECT = "de.jungierek.grblrunner.command.serial.disconnect";
    public static final String COMMAND_SERIAL_UPDATE = "de.jungierek.grblrunner.command.serial.update";
    public static final String COMMAND_SERIAL_SELECT_PORT = "de.jungierek.grblrunner.command.serial.port";
    public final static String PARAMETER_SERIAL_SELECT_PORT = "de.jungierek.grblrunner.commandparameter.serial.port";

    public static final String COMMAND_GRBL_HOME = "de.jungierek.grblrunner.command.grbl.home";
    public static final String COMMAND_GRBL_UNLOCK = "de.jungierek.grblrunner.command.grbl.unlock";
    public static final String COMMAND_GRBL_CHECK = "de.jungierek.grblrunner.command.grbl.check";
    public static final String COMMAND_GRBL_HELP = "de.jungierek.grblrunner.command.grbl.help";
    public static final String COMMAND_GRBL_SETTINGS = "de.jungierek.grblrunner.command.grbl.settings";
    public static final String COMMAND_GRBL_COORIDNATES = "de.jungierek.grblrunner.command.grbl.coordinates";
    public static final String COMMAND_GRBL_MODES = "de.jungierek.grblrunner.command.grbl.modes";
    public static final String COMMAND_GRBL_INFO = "de.jungierek.grblrunner.command.grbl.info";
    public static final String COMMAND_GRBL_STARTUP = "de.jungierek.grblrunner.command.grbl.startup";

}
