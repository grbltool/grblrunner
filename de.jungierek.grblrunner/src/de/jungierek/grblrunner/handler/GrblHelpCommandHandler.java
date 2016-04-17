package de.jungierek.grblrunner.handler;

public class GrblHelpCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$";

    }

    @Override
    protected boolean isSuppressLines () {

        return false;

    }

}
