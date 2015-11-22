 
package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.groups.GcodeFileGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class GcodeEditor {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditor.class );

    private static final String GROUP_NAME = "Gcode File";

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private ESelectionService selectionService;

    // prevent from garbage collection
    @SuppressWarnings("unused")
    private GcodeFileGroup gcodeFileGroup;

	@PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {
	    
        LOG.debug ( "postConstruct: program=" + gcodeProgram );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, false ) );
        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols ); // all groups have a width of 1 column
        
        context.set ( IGcodeProgram.class, gcodeProgram );

        // collect groups
        gcodeFileGroup = ContextInjectionFactory.make ( GcodeFileGroup.class, context );
		
	}
	
    @Focus
    public void focus () {

        LOG.debug ( "focus:" );

        selectionService.setSelection ( gcodeProgram );

    }

	@Persist
	public void save() {
		
	}
	
}