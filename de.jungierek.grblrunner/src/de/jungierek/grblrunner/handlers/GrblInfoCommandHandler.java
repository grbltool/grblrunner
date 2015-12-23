package de.jungierek.grblrunner.handlers;

public class GrblInfoCommandHandler extends GrblCommandHandler {

    @Override
    protected String getCommand () {

        return "$I";

    }

    @Override
    protected boolean isSuppressLines () {

        return false;

    }

}
