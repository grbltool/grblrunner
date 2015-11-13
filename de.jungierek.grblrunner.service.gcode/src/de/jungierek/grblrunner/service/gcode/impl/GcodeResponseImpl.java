package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.service.gcode.IGcodeResponse;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class GcodeResponseImpl implements IGcodeResponse {
    
    // TODO implment equals

    public final boolean suppressInTerminal;
    public final String line;
    
    public GcodeResponseImpl ( boolean suppressInTerminal, String line ) {
        
        this.suppressInTerminal = suppressInTerminal;
        this.line = line;
        
    }

    @Override
    public boolean suppressInTerminal () {
        
        return suppressInTerminal;
        
    }

    @Override
    public String getLine () {
        
        return line;
        
    }
    
    @Override
    public boolean isReset () {
        
        return line != null && line.length () == 1 && line.charAt ( 0 ) == IGcodeService.GRBL_RESET_CODE;
        
    }

    public boolean isHome () {

        return "$H".equals ( line );

    }

    public boolean isUnlock () {

        return "$X".equals ( line );

    }

    @Override
    public String toString () {

        String result = "Gcode-Response[" + line;
        if ( suppressInTerminal ) result += ",suppress";
        result += "]";
        
        return result;
        
    }
    
}

