package de.jungierek.grblrunner.part;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPersistenceKey;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.part.group.GcodeFileGroup;
import de.jungierek.grblrunner.part.group.MacroDxfGroup;
import de.jungierek.grblrunner.part.group.MacroGroup;
import de.jungierek.grblrunner.part.group.MacroHobbedBoltGroup;
import de.jungierek.grblrunner.part.group.MacroPocketGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tool.Toolbox;

@SuppressWarnings("restriction")
public class MacroPart {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroPart.class );
    
    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    private ESelectionService selectionService;

    @Inject
    private Toolbox toolbox;

    // prevent from garbage collection
    private MacroGroup macroGroup;

    // prevent from garbage collection
    @SuppressWarnings("unused")
    private GcodeFileGroup gcodeFileGroup;

    private Text gcodeText;
    private String macroType;

    public void restorePreferenceData () {

        macroGroup.restorePreferenceData ();

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, Display display, MPart part, MApplication application, Shell shell, @Preference(
            nodePath = IConstant.PREFERENCE_NODE,
            value = IPreferenceKey.GCODE_PATH) String gcodePath ) {

        LOG.debug ( "createGui: program=" + gcodeProgram );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, false ) );
        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_COLS, cols ); // all groups have a width of 1 column

        context.set ( IGcodeProgram.class, gcodeProgram );

        // collect groups
        gcodeFileGroup = ContextInjectionFactory.make ( GcodeFileGroup.class, context );
        macroType = part.getPersistedState ().get ( IContextKey.MACRO_TYPE );
        if ( macroType == null ) {
            macroType = (String) context.get ( IContextKey.MACRO_TYPE );
        }
        switch ( macroType ) {

            case "hobbed_bolt":
                macroGroup = ContextInjectionFactory.make ( MacroHobbedBoltGroup.class, context );
                break;

            case "pocket":
                macroGroup = ContextInjectionFactory.make ( MacroPocketGroup.class, context );
                break;

            case "dxf":

                FileDialog dialog = new FileDialog ( shell, SWT.OPEN );
                dialog.setFilterExtensions ( IConstant.DXF_FILE_EXTENSIONS );

                String filterPath = application.getPersistedState ().get ( IPersistenceKey.GCODE_PATH );
                if ( filterPath == null ) filterPath = gcodePath;
                dialog.setFilterPath ( filterPath );

                String result = dialog.open ();
                if ( result != null ) {
                    application.getPersistedState ().put ( IPersistenceKey.GCODE_PATH, filterPath );
                    context.set ( IContextKey.MACRO_DXF_FILE_NAME, result );
                    macroGroup = ContextInjectionFactory.make ( MacroDxfGroup.class, context );
                }
                else {
                    // TODO what happens, when no file is selected
                    MessageDialog.openError ( shell, "Internal Error", "no dxf file selected" );
                }


                break;

            default:
                break;

        }

        gcodeText = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
        gcodeText.setFont ( new Font ( display, "Courier", 10, SWT.NONE ) ); // TODO make it more portable
        gcodeText.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        // largeFont = FontDescriptor.createFrom ( gcodeModeLabel.getFont () ).setStyle ( IPreferences.GCODE_LARGE_FONT_STYLE ).setHeight ( IPreferences.GCODE_LARGE_FONT_SIZE
        // ).createFont ( gcodeModeLabel.getDisplay () );
        gcodeText.setEditable ( false );

        context.set ( IConstant.MACRO_TEXT_ID, gcodeText );

        toolbox.gcodeToText ( gcodeText, gcodeProgram );

        // TODO reload parameters
        // String path = part.getPersistedState ().get ( IPersistenceKey.EDITOR_PATH );

    }

    @PersistState
    public void persistState ( MPart part ) {

        LOG.debug ( "persistState:" );

        part.getPersistedState ().put ( IContextKey.MACRO_TYPE, macroType );

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
    public void save () {

        LOG.info ( "save:" );

    }
    
    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String timestamp ) {

        LOG.debug ( "playerStartNotified: isPlaying=" + gcodeProgram.isPlaying () );

        if ( gcodeProgram.isPlaying () ) {
            macroGroup.setControlsEnabled ( false );
        }

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String timestamp ) {

        LOG.debug ( "playerStartNotified: isPlaying=" + gcodeProgram.isPlaying () );

        if ( gcodeProgram.isPlaying () ) {
            macroGroup.setControlsEnabled ( true );
        }

    }

}
