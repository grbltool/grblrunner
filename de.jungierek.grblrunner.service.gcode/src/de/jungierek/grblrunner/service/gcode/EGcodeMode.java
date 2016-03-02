package de.jungierek.grblrunner.service.gcode;

public enum EGcodeMode {
    
    /* @formatter:off */
    METRIC_MODE_INCH    ( "G20" ), 
    METRIC_MODE_MM      ( "G21" ),
    MOTION_MODE_PROBE   ( "G38" ),

    MOTION_MODE_LINEAR  ( "G01", "G1" ),
    MOTION_MODE_CW_ARC  ( "G02", "G2" ),
    MOTION_MODE_CCW_ARC ( "G03", "G3" ),
    MOTION_MODE_SEEK    ( "G00", "G0" ), // must be here to prevent failures
    MOTION_MODE_NONE    ( "G80" ),
    COMMENT             ( "(", ";" ),
    SPINDLE_ON_CW       ( "M03", "M3" ),
    SPINDLE_ON_CCW      ( "M04", "M4" ),
    SPINDLE_OFF         ( "M05", "M5" ),
    DISTANCE_MODE_ABS   ( "G90" ),
    DISTANCE_MODE_REL   ( "G91" ),
    WAIT                ( "G04", "G4" ),
    
    GCODE_MODE_UNDEF ( "" )
    ;
    /* @formatter:on */
    
    private final String command;
    private final String command_alt;
    
    private EGcodeMode ( String command ) {
        
        this.command = command;
        this.command_alt = null;
        
    }
    
    private EGcodeMode ( String command, String command_alt ) {
        
        this.command = command;
        this.command_alt = command_alt;
        
    }
    
    public static EGcodeMode identify ( String line ) {
        
        if ( line == null ) return MOTION_MODE_NONE;
        
        for ( EGcodeMode mode : values () ) {
            if ( line.startsWith ( mode.command ) ) return mode;
            if ( mode.command_alt != null && line.startsWith ( mode.command_alt ) ) return mode;
        }
        
        return GCODE_MODE_UNDEF;
        
    }
    
    public String getCommand () {
        
        if ( command_alt != null ) return command_alt;
        return command;
        
    }
    
    public boolean isMotionMode () {
        
        return isMotionModeSeek () || isMotionModeLinear ();
                
    }

    public boolean isMotionModeLinear () {

        return this == EGcodeMode.MOTION_MODE_LINEAR;

    }

    public boolean isMotionModeSeek () {

        return this == EGcodeMode.MOTION_MODE_SEEK;

    }

    public boolean isArcMode () {

        return this == EGcodeMode.MOTION_MODE_CW_ARC || this == EGcodeMode.MOTION_MODE_CCW_ARC;

    }

}
