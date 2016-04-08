 
package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.MacroPart;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class MacroRestoreCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroRestoreCommandHandler.class );

	@Execute
    public void execute ( MPart part ) {

        LOG.debug ( "execute:" );

        ((MacroPart) part.getObject ()).restorePreferenceData ();

	}
	
	@CanExecute
    public boolean canExecute ( MPart part, IGcodeProgram gcodeProgram, IGcodeService gcodeService ) {

        LOG.debug ( "canExecute: part=" + part + " program=" + gcodeProgram );

        if ( part == null || !(part.getObject () instanceof MacroPart) ) return false;

        return !gcodeProgram.isPlaying () && !gcodeProgram.isAutolevelScan ();

    }
		
}