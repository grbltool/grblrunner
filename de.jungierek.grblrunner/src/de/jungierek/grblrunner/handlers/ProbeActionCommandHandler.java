package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.ICommandID;

public class ProbeActionCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbeActionCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Named(ICommandID.PROBE_ACTION_DEPTH_PARAMETER) String depth ) {

        LOG.debug ( "execute: depth=" + depth );

        if ( command != null ) {
            if ( IPreferences.PROBE_WITH_ERROR ) {
                gcodeService.sendCommandSuppressInTerminal ( "G90G38.2Z" + depth + "F" + IPreferences.PROBE_FEEDRATE );
            }
            else {
                gcodeService.sendCommandSuppressInTerminal ( "G90G38.3Z" + depth + "F" + IPreferences.PROBE_FEEDRATE );
            }
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();

    }

}
