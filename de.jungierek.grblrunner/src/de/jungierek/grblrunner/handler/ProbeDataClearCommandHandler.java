package de.jungierek.grblrunner.handler;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class ProbeDataClearCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeDataClearCommandHandler.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Execute
    public void execute ( @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        gcodeProgram.clearAutolevelData ();

    }

    @CanExecute
    public boolean canExecute ( IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        return gcodeProgram != null && gcodeProgram.isAutolevelScanComplete () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();

    }

}
