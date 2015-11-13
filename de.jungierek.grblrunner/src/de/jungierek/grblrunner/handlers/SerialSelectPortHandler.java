 
package de.jungierek.grblrunner.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.IEvents;

public class SerialSelectPortHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialSelectPortHandler.class );

	@Execute
    public void execute ( @Optional @Named("de.jungierek.grblrunner.commandparameter.serial.port") String port, ISerialService serial ) {

        LOG.debug ( "called port=" + port );
        
        serial.setPortName ( port );
		
	}

    @CanExecute
    public boolean canExecute ( ISerialService serial ) {

        LOG.debug ( "canExecte" );
        
        return !updateRunning && !serial.isOpen ();

    }

    private boolean updateRunning = false;

    @Inject
    @Optional
    public void portsDetectingNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_PORTS_DETECTING) Object dummy ) {

        LOG.debug ( "portsDetectedNotified:" );

        updateRunning = true;

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.debug ( "portsDetectedNotified: ports=" + ports );

        updateRunning = false;

    }
		
}