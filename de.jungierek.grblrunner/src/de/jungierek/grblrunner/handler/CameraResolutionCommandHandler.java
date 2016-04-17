 
package de.jungierek.grblrunner.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.webcam.IWebcamService;

public class CameraResolutionCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraResolutionCommandHandler.class );

    @Execute
    public void execute ( @Optional @Named(ICommandId.CAMERA_RESOLUTION_PARAMETER) String resolution, IWebcamService webcamService ) {

        LOG.debug ( "execute called resolution=" + resolution );
        LOG.debug ( "res1=" + webcamService.getWebcam ().getViewSize () );

        if ( resolution == null ) return;

        webcamService.setViewSize ( resolution );

        LOG.debug ( "res2=" + webcamService.getWebcam ().getViewSize () );
    }

    @CanExecute
    public boolean canExecute ( IWebcamService webcamService ) {

        final Webcam webcam = webcamService.getWebcam ();
        return webcam != null && !webcam.isOpen ();

    }
		
}