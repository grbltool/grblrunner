package de.jungierek.grblrunner.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GrblMoveZeroCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblMoveZeroCommandHandler.class );

    // preference
    private double seekFeedrate;

    @Inject
    public void setSeekFeedrate ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.MAX_SEEK_FEEDRATE) int feedrate ) {

        LOG.debug ( "setSeekFeedrate: feedrate=" + feedrate );

        seekFeedrate = feedrate;

    }

    @Execute
    public void execute ( IGcodeService gcodeService, ParameterizedCommand command, @Optional @Named(ICommandId.GRBL_MOVE_ZERO_AXIS_PARAMETER) String axis ) {

        LOG.debug ( "execute: axis=" + axis );

        if ( command != null ) {

            String line = "$J=G21G90F" + seekFeedrate;

            for ( int i = 0; i < axis.length (); i++ ) {
                line += axis.charAt ( i ) + "0";
            }
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
