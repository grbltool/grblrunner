 
package de.jungierek.grblrunner.handlers;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.tools.Toolbox;

public class CameraSelectDynMenuHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialDynMenuHandler.class );

    private final static String COMMAND_ID = "de.jungierek.grblrunner.command.camera.select";
    private final static String PARAMETER_ID = "de.jungierek.grblrunner.commandparameter.camera.select";

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

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

                MParameter parameter = MCommandsFactory.INSTANCE.createParameter ();
                final String webcamName = webcam.getName ();
                LOG.debug ( "name=" + webcamName );

                parameter.setElementId ( PARAMETER_ID + webcamName );
                parameter.setName ( PARAMETER_ID ); // this is the importend "id"
                parameter.setValue ( webcamName );
                LOG.debug ( "parameter=" + parameter );

                MCommand command = toolbox.findCommand ( COMMAND_ID );
                LOG.debug ( "command=" + command );

                MCommandParameter commandParameter = MCommandsFactory.INSTANCE.createCommandParameter ();
                commandParameter.setElementId ( PARAMETER_ID );
                commandParameter.setName ( PARAMETER_ID );
                commandParameter.setOptional ( true );
                LOG.debug ( "commandParameter=" + commandParameter );

                MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
                item.setLabel ( webcamName );
                item.setType ( ItemType.RADIO );
                item.setCommand ( command );
                item.getParameters ().add ( parameter );
                if ( webcam.equals ( selectedWebcam ) ) {
                    item.setSelected ( true );
                }
                items.add ( item );

            }

        }

    }

    @AboutToHide
    public void aboutToHide () {

    }

}