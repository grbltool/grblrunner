package de.jungierek.grblrunner.handler;

import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tool.Toolbox;

public class ProbeDataScanCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeDataScanCommandHandler.class );

    // @formatter:off
    @Execute
    public void execute ( 
            IGcodeService gcodeService, 
            Toolbox toolbox, 
            @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram, 
            @Named(ICommandId.AUTOLEVEL_ZMIN_PARAMETER) String zMin, 
            @Named(ICommandId.AUTOLEVEL_ZMAX_PARAMETER) String zMax, 
            @Named(ICommandId.AUTOLEVEL_ZCLEARANCE_PARAMETER) String zClearance, 
            @Named(ICommandId.AUTOLEVEL_PROBEFEEDRATE_PARAMETER) String probeFeedrate, 
            @Preference( nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.Z_CLEARANCE) double zClearancePref,
            @Preference( nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_Z_MAX) double probeZMaxPref, 
            @Preference( nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_DEPTH) double probeDepthPref, 
            @Preference( nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_FEEDRATE) double probeFeedratePref, 
            @Preference( nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_WITH_ERROR) boolean probeWithErrorPref 
        ) {
        // @formatter:on

        gcodeService.scanAutolevelData (
                gcodeProgram, // current selected gcode program
                toolbox.parseDouble ( zMin, probeDepthPref ), toolbox.parseDouble ( zMax, probeZMaxPref ), toolbox.parseDouble ( zClearance, zClearancePref ),
                toolbox.parseDouble ( probeFeedrate, probeFeedratePref ), probeWithErrorPref );

    }

    @CanExecute
    public boolean canExecute ( ISerialService serialService, IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        return (serialService.isOpen () || IConstant.AUTOLEVEL_ENABLE_WITHOUT_SERIAL) && gcodeProgram != null && gcodeProgram.isLoaded ()
                && !gcodeProgram.isAutolevelScanComplete () && !gcodeService.isPlaying ()
                && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}