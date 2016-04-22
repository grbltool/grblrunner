package de.jungierek.grblrunner.handler;

public class SpindleStartCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "M3";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
