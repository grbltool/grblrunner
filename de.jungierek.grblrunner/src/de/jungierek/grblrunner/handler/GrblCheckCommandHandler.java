package de.jungierek.grblrunner.handler;

public class GrblCheckCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$C";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
