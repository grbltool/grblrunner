package de.jungierek.grblrunner.handler;

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
