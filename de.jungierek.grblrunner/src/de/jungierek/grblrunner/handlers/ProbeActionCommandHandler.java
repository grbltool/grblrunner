package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.ICommandID;

public class ProbeActionCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeActionCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Named(ICommandID.PROBE_ACTION_DEPTH_PARAMETER) String probeDepth, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_FEEDRATE)double probeFeedrate, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_WITH_ERROR)boolean probeWithError ) {

        LOG.debug ( "execute: depth=" + probeDepth );

        if ( command != null ) {
            if ( probeWithError ) {
                gcodeService.sendCommandSuppressInTerminal ( "G90G38.2Z" + probeDepth + "F" + probeFeedrate );
            }
            else {
                gcodeService.sendCommandSuppressInTerminal ( "G90G38.3Z" + probeDepth + "F" + probeFeedrate );
            }
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
