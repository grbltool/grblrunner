package de.jungierek.grblrunner.handlers;

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

public class ProbeDataLoadHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeDataLoadHandler.class );

    @Execute
    public void execute ( Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        final File probeDataFile = gcodeProgram.getAutolevelDataFile ();
        if ( probeDataFile.isFile () ) {
            gcodeProgram.loadAutolevelData ();
        }
        else {
            MessageDialog.openError ( shell, "Error", "File with probe data not found!\n" + probeDataFile.getPath () );
        }

    }

    @CanExecute
    public boolean canExecute ( IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isScanning () );

        return gcodeProgram != null && gcodeProgram.isLoaded () && gcodeProgram.getAutolevelDataFile ().isFile () && !gcodeService.isPlaying () && !gcodeService.isScanning ();

    }

}
