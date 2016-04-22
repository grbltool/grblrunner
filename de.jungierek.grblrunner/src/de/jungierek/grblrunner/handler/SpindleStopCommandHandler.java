package de.jungierek.grblrunner.handler;

public class SpindleStopCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "M5";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
