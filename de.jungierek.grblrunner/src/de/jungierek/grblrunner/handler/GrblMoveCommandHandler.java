package de.jungierek.grblrunner.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GrblMoveCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblMoveCommandHandler.class );

    // preference
    private double seekFeedrate;

    @Inject
    public void setSeekFeedrate ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.MAX_SEEK_FEEDRATE) int feedrate ) {

        LOG.debug ( "setSeekFeedrate: feedrate=" + feedrate );

        seekFeedrate = feedrate;

    }

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Named(ICommandId.GRBL_MOVE_AXIS_PARAMETER) String axis,
            @Named(ICommandId.GRBL_MOVE_DIRECTION_PARAMETER) String direction, @Named(ICommandId.GRBL_MOVE_DISTANCE_PARAMETER) String distance ) {

        LOG.debug ( "execute: axis=" + axis + " cmd=" + command + " dir=" + direction + " dist=" + distance );

        if ( command != null ) {
            final String line = "$J=G21G91F" + seekFeedrate + axis + direction + distance;
            try {
                gcodeService.sendCommandSuppressInTerminal ( line );
            }
            catch ( InterruptedException exc ) {
                LOG.info ( "execute: interrupted exception in line=" + line );
            }
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && (gcodeService.isGrblIdle () || gcodeService.isGrblJog ());

    }

}
