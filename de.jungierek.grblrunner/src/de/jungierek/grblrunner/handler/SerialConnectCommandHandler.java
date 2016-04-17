 
package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.serial.ISerialService;

public class SerialConnectCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialConnectCommandHandler.class );

    @Execute
    public void execute ( ISerialService serial ) {
		
        serial.connect ();

	}
	
	
	@CanExecute
    public boolean canExecute ( ISerialService serial ) {
		
        return serial.getPortName () != null && !serial.isOpen () && !serial.isDetectingSerialPorts ();

	}
		
}