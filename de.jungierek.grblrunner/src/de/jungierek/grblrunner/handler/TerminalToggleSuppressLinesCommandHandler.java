package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.part.TerminalPart;

public class TerminalToggleSuppressLinesCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( TerminalToggleSuppressLinesCommandHandler.class );

    @Execute
    @Optional
    public void execute ( MPart part ) {

        LOG.debug ( "execute: part=" + part );

        if ( part != null ) ((TerminalPart) part.getObject ()).toggleSuppressLines ();

    }

}
