package de.jungierek.grblrunner.handlers;

public class GrblToolChangeHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "G30";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
