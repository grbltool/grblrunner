 
package de.jungierek.grblrunner.handler.menu;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.tool.Toolbox;

public class CameraSelectDynMenuHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialDynMenuHandler.class );

    @Inject
    private IWebcamService webcamService;

    @Inject
    private Toolbox toolbox;

    @AboutToShow
    public void aboutToShow ( List<MMenuElement> items ) {

        LOG.debug ( "aboutToShow:" );

        Webcam [] webcams = webcamService.getWebcams ();

        if ( webcams == null ) {

            MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
            item.setLabel ( "will be detected ..." );
            items.add ( item );

        }
        else {

            Webcam selectedWebcam = webcamService.getWebcam ();
            for ( Webcam webcam : webcams ) {
                LOG.debug ( "webcam=" + webcam );
                toolbox.addMenuItemTo ( items, webcam.equals ( selectedWebcam ), ICommandId.CAMERA_SELECT, ICommandId.CAMERA_SELECT_PARAMETER, webcam.getName () );
            }

        }

    }

    @AboutToHide
    public void aboutToHide () {

    }

}