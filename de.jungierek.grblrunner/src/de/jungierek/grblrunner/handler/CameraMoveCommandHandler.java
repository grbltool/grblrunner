package de.jungierek.grblrunner.handler;

import javax.inject.Inject;
import javax.inject.Named;

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

public class CameraMoveCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraMoveCommandHandler.class );

    // preference
    private double seekFeedrate;

    @Inject
    public void setSeekFeedrate ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.MAX_SEEK_FEEDRATE) int feedrate ) {

        LOG.debug ( "setSeekFeedrate: feedrate=" + feedrate );

        seekFeedrate = feedrate;

    }

    @Execute
    public void execute ( IGcodeService gcodeService, @Named(ICommandId.GRBL_MOVE_DIRECTION_PARAMETER) String direction, @Preference(
            nodePath = IConstant.PREFERENCE_NODE,
            value = IPreferenceKey.CAMERA_MILL_OFFSET_X) int offsetX, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.CAMERA_MILL_OFFSET_Y) int offsetY ) {

        // direction is '+' or '-'
        String line = "$J=G21G91 X" + direction + offsetX + " Y" + direction + offsetY + " F" + seekFeedrate;
        LOG.debug ( "execute: line=" + line );

        try {
            gcodeService.sendCommandSuppressInTerminal ( line );
        }
        catch ( InterruptedException exc ) {
            LOG.info ( "execute: send cancelled for command=" + line );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
