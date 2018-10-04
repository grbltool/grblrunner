package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.handler.GcodePlayCommandHandler.GcodePlayDialog;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GcodePlayCommandInPartHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodePlayCommandInPartHandler.class );

    // @formatter:off
    @SuppressWarnings("restriction")
    @Execute
    public void execute ( 
            Display display, 
            Shell shell, 
            IGcodeService gcodeService, 
            EPartService partService, 
            IGcodeProgram gcodeProgram,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PLAY_GCODE_DIALOG_SHOW) boolean showDialog,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PLAY_GCODE_DIALOG_FONT_DATA) String fontDataString,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_DEPTH) double probeDepth, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_FEEDRATE) double probeFeedrate, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_WITH_ERROR) boolean probeWithError
    ) {
    // @formatter:on

        LOG.debug ( "execute: program=" + gcodeProgram );

        if ( !showDialog || showDialog && new GcodePlayDialog ( display, shell, gcodeService, partService, fontDataString ).open () == 0 ) { // 0 -> ok, 1 -> Cancel
            gcodeService.playGcodeProgram ( gcodeProgram, probeDepth, probeFeedrate, probeWithError );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService, IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        // check for global playing or scanning, only one program can be played/scanned!!!
        return serial.isOpen () && gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
