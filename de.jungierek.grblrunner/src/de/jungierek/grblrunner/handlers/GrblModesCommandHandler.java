package de.jungierek.grblrunner.handlers;

public class GrblModesCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$G";

    }

    @Override
    protected boolean isSuppressLines () {

        return false;

    }

}
