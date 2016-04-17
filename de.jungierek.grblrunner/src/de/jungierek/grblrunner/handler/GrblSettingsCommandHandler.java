package de.jungierek.grblrunner.handler;

public class GrblSettingsCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$$";
    }

    @Override
    protected boolean isSuppressLines () {

        return false;

    }

}
