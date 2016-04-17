package de.jungierek.grblrunner.handler;

public class GrblSpindleStartCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "M3";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
