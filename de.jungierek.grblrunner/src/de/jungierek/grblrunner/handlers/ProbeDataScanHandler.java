package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class ProbeDataScanHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeDataScanHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, PartTools partTools, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram,
            @Named(ICommandID.AUTOLEVEL_ZMIN_PARAMETER) String zMin, @Named(ICommandID.AUTOLEVEL_ZMAX_PARAMETER) String zMax,
            @Named(ICommandID.AUTOLEVEL_ZCLEARANCE_PARAMETER) String zClearance, @Named(ICommandID.AUTOLEVEL_PROBEFEEDRATE_PARAMETER) String probeFeedrate ) {

        gcodeService.scanAutolevelData (
                gcodeProgram, // current selected gcode program
                partTools.parseDouble ( zMin, IPreferences.PROBE_Z_MIN ), partTools.parseDouble ( zMax, IPreferences.PROBE_Z_MAX ),
                partTools.parseDouble ( zClearance, IPreferences.Z_CLEARANCE ), partTools.parseDouble ( probeFeedrate, IPreferences.PROBE_FEEDRATE ) );

    }

    @CanExecute
    public boolean canExecute ( ISerialService serialService, IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        return serialService.isOpen () && gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isAutolevelScanComplete () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();
    }

}