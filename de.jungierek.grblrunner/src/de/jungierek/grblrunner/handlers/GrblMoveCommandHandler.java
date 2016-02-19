package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.ICommandID;

public class GrblMoveCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblMoveCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Named(ICommandID.GRBL_MOVE_AXIS_PARAMETER) String axis,
            @Named(ICommandID.GRBL_MOVE_DIRECTION_PARAMETER) String direction, @Named(ICommandID.GRBL_MOVE_DISTANCE_PARAMETER) String distance ) {

        LOG.debug ( "execute: axis=" + axis + " cmd=" + command + " dir=" + direction + " dist=" + distance );

        if ( command != null ) {
            gcodeService.sendCommandSuppressInTerminal ( "G91 G0 " + axis + direction + distance );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();

    }

}
