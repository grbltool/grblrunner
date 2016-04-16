package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GrblMoveZeroCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblMoveZeroCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Optional @Named(ICommandId.GRBL_MOVE_ZERO_AXIS_PARAMETER) String axis ) {

        LOG.debug ( "execute: axis=" + axis );

        if ( command != null ) {

            String gcode = "G90 G0 ";

            for ( int i = 0; i < axis.length (); i++ ) {
                gcode += axis.charAt ( i ) + "0";
            }
            gcodeService.sendCommandSuppressInTerminal ( gcode );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
