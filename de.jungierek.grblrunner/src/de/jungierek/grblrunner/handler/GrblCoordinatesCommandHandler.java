package de.jungierek.grblrunner.handler;

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
