package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.service.gcode.IGrblRequest;

public class GrblRequestImpl extends GrblMessage implements IGrblRequest {

    protected GrblRequestImpl ( boolean suppressInTerminal, String line ) {

        super ( suppressInTerminal, line );

    }

    @Override
    public boolean isReset () {

        return message != null && message.length () == 1 && message.charAt ( 0 ) == IConstant.GRBL_RESET_CODE;

    }

    @Override
    public boolean isHome () {

        return "$H".equals ( message );

    }

    @Override
    public boolean isUnlock () {

        return "$X".equals ( message );

    }

    @Override
    protected String getToStringName () {

        return "GrblRequest";

    }

}
