package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GcodeRefreshHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeRefreshHandler.class );

    @Execute
    public void execute ( @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "execute:" );

        gcodeProgram.loadGcodeProgram ( gcodeProgram.getGcodeProgramFile () );

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isScanning () );

        return serial.isOpen () && gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeService.isPlaying () && !gcodeService.isScanning ()
                && gcodeProgram.getGcodeProgramFile ().isFile ();

    }

}
