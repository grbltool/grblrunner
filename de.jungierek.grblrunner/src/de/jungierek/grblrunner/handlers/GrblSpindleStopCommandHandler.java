package de.jungierek.grblrunner.handlers;

public class GrblSpindleStopCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "M5";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
