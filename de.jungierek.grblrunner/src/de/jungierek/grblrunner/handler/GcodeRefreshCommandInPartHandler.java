package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeRefreshCommandInPartHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeRefreshCommandInPartHandler.class );

    @Execute
    public void execute ( IGcodeProgram gcodeProgram ) {

        LOG.debug ( "execute: program=" + gcodeProgram );

        gcodeProgram.loadGcodeProgram ( gcodeProgram.getGcodeProgramFile () );

    }

    @CanExecute
    public boolean canExecute ( IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeProgram.isPlaying () + " isscanning=" + gcodeProgram.isAutolevelScan () );

        return gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isPlaying () && !gcodeProgram.isAutolevelScan ()
                && gcodeProgram.getGcodeProgramFile ().isFile ();

    }

}
