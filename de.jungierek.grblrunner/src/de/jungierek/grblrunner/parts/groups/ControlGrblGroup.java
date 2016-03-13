package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class ControlGrblGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlCycleGroup.class );

    private static final String GROUP_NAME = "Grbl";

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    private PartTools partTools;

    private Button grblHomeButton;
    private Button grblUnlockButton;
    private Button grblCheckButton;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IContextKey.KEY_PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.KEY_PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 3, false ) );

        GuiFactory.createHiddenLabel ( group );
        grblHomeButton = GuiFactory.createPushButton ( group, "home", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( group );

        Label label = GuiFactory.createHiddenLabel ( group, 1 );
        label.setLayoutData ( new GridData ( SWT.LEFT, SWT.FILL, false, false, 1, 1 ) );
        grblUnlockButton = GuiFactory.createPushButton ( group, "unlock", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( group );

        GuiFactory.createHiddenLabel ( group );
        grblCheckButton = GuiFactory.createPushButton ( group, "check", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( group );

        grblHomeButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.GRBL_HOME ) );
        grblUnlockButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.GRBL_UNLOCK ) );
        grblCheckButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.GRBL_CHECK ) );

    }

    private void setControlsEnabled ( boolean enabled ) {

        grblHomeButton.setEnabled ( enabled );
        grblUnlockButton.setEnabled ( enabled );
        grblCheckButton.setEnabled ( enabled );

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

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.AUTOLEVEL_START) Object dummy ) {

        LOG.trace ( "scanStartNotified:" );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.AUTOLEVEL_STOP) Object dummy ) {

        LOG.trace ( "scanStopNotified:" );

        setControlsEnabled ( true );

    }

}