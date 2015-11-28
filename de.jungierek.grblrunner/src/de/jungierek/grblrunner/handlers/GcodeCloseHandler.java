 
package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.GcodeEditor;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class GcodeCloseHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeCloseHandler.class );

	@Execute
    public void execute ( MPart part, EPartService partService, ESelectionService selectionService ) {

        LOG.debug ( "execute: part=" + part );

        // setSelection must be here, after hide part, this selection is ignored!
        selectionService.setSelection ( null );
        partService.hidePart ( part );
		
	}
	
    // @CanExecute
    // public boolean canExecute () {
    //
    // LOG.info ( "canExecute:" );
    //
    // return true;
    //
    // }

    @CanExecute
    public boolean canExecute ( MPart part, IGcodeProgram gcodeProgram, IGcodeService gcodeService ) {

        LOG.debug ( "canExecute: part=" + part + " program=" + gcodeProgram );

        if ( part == null || !(part.getObject () instanceof GcodeEditor) ) return false;

        return !gcodeService.isPlaying () && !gcodeService.isScanning ();

	}
		
}