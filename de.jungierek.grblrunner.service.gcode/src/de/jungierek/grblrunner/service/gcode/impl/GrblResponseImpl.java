package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.service.gcode.IGrblResponse;

public class GrblResponseImpl extends GrblMessage implements IGrblResponse {

    protected GrblResponseImpl ( boolean suppressInTerminal, String line ) {

        super ( suppressInTerminal, line );

    }

    @Override
    protected String getToStringName () {

        return "GrblResponse";

    }

}
