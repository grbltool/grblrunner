package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeOptimizeInPartCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeOptimizeInPartCommandHandler.class );

    @Execute
    public void execute ( IGcodeProgram gcodeProgram, IEventBroker eventBroker ) {

        LOG.debug ( "execute: program=" + gcodeProgram );

        gcodeProgram.optimize ();

    }

    @CanExecute
    public boolean canExecute ( IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeProgram.isPlaying () + " isscanning=" + gcodeProgram.isAutolevelScan () );
        return gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isOptimized () && !gcodeProgram.isPlaying () && !gcodeProgram.isAutolevelScan ()
                && gcodeProgram.getGcodeProgramFile ().isFile ();

    }

}
