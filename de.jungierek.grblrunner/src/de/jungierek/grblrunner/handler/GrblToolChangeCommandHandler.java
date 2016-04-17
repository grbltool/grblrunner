package de.jungierek.grblrunner.handler;

public class GrblToolChangeCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "G30";

    }

    @Override
    protected boolean isSuppressLines () {

        return true;

    }

}
