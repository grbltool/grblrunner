package de.jungierek.grblrunner.handlers;

public class GrblHomeCommandHandleer extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$H";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
