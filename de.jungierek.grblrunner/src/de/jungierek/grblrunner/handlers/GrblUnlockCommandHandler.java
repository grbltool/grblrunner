package de.jungierek.grblrunner.handlers;

public class GrblUnlockCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$X";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
