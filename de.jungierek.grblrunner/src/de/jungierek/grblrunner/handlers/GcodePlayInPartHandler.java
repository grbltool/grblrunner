package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GcodePlayInPartHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodePlayInPartHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, IGcodeProgram gcodeProgram ) {

        LOG.debug ( "execute: program=" + gcodeProgram );

        gcodeService.playGcodeProgram ( gcodeProgram );

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService, IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        // check for global playing or scanning, only one program can be played/scanned!!!
        return serial.isOpen () && gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
