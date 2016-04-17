package de.jungierek.grblrunner.handler;

public class GrblStartupCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$N";

    }

    @Override
    protected boolean isSuppressLines () {

        return false;

    }

}
