package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public abstract class GrblCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblCommandHandler.class );

    abstract protected String getCommand ();
    abstract protected boolean isSuppressLines ();
        
    @Execute
    public void execute ( IGcodeService gcodeService ) {

        LOG.debug ( "execute:" );

        final String line = getCommand ();
        try {
            if ( isSuppressLines () ) {
                gcodeService.sendCommandSuppressInTerminal ( line );
            }
            else {
                gcodeService.sendCommand ( line );
            }
        }
        catch ( InterruptedException exc ) {
            LOG.info ( "execute: interrupted exception in line=" + line );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();

    }


}
