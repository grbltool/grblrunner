 
package de.jungierek.grblrunner.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GcodeSimpleCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeSimpleCommandHandler.class );

    @Inject
    public GcodeSimpleCommandHandler () {
        LOG.trace ( "GcodeSimpleCommandHandler: constructor called" );
    }

    // this only works for toolbar items
    @Execute
    @Optional
    public void execute ( IGcodeService gcode, MHandledToolItem item ) {

        LOG.debug ( "execute: item=" + item );

        if ( item != null ) {

            String id = item.getElementId ();
            boolean isSuppressLines = item.getPersistedState ().get ( "no_suppress_lines" ) == null;

            sendCommand ( gcode, id, isSuppressLines );

        }

    }

    private void sendCommand ( IGcodeService gcode, String id, boolean isSuppressLines ) {

        String grblCommand = id.substring ( 1 + id.lastIndexOf ( '.' ) );
        LOG.debug ( "execute: grblCommand=" + grblCommand );

        String simpleCOmmand = null;

        switch ( grblCommand ) {
            case "home":
                simpleCOmmand = "$H";
                break;
            case "unlock":
                simpleCOmmand = "$X";
                break;
            case "check":
                simpleCOmmand = "$C";
                break;

            default:
                if ( grblCommand.startsWith ( "$" ) ) {
                    simpleCOmmand = grblCommand;
                }
                break;
        }

        LOG.debug ( "execute: simpleCommand=" + simpleCOmmand );

        if ( simpleCOmmand != null ) {
            LOG.debug ( "execute: isSuppressLines=" + isSuppressLines );
            if ( isSuppressLines ) {
                gcode.sendCommandSuppressInTerminal ( simpleCOmmand );
            }
            else {
                gcode.sendCommand ( simpleCOmmand );
            }
        }
    }
	
	@CanExecute
    public boolean canExecute ( IGcodeService gcode, IGcodeModel model, ISerialService serial ) {

        LOG.trace ( "canExecute:" );
		
        return serial.isOpen () && !gcode.isPlaying () && !gcode.isScanning ();

	}
		
}