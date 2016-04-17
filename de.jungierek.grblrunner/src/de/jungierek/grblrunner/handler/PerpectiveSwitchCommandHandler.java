package de.jungierek.grblrunner.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;

public class PerpectiveSwitchCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( PerpectiveSwitchCommandHandler.class );

    @Execute
    // @Optional
    public void switchPerspective ( @Named(ICommandId.PERSPECTIVE_SWITCH_PARAMETER) String perspectiveElementId, MPerspective activePerspective, MApplication application, EPartService partService, EModelService modelService ) {

        LOG.debug ( "switchPersepctive: id=" + perspectiveElementId );

        MPerspective perspective = null;

        if ( IConstant.CYCLIC_PERSPECTIVE_SWITCHING.equals ( perspectiveElementId ) ) {

            List<MPerspective> perspectiveList = modelService.findElements ( application, null, MPerspective.class, null );
            MPerspective [] perspectives = perspectiveList.toArray ( new MPerspective [perspectiveList.size ()] );

            for ( int i = 0; i < perspectives.length; i++ ) {
                if ( perspectives[i] == activePerspective ) {
                    int p = i + 1;
                    if ( p > perspectives.length - 1 ) p = 0;
                    perspective = perspectives[p];
                    break;
                }
            }

        }
        else {
            List<MPerspective> perspectiveList = modelService.findElements ( application, perspectiveElementId, MPerspective.class, null );
            if ( perspectiveList.size () == 1 ) perspective = perspectiveList.get ( 0 );
        }

        if ( perspective != null ) partService.switchPerspective ( perspective );

    }

}
