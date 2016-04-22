 
package de.jungierek.grblrunner.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.webcam.IWebcamService;

public class CameraSelectCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraSelectCommandHandler.class );

    @Execute
    public void execute ( @Named(ICommandId.CAMERA_SELECT_PARAMETER) String webcamName, IWebcamService webcamService, IEventBroker eventBroker ) {

        LOG.debug ( "execute called name=" + webcamName );

        if ( webcamName == null ) return;

        Webcam [] webcams = webcamService.getWebcams ();
        for ( Webcam webcam : webcams ) {
            if ( webcamName.equals ( webcam.getName () ) ) {
                webcamService.setWebcam ( webcam );
                eventBroker.send ( IEvent.CAMERA_SELECTED, webcam.getName () );
            }
        }
		
	}

    @CanExecute
    public boolean canExecute ( IWebcamService webcamService ) {
        
        return webcamService.getWebcams () != null && (webcamService.getWebcam () == null || !webcamService.getWebcam ().isOpen ());

    }
		
}