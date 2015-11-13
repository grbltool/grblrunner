package de.jungierek.grblrunner.handlers;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class ProbeDataSaveHandler {

    @Inject
    IGcodeService gcodeService;

    @Inject
    IGcodeModel gcodeModel;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Execute
    public void execute () {

        if ( gcodeModel.isScanDataComplete () ) {
            final File probeDataFile = gcodeService.getProbeDataFile ();
            if ( !probeDataFile.isFile () || MessageDialog.openQuestion ( shell, "Decision", "Overwrite file with probe data?\n" + probeDataFile.getPath () ) ) {
                gcodeService.saveProbeData ();
            }
        }
        else {
            MessageDialog.openError ( shell, "Error", "No probe data available!" );
        }

    }

    @CanExecute
    public boolean canExecute () {

        return gcodeModel.isGcodeProgramLoaded () && gcodeModel.isScanDataComplete () && !gcodeService.isPlaying () && !gcodeService.isScanning ();

    }

}
