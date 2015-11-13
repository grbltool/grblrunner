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

public class ProbeDataLoadHandler {

    @Inject
    IGcodeService gcodeService;

    @Inject
    IGcodeModel gcodeModel;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Execute
    public void execute () {

        final File probeDataFile = gcodeService.getProbeDataFile ();
        if ( probeDataFile.isFile () ) {
            gcodeService.loadProbeData ();
        }
        else {
            MessageDialog.openError ( shell, "Error", "File with probe data not found!\n" + probeDataFile.getPath () );
        }

    }

    @CanExecute
    public boolean canExecute () {

        return gcodeModel.isGcodeProgramLoaded () && gcodeService.getProbeDataFile ().isFile () && !gcodeService.isPlaying () && !gcodeService.isScanning ();

    }

}
