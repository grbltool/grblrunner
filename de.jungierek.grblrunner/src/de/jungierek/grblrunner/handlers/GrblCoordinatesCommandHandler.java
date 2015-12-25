package de.jungierek.grblrunner.handlers;

public class GrblCoordinatesCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$#";

    }

    @Override
    protected boolean isSuppressLines () {

        return false;

    }

}
