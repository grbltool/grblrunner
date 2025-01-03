 
package de.jungierek.grblrunner.part;

import java.io.File;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPersistenceKey;
import de.jungierek.grblrunner.part.group.GcodeFileGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tool.Toolbox;

public class GcodeEditorPart {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditorPart.class );

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    private ESelectionService selectionService;

    @Inject
    private Toolbox toolbox;

    // prevent from garbage collection
    @SuppressWarnings("unused")
    private GcodeFileGroup gcodeFileGroup;

    private Text gcodeText;
    
	@PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, Display display, MPart part, EPartService partService ) {
	    
        LOG.debug ( "createGui: program=" + gcodeProgram );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, false ) );
        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_COLS, cols ); // all groups have a width of 1 column
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        
        context.set ( IGcodeProgram.class, gcodeProgram );

        // collect groups
        gcodeFileGroup = ContextInjectionFactory.make ( GcodeFileGroup.class, context );

        gcodeText = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
        gcodeText.setFont ( new Font ( display, "Courier", 10, SWT.NONE ) ); // TODO make it more portable
        gcodeText.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        // largeFont = FontDescriptor.createFrom ( gcodeModeLabel.getFont () ).setStyle ( IPreferences.GCODE_LARGE_FONT_STYLE ).setHeight ( IPreferences.GCODE_LARGE_FONT_SIZE
        // ).createFont ( gcodeModeLabel.getDisplay () );
        gcodeText.setEditable ( false );

        String path = part.getPersistedState ().get ( IPersistenceKey.EDITOR_PATH );
        if ( path != null ) {
            gcodeProgram.loadGcodeProgram ( new File ( path ) );
        }
        
        part.getTags ().add ( EPartService.REMOVE_ON_HIDE_TAG );
        partService.activate ( part );

	}
	
    @PreDestroy
    public void preDestroy () {

        LOG.debug ( "preDestroy:" );

        // gcodeProgram = null;

    }

    @Focus
    public void focus () {

        LOG.debug ( "focus: program=" + gcodeProgram );

        selectionService.setSelection ( gcodeProgram );
        gcodeText.setFocus ();

    }

	@Persist
	public void save() {

        LOG.info ( "save:" );
		
	}
	
    @PersistState
    public void persistState ( MPart part ) {
        
        LOG.debug ( "persistState:" );

        part.getPersistedState ().put ( IPersistenceKey.EDITOR_PATH, gcodeProgram.getGcodeProgramFile ().getPath () );

    }

    private void fillGcodeText ( String fileName ) {

        File gcodeProgramFile = gcodeProgram.getGcodeProgramFile ();
        if ( gcodeProgramFile != null && gcodeProgramFile.getPath ().equals ( fileName ) ) {

            toolbox.gcodeToText ( gcodeText, gcodeProgram );

        }

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        fillGcodeText ( fileName );

    }

    @Inject
    @Optional
    public void programOptimizedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_OPTIMIZED) String fileName ) {

        LOG.debug ( "programOptimizedNotified: fileName=" + fileName );

        fillGcodeText ( fileName );

    }

}