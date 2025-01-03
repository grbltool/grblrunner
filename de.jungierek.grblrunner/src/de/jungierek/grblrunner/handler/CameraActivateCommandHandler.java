 
package de.jungierek.grblrunner.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.service.webcam.IWebcamService;
import jakarta.inject.Inject;

public class CameraActivateCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraActivateCommandHandler.class );

    @Inject
    private IWebcamService webcamService;

    // private final static Dimension [] customSizes = { new Dimension ( 1024, 768 ), new Dimension ( 1600, 1200 ), };

	@Execute
	public void execute() {

        LOG.debug ( "execute called" );

        final Webcam webcam = webcamService.getWebcam ();
        if ( webcam.isOpen () ) {
            webcamService.stop ();
        }
        else {
            new Thread ( ( ) -> webcamService.start () ).start ();
        }
		
	}

    @CanExecute
	public boolean canExecute () {
	    
        return webcamService.getWebcam () != null;

	}
		
}