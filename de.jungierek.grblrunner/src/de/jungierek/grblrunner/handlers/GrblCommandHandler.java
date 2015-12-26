package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public abstract class GrblCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblCommandHandler.class );

    private static final String COMMAND = "noop";
    
    abstract protected String getCommand ();
    abstract protected boolean isSuppressLines ();
        
    @Execute
    public void execute ( IGcodeService gcodeService ) {

        LOG.debug ( "execute:" );

        if ( isSuppressLines () ) {
            gcodeService.sendCommandSuppressInTerminal ( getCommand () );
        }
        else {
            gcodeService.sendCommand ( getCommand () );
        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcodeService.isPlaying () && !gcodeService.isAutolevelScan ();

    }


}
