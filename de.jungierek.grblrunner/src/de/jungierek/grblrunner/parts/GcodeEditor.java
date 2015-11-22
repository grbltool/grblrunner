 
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
import org.eclipse.e4.ui.di.UIEventTopic;
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
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class GcodeEditor {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditor.class );

    private static final String GROUP_NAME = "Gcode File";

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    private ESelectionService selectionService;

    // prevent from garbage collection
    @SuppressWarnings("unused")
    private GcodeFileGroup gcodeFileGroup;

    private Text gcodeText;

	@PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, Display display ) {
	    
        LOG.debug ( "postConstruct: program=" + gcodeProgram );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, false ) );
        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols ); // all groups have a width of 1 column
        
        context.set ( IGcodeProgram.class, gcodeProgram );

        // collect groups
        gcodeFileGroup = ContextInjectionFactory.make ( GcodeFileGroup.class, context );

        gcodeText = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
        gcodeText.setFont ( new Font ( display, "Courier", 10, SWT.NONE ) );
        gcodeText.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        // largeFont = FontDescriptor.createFrom ( gcodeModeLabel.getFont () ).setStyle ( IPreferences.GCODE_LARGE_FONT_STYLE ).setHeight ( IPreferences.GCODE_LARGE_FONT_SIZE
        // ).createFont ( gcodeModeLabel.getDisplay () );
        gcodeText.setEditable ( false );

        gcodeText.setText ( "..." );
		
	}
	
    @PreDestroy
    public void preDestroy () {

        gcodeProgram = null;

    }

    @Focus
    public void focus () {

        LOG.info ( "focus: program=" + gcodeProgram );

        selectionService.setSelection ( gcodeProgram );

    }

	@Persist
	public void save() {
		
	}
	
    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.PLAYER_LOADED) String fileName ) {

        LOG.info ( "playerLoadedNotified: fileName=" + fileName );

        File gcodeProgramFile = gcodeProgram.getGcodeProgramFile ();
        LOG.info ( "playerLoadedNotified: programFile=" + gcodeProgramFile );
        if ( gcodeProgramFile != null && gcodeProgramFile.getPath ().equals ( fileName ) ) {

            LOG.info ( "playerLoadedNotified: read gcode" );

            gcodeText.setText ( "" );
            StringBuilder sb = new StringBuilder ();

            gcodeProgram.visit ( new IGcodeModelVisitor () {

                @Override
                public void visit ( IGcodeLine gcodeLine ) {
                    sb.append ( gcodeLine.getLine () + "\n" );
                }

            } );

            gcodeText.setText ( "" + sb );

        }


    }

}