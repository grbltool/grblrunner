package de.jungierek.grblrunner.handler;

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

    @Execute
    public void execute ( IGcodeService gcodeService, @Named(ICommandId.GRBL_MOVE_DIRECTION_PARAMETER) String direction, @Preference(
            nodePath = IConstant.PREFERENCE_NODE,
            value = IPreferenceKey.CAMERA_MILL_OFFSET_X) int offsetX, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.CAMERA_MILL_OFFSET_Y) int offsetY ) {

        LOG.debug ( "execute:" );

        switch ( direction ) {

            case "+": // to mill
                gcodeService.sendCommandSuppressInTerminal ( "G91 G0 X" + offsetX + " Y" + offsetY );
                break;

            case "-": // to camera
                gcodeService.sendCommandSuppressInTerminal ( "G91 G0 X" + -offsetX + " Y" + -offsetY );
                break;

            default:
                break;
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
