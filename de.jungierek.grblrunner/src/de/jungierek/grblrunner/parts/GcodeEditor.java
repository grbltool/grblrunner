 
package de.jungierek.grblrunner.parts;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.parts.groups.GcodeFileGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class GcodeEditor {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditor.class );

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    private ESelectionService selectionService;

    // prevent from garbage collection
    @SuppressWarnings("unused")
    private GcodeFileGroup gcodeFileGroup;

    private Text gcodeText;
    
	@PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, Display display, MPart part ) {
	    
        LOG.debug ( "createGui: program=" + gcodeProgram );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, false ) );
        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols ); // all groups have a width of 1 column
        
        context.set ( IGcodeProgram.class, gcodeProgram );

        // collect groups
        gcodeFileGroup = ContextInjectionFactory.make ( GcodeFileGroup.class, context );

        gcodeText = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
        gcodeText.setFont ( new Font ( display, "Courier", 10, SWT.NONE ) ); // TODO make it more portable
        gcodeText.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        // largeFont = FontDescriptor.createFrom ( gcodeModeLabel.getFont () ).setStyle ( IPreferences.GCODE_LARGE_FONT_STYLE ).setHeight ( IPreferences.GCODE_LARGE_FONT_SIZE
        // ).createFont ( gcodeModeLabel.getDisplay () );
        gcodeText.setEditable ( false );

        String path = part.getPersistedState ().get ( IPersistenceKeys.KEY_EDITOR_PATH );
        if ( path != null ) {
            gcodeProgram.loadGcodeProgram ( new File ( path ) );
        }

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

    }

	@Persist
	public void save() {

        LOG.info ( "save:" );
		
	}
	
    @PersistState
    public void persistState ( MPart part ) {
        
        LOG.debug ( "persistState:" );

        part.getPersistedState ().put ( IPersistenceKeys.KEY_EDITOR_PATH, gcodeProgram.getGcodeProgramFile ().getPath () );

    }

    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.PLAYER_LOADED) String fileName ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        File gcodeProgramFile = gcodeProgram.getGcodeProgramFile ();
        if ( gcodeProgramFile != null && gcodeProgramFile.getPath ().equals ( fileName ) ) {

            gcodeText.setText ( "" );
            StringBuilder sb = new StringBuilder ();

            for ( IGcodeLine gcodeLine : gcodeProgram.getAllGcodeLines () ) {
                sb.append ( gcodeLine.getLine () + "\n" );
            }

            gcodeText.setText ( "" + sb );

        }


    }

}