package de.jungierek.grblrunner.handlers;

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
