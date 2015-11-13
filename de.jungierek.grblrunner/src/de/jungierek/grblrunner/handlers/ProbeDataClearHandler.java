package de.jungierek.grblrunner.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class ProbeDataClearHandler {

    @Inject
    IGcodeService gcodeService;

    @Inject
    IGcodeModel gcodeModel;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Execute
    public void execute () {

        gcodeService.clearProbeData ();

    }

    @CanExecute
    public boolean canExecute () {

        return gcodeModel.isScanDataComplete () && !gcodeService.isPlaying () && !gcodeService.isScanning ();

    }

}
