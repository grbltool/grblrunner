package de.jungierek.grblrunner.service.gcode;


public enum EGrblState {

    // @formatter:off
    IDLE ( "<Idle" ),
    RUN ( "<Run" ),
    HOLD ( "<Hold" ),
    JOG ( "<Jog" ),
    ALARM ( "<Alarm" ),
    DOOR ( "Door" ),
    CHECK ( "<Check" ),
    HOME ( "<Home" ),
    SLEEP ( "<Sleep" ),
    GRBL_STATE_UNDEF ( null ),
    ;
    // @formatter:on
    
    private final  String idText;
    
    private EGrblState ( String idText ) {
        
        this.idText = idText;
        
    }
    
    public static EGrblState identify ( String line ) {
        
        if ( line == null ) return GRBL_STATE_UNDEF;
        
        for ( EGrblState state : values () ) {
            if ( line.startsWith ( state.idText ) ) return state;
        }
        
        return GRBL_STATE_UNDEF;
        
    }
    
    public String getText () {
        
        if ( idText == null ) return null;
        
        return idText.substring ( 1 );
    
    }

}
