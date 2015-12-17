 
package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.parts.GcodeEditorPart;
import de.jungierek.grblrunner.parts.MacroPart;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class GcodeCloseHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeCloseHandler.class );

	@Execute
    public void execute ( MPart part, EPartService partService, ESelectionService selectionService, IEventBroker eventBroker ) {

        LOG.debug ( "execute: part=" + part );

        // setSelection must be here, after hide part, this selection is ignored!
        selectionService.setSelection ( null );
        partService.hidePart ( part );
        
        eventBroker.send ( IEvents.GCODE_CLOSED, null );
		
	}
	
    @CanExecute
    public boolean canExecute ( MPart part, IGcodeProgram gcodeProgram, IGcodeService gcodeService ) {

        LOG.debug ( "canExecute: part=" + part + " program=" + gcodeProgram );

        if ( part == null ) return false;

        final Object partObject = part.getObject ();
        if ( !(partObject instanceof GcodeEditorPart) && !(partObject instanceof MacroPart) ) return false;

        return !gcodeProgram.isPlaying () && !gcodeService.isScanning ();

	}
		
}