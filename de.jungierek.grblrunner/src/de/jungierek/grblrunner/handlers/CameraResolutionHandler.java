 
package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.service.webcam.IWebcamService;

public class CameraResolutionHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraResolutionHandler.class );

    @Execute
    public void execute ( @Optional @Named("de.jungierek.grblrunner.commandparameter.camera.resolution") String resolution, IWebcamService webcamService ) {

        LOG.info ( "execute called resolution=" + resolution );
        LOG.info ( "res1=" + webcamService.getWebcam ().getViewSize () );

        if ( resolution == null ) return;

        webcamService.setViewSize ( resolution );

        LOG.info ( "res2=" + webcamService.getWebcam ().getViewSize () );
    }

    @CanExecute
    public boolean canExecute ( IWebcamService webcamService ) {

        final Webcam webcam = webcamService.getWebcam ();
        return webcam != null && !webcam.isOpen ();

    }
		
}