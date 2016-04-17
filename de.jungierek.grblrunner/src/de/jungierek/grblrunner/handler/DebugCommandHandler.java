 
package de.jungierek.grblrunner.handler;

import java.util.Collection;
import java.util.List;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( DebugCommandHandler.class );

    // @Execute
    public void executeListPartData ( EPartService partService ) {

        LOG.debug ( "execute:" );

        Collection<MPart> parts = partService.getParts ();
        LOG.info ( "execute: ===========================================" );
        for ( MPart part : parts ) {

            final String data = part.getContainerData ();
            if ( data != null ) LOG.info ( "execute: data=" + data + " part=" + part );
        }
        LOG.info ( "execute: ===========================================" );


    }

    @Execute
    public void executeListPartStackData ( EModelService modelService, MApplication application, MWindow window ) {

        LOG.debug ( "execute:" );

        List<MPartStack> elements = modelService.findElements ( application, null, MPartStack.class, null );
        LOG.info ( "execute: ===========================================" );
        for ( MPartStack partStack : elements ) {
            final String data = partStack.getContainerData ();
            if ( data != null ) LOG.info ( "execute: data=" + data + " partStack=" + partStack );
        }
        LOG.info ( "execute: ===========================================" );

    }

	@CanExecute
	public boolean canExecute() {

        LOG.debug ( "canExecute:" );
		
		return true;

	}
		
}