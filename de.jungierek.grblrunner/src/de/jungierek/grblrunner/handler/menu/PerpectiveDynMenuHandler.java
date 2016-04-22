package de.jungierek.grblrunner.handler.menu;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.webcam.IWebcamService;
import de.jungierek.grblrunner.tool.Toolbox;

public class PerpectiveDynMenuHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( PerpectiveDynMenuHandler.class );

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    @Inject
    private IWebcamService webcamService;

    @Inject
    private Toolbox toolbox;
    
    @AboutToShow
    public void aboutToShow ( List<MMenuElement> items, @Named(IServiceConstants.ACTIVE_PART) MPart activePart ) {

        LOG.debug ( "aboutToShow:" );

        List<MPerspective> perspectiveList = modelService.findElements ( application, null, MPerspective.class, null );
        final MPerspective selectedPerpective = modelService.getPerspectiveFor ( activePart );
        for ( MPerspective perspective : perspectiveList ) {
            toolbox.addMenuItemTo ( items, perspective == selectedPerpective, ICommandId.PERSPECTIVE_SWITCH, ICommandId.PERSPECTIVE_SWITCH_PARAMETER,
                    perspective.getElementId () );
        }

    }

    @AboutToHide
    public void aboutToHide ( List<MMenuElement> items ) {

    }

}