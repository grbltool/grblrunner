 
package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class SerialDisconnectHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialDynMenuHandler.class );

    @Execute
    public void execute ( ISerialService serial ) {
		
        serial.close ();

	}

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcode ) {

        return serial.isOpen () && !gcode.isAutolevelScan () && !gcode.isPlaying ();

    }

}