package de.jungierek.grblrunner.handler;

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
