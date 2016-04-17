package de.jungierek.grblrunner.handler;

import java.io.File;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class ProbeDataSaveCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeDataSaveCommandHandler.class );

    @Execute
    public void execute ( Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        if ( gcodeProgram.isAutolevelScanComplete () ) {
            final File probeDataFile = gcodeProgram.getAutolevelDataFile ();
            if ( !probeDataFile.isFile () || MessageDialog.openQuestion ( shell, "Decision", "Overwrite file with probe data?\n" + probeDataFile.getPath () ) ) {
                gcodeProgram.saveAutolevelData ();
            }
        }
        else {
            MessageDialog.openError ( shell, "Error", "No probe data available!" );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        return gcodeProgram != null && gcodeProgram.isLoaded () && gcodeProgram.isAutolevelScanComplete () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();

    }

}
