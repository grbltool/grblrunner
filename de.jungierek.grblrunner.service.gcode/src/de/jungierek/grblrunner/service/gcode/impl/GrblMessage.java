package de.jungierek.grblrunner.service.gcode.impl;

public abstract class GrblMessage {

    public final boolean suppressInTerminal;
    public final String message;

    protected GrblMessage ( boolean suppressInTerminal, String message ) {

        this.suppressInTerminal = suppressInTerminal;
        this.message = message;

    }

    public boolean isSuppressInTerminal () {

        return suppressInTerminal;

    }

    public String getMessage () {

        return message;

    }
    
    protected abstract String getToStringName ();
    
    @Override
    public String toString () {

        String result = getToStringName () + "[" + message;
        if ( suppressInTerminal ) result += ",suppress";
        result += "]";

        return result;

    }



}
