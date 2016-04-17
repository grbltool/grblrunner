package de.jungierek.grblrunner.handler;

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
