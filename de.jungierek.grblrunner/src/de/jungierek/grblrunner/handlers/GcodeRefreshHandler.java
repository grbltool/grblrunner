package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeRefreshHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeRefreshHandler.class );

    @Execute
    public void execute ( @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "execute: program=" + gcodeProgram );

        gcodeProgram.loadGcodeProgram ( gcodeProgram.getGcodeProgramFile () );

    }

    @CanExecute
    public boolean canExecute ( @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeProgram.isPlaying () + " isscanning=" + gcodeProgram.isAutolevelScan () );

        return gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isPlaying () && !gcodeProgram.isAutolevelScan ()
                && gcodeProgram.getGcodeProgramFile ().isFile ();

    }

}
