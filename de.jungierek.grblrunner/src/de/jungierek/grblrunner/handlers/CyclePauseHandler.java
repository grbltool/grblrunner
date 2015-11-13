 
package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class CyclePauseHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CyclePauseHandler.class );

    @Execute
    public void execute ( IGcodeService gcode ) {

        LOG.debug ( "execute:" );

        gcode.sendFeedHold ();
		
	}
	
    @CanExecute
    public boolean canExecute ( ISerialService serial ) {

        LOG.debug ( "canExecute:" );

        return serial.isOpen ();
    }

}