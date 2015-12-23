package de.jungierek.grblrunner.handlers;

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
