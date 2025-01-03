 
package de.jungierek.grblrunner.handler;

import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.serial.ISerialService;

public class SerialSelectPortCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialSelectPortCommandHandler.class );

	@Execute
    public void execute ( @Optional @Named("de.jungierek.grblrunner.commandparameter.serial.port") String port, ISerialService serial ) {

        LOG.debug ( "called port=" + port );
        
        serial.setPortName ( port );
		
	}

    @CanExecute
    public boolean canExecute ( ISerialService serial ) {

        LOG.debug ( "canExecte" );
        
        return !serial.isOpen () && !serial.isDetectingSerialPorts ();

    }

}