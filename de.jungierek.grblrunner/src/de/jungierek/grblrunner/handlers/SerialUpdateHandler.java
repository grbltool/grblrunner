 
package de.jungierek.grblrunner.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class SerialUpdateHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialUpdateHandler.class );

    @Execute
    public void execute ( ISerialService serial ) {
		
        serial.detectSerialPortsAsync ();

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial ) {

        return !updateRunning && serial.getCachedSerialPorts () != null && !serial.isOpen ();

    }

    private boolean updateRunning = false;

    @Inject
    @Optional
    public void portsDetectingNotified ( @UIEventTopic(IEvents.SERIAL_PORTS_DETECTING) Object dummy ) {

        LOG.debug ( "portsDetectedNotified:" );

        updateRunning = true;

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvents.SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.debug ( "portsDetectedNotified: ports=" + ports );

        updateRunning = false;

    }

}