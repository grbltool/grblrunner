 
package de.jungierek.grblrunner.parts;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalDirectMenuItem {

    private static final Logger LOG = LoggerFactory.getLogger ( TerminalDirectMenuItem.class );

    @Execute
    public void execute ( MPart part, MDirectMenuItem item ) {

        LOG.debug ( "execute: part=" + part + " item=" + item );

        TerminalPart terminalPart = (TerminalPart) part.getObject ();
        String type = item.getPersistedState ().get ( "type" );

        boolean selected = item.isSelected ();

        if ( type != null ) {
            switch ( type ) {
                case "grblstate":
                    terminalPart.setShowGrblState ( selected );
                    break;
                case "gcodestate":
                    terminalPart.setShowGcodeState ( selected );
                    break;
                default:
                    break;
            }
        }
		
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;

	}
		
}