 
package de.jungierek.grblrunner.handler.menu;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.tool.Toolbox;

public class CameraResolutionDynMenuHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraResolutionDynMenuHandler.class );

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    @Inject
    private IWebcamService webcamService;

    @Inject
    private Toolbox toolbox;

    @PostConstruct
    public void initMenu () {}

    @AboutToShow
    public void aboutToShow ( List<MMenuElement> items ) {

        LOG.debug ( "aboutToShow:" );

        Webcam webcam = webcamService.getWebcam ();

        if ( webcam == null ) {

            MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
            item.setLabel ( "no Cam selected ..." );
            items.add ( item );

        }
        else {

            String [] webcamSizes = webcamService.getWebcamSizes ();
            final String selectedWebcamSize = webcamService.getWebcamSize ();
            for ( String size : webcamSizes ) {
                LOG.debug ( "size=" + size );
                toolbox.addMenuItemTo ( items, size.equals ( selectedWebcamSize ), ICommandId.CAMERA_RESOLUTION, ICommandId.CAMERA_RESOLUTION_PARAMETER, size );
            }

        }

    }

    @AboutToHide
    public void aboutToHide () {

    }

}