 
package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.service.webcam.IWebcamService;

public class CameraSelectHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraSelectHandler.class );

    @Execute
    public void execute ( @Optional @Named("de.jungierek.grblrunner.commandparameter.camera.select") String webcamName, IWebcamService webcamService ) {

        LOG.debug ( "execute called name=" + webcamName );

        if ( webcamName == null ) return;

        Webcam [] webcams = webcamService.getWebcams ();
        for ( Webcam webcam : webcams ) {
            if ( webcamName.equals ( webcam.getName () ) ) {
                webcamService.setWebcam ( webcam );
            }
        }
		
	}

    @CanExecute
    public boolean canExecute ( IWebcamService webcamService ) {
        
        return webcamService.getWebcams () != null && (webcamService.getWebcam () == null || !webcamService.getWebcam ().isOpen ());

    }
		
}