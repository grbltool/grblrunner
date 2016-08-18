package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class ExitHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( ExitHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, ISerialService serialService, IEventBroker eventBroker, IWorkbench workbench ) {

        if ( gcodeService.isPlaying () || gcodeService.isAutolevelScan () || !gcodeService.isGrblIdle () && !gcodeService.isGrblAlarm () ) {
            LOG.warn ( "close: job is runnung" );
            eventBroker.post ( IEvent.MESSAGE_ERROR, "Closing application is not possible! Job is running!" );
            return;
        }

        serialService.close ();
        workbench.close ();

    }

}
