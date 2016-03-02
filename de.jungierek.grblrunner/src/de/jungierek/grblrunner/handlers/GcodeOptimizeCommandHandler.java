 
package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;

import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeOptimizeCommandHandler {

	@Execute
    public void execute ( IGcodeProgram program, IEventBroker eventBroker ) {
		
        program.optimize ();

	}
	
	
	@CanExecute
    public boolean canExecute ( IGcodeProgram gcodeProgram ) {
		
        return gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isOptimized () && !gcodeProgram.isPlaying () && !gcodeProgram.isAutolevelScan ()
                && gcodeProgram.getGcodeProgramFile ().isFile ();

	}
		
}