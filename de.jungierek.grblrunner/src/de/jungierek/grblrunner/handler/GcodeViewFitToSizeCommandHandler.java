 
package de.jungierek.grblrunner.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.part.GcodeViewPart;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeViewFitToSizeCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewFitToSizeCommandHandler.class );

	@Execute
    public void execute ( EPartService partService ) {
	    
        LOG.debug ( "execute:" );

        ((GcodeViewPart) partService.findPart ( "de.jungierek.grblrunner.part.view" ).getObject ()).getGcodeViewGroup ().fitToSize ();
		
	}
	
	@CanExecute
    public boolean canExecute ( @Optional @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {
	    
        return program != null;

	}
		
}