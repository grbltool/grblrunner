package de.jungierek.grblrunner.handlers;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.tools.CommandTools;

public class PerpectiveDynMenuHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( PerpectiveDynMenuHandler.class );

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    @Inject
    private IWebcamService webcamService;

    @Inject
    private CommandTools commandTool;
    
    @AboutToShow
    public void aboutToShow ( List<MMenuElement> items, @Named(IServiceConstants.ACTIVE_PART) MPart activePart ) {

        LOG.debug ( "aboutToShow:" );

        List<MPerspective> perspectiveList = modelService.findElements ( application, null, MPerspective.class, null );
        for ( MPerspective perspective : perspectiveList ) {

            MCommand command = commandTool.findCommand ( ICommandId.PERSPECTIVE_SWITCH );

            MParameter parameter = MCommandsFactory.INSTANCE.createParameter ();
            parameter.setElementId ( ICommandId.PERSPECTIVE_SWITCH_PARAMETER + "." + perspective.getElementId () );
            parameter.setName ( ICommandId.PERSPECTIVE_SWITCH_PARAMETER ); // this is the importend "id"
            parameter.setValue ( perspective.getElementId () );

            MCommandParameter commandParameter = MCommandsFactory.INSTANCE.createCommandParameter ();
            commandParameter.setElementId ( ICommandId.PERSPECTIVE_SWITCH_PARAMETER );
            commandParameter.setName ( ICommandId.PERSPECTIVE_SWITCH_PARAMETER );
            commandParameter.setOptional ( true );

            MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
            item.setLabel ( perspective.getLabel () );
            item.setType ( ItemType.RADIO );
            item.setCommand ( command );
            item.getParameters ().add ( parameter );
            if ( perspective == modelService.getPerspectiveFor ( activePart ) ) {
                item.setSelected ( true );
            }
            items.add ( item );

        }

    }

    @AboutToHide
    public void aboutToHide ( List<MMenuElement> items ) {

    }

}