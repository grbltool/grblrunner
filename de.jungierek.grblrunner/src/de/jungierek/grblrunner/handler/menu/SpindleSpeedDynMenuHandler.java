package de.jungierek.grblrunner.handler.menu;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.Toolbox;

public class SpindleSpeedDynMenuHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( SpindleSpeedDynMenuHandler.class );
    
    @Inject
    private Toolbox toolbox;

    // @formatter:off
    @AboutToShow
    public void aboutToShow ( 
            List<MMenuElement> items, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_SPEED_ENTRY_1) int spindleSpeed1, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_SPEED_ENTRY_2) int spindleSpeed2, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_SPEED_ENTRY_3) int spindleSpeed3 
    ) {
    // @formatter:on

        LOG.debug ( "SpindleSpeedDynMenuHandler:" );
        
        for ( int speedEntry : new int [] { spindleSpeed1, spindleSpeed2, spindleSpeed3 } ) {
            toolbox.addMenuItemTo ( items, false, ICommandId.SPINDLE_SPEED, ICommandId.SPINDLE_SPEED_PARAMETER, "" + speedEntry + " rpm" );
        }
        

    }

}
