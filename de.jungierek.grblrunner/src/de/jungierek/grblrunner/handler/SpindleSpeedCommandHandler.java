package de.jungierek.grblrunner.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class SpindleSpeedCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SpindleSpeedCommandHandler.class );

    @Execute
    @Optional
    public void execute ( IGcodeService gcodeService, @Named(ICommandId.SPINDLE_SPEED_PARAMETER) String speed ) {

        LOG.debug ( "execute:" );

        final String line = "S" + speed;
        try {
            gcodeService.sendCommandSuppressInTerminal ( line );
        }
        catch ( InterruptedException exc ) {
            LOG.info ( "execute: interrupted exception in line=" + line );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan () && gcodeService.isGrblIdle ();

    }

}
