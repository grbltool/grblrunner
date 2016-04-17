package de.jungierek.grblrunner.handler;

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

public class CoordinateOffsetCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CoordinateOffsetCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Optional @Named(ICommandId.COORDINATE_OFFSET_PARAMETER) String axis ) {

        LOG.debug ( "execute: axis=" + axis + " cmd=" + command );

        if ( command != null ) {
            String id = command.getId ();
            String type = id.substring ( id.lastIndexOf ( '.' ) + 1 );
            switch ( type ) {
                case "set":
                    gcodeService.sendCommandSuppressInTerminal ( "G10 L20 " + axis + "0" );
                    break;
                case "reset":
                    gcodeService.sendCommandSuppressInTerminal ( "G10 L2 " + axis + "0" );
                    break;

                default:
                    break;
            }
        }


    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
