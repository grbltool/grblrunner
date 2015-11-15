package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandIDs;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.PartTools;

public class GcodeFileGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeFileGroup.class );

    private static final String GROUP_NAME = "Gcode File";

    @Inject
    private IGcodeModel model;

    @Inject
    private PartTools partTools;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private Label fileNameLabel;
    private Label gcodeMinLabel;
    private Label gcodeMaxLabel;
    private Text gcodeRotationText;

    private Button fileLoadButton;
    private Button fileRefreshButton;
    private Button fileRunButton;

    private Button fileViewButton;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        final int cols = 9;
        group.setLayout ( new GridLayout ( cols, true ) );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "File", 1 );
        fileNameLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 7 );
        fileLoadButton = GuiFactory.createPushButton ( group, "load", SWT.FILL, true );
        fileLoadButton.setEnabled ( IPreferences.SELECT_FILE_ENABLED_FOREVER );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "min:", 1 );
        gcodeMinLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 7 );
        fileRefreshButton = GuiFactory.createPushButton ( group, "refresh", SWT.FILL, true );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "max:", 1 );
        gcodeMaxLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 7 );
        fileRunButton = GuiFactory.createPushButton ( group, "run", SWT.FILL, true );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "gcode rotation:", 1 );
        gcodeRotationText = GuiFactory.createIntegerText ( group, "0", 1, true, -90, +90 );
        if ( IPreferences.BUTTON_GCODE_DIALOG_ON ) {
            GuiFactory.createHiddenLabel ( group, 6 );
            fileViewButton = GuiFactory.createPushButton ( group, "(gcode)", SWT.FILL, true );
        }

        gcodeRotationText.addModifyListener ( new ModifyListener () {

            @Override
            public void modifyText ( ModifyEvent evt ) {
                model.rotate ( partTools.parseIntegerField ( gcodeRotationText, 0 ) );
                model.prepareAutolevelScan ();
                updateMinMax ();
                eventBroker.send ( IEvents.REDRAW, null );
            }

        } );

        fileLoadButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_GCODE_LOAD ) );
        fileRefreshButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_GCODE_REFRESH ) );
        fileRunButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_GCODE_PLAY ) );

        fileViewButton.addSelectionListener ( new SelectionAdapter () {

            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                new GcodeDialog ( shell ).open ();
            }

        } );
    }

    private class GcodeDialog extends Dialog {

        public GcodeDialog ( Shell shell ) {

            super ( shell );

        }

        @Override
        protected Control createDialogArea ( Composite parent ) {

            LOG.debug ( "createDialogArea: parent=" + parent );

            Font terminalFont = JFaceResources.getFont ( JFaceResources.TEXT_FONT );

            Composite container = (Composite) super.createDialogArea ( parent );
            GridLayout layout = new GridLayout ( 1, true );
            layout.marginRight = 5;
            layout.marginLeft = 10;
            container.setLayout ( layout );

            Text textGcodeLine = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
            textGcodeLine.setFont ( terminalFont );
            textGcodeLine.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true ) );
            textGcodeLine.setEditable ( false );

            model.visit ( new IGcodeModelVisitor () {

                private static final String JUSTIFY_PLACE = "                                                                                "; // 80x space

                @Override
                public void visit ( IGcodeLine gcodeLine ) {
                    
                    StringBuilder sb = new StringBuilder ( 200 );
                    sb.append ( "  " );
                    sb.append ( gcodeLine.getLine () );

                    if ( gcodeLine.isMotionMode () ) {

                        IGcodePoint start = gcodeLine.getStart ();
                        IGcodePoint end = gcodeLine.getEnd ();

                        int col = 30;
                        sb.append ( JUSTIFY_PLACE.substring ( 0, col - sb.length () ) );
                        sb.append ( String.format ( "%5s:  ", gcodeLine.getLineNo () ) );
                        sb.append ( String.format ( "%3s   ", gcodeLine.getGcodeMode ().getCommand () ) );

                        sb.append ( String.format ( "X%+08.3f ", start.getX () ) );
                        sb.append ( String.format ( "Y%+08.3f ", start.getY () ) );
                        sb.append ( String.format ( "Z%+08.3f   ", start.getZ () ) );

                        sb.append ( String.format ( "X%+08.3f ", end.getX () ) );
                        sb.append ( String.format ( "Y%+08.3f ", end.getY () ) );
                        sb.append ( String.format ( "Z%+08.3f   ", end.getZ () ) );
                        
                        sb.append ( String.format ( "F%s   ", gcodeLine.getFeedrate () ) );

                        // sb.append ( gcodeLine );
                    }

                    sb.append ( "\n" );

                    textGcodeLine.append ( "" + sb );

                }
            } );


            return super.createDialogArea ( parent );
        }

    }

    private void setControlsEnabled ( boolean enabled ) {

        fileLoadButton.setEnabled ( enabled );
        fileRefreshButton.setEnabled ( enabled && model.isGcodeProgramLoaded () );
        fileRunButton.setEnabled ( enabled && model.isGcodeProgramLoaded () );
        if ( IPreferences.BUTTON_GCODE_DIALOG_ON ) fileViewButton.setEnabled ( enabled && model.isGcodeProgramLoaded () );

        gcodeRotationText.setEnabled ( enabled && model.isGcodeProgramLoaded () );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.PLAYER_LOADED) String fileName ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        fileNameLabel.setText ( fileName );

        updateMinMax ();
        gcodeRotationText.setText ( String.format ( "%.0f", model.getRotationAngle () ) );

        setControlsEnabled ( true );

    }

    private void updateMinMax () {

        gcodeMinLabel.setText ( "" + model.getMin () );
        gcodeMaxLabel.setText ( "" + model.getMax () );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.GRBL_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvents.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.AUTOLEVEL_START) Object dummy ) {

        LOG.debug ( "scanStopNotified:" );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.AUTOLEVEL_STOP) Object dummy ) {

        LOG.debug ( "scanStopNotified:" );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvents.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvents.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        setControlsEnabled ( true );

    }

}
