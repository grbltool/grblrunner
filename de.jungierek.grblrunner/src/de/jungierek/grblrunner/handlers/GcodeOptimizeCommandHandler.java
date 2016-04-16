 
package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeOptimizeCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeOptimizeCommandHandler.class );

	@Execute
    public void execute ( @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram, IEventBroker eventBroker ) {
		
        LOG.debug ( "execute: program=" + gcodeProgram );

        gcodeProgram.optimize ();

	}
	
	
	@CanExecute
    public boolean canExecute ( @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {
		
        LOG.debug ( "canExecute: program=" + gcodeProgram );

        return gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isOptimized () && !gcodeProgram.isPlaying () && !gcodeProgram.isAutolevelScan ()
                && gcodeProgram.getGcodeProgramFile ().isFile ();

	}
		
}