package de.jungierek.grblrunner.part.group;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

public class ControlInfoGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlInfoGroup.class );

    private static final String GROUP_NAME = "Info";

    @Inject
    private Toolbox toolbox;

    private Button grblHelpButton;
    private Button grblSettingsButton;
    private Button grblCoordinatesButton;
    private Button grblModesButton;
    private Button grblInfoButton;
    private Button grblStartupBlocksButton;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 6, false ) );

        grblHelpButton = GuiFactory.createPushButton ( group, "$", SWT.FILL, true );
        grblSettingsButton = GuiFactory.createPushButton ( group, "$$", SWT.FILL, true );
        grblCoordinatesButton = GuiFactory.createPushButton ( group, "$#", SWT.FILL, true );
        grblModesButton = GuiFactory.createPushButton ( group, "$G", SWT.FILL, true );
        grblInfoButton = GuiFactory.createPushButton ( group, "$I", SWT.FILL, true );
        grblStartupBlocksButton = GuiFactory.createPushButton ( group, "$N", SWT.FILL, true );
        
        grblHelpButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.GRBL_HELP ) );
        grblSettingsButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.GRBL_SETTINGS ) );
        grblCoordinatesButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.GRBL_COORIDNATES ) );
        grblModesButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.GRBL_MODES ) );
        grblInfoButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.GRBL_INFO ) );
        grblStartupBlocksButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.GRBL_STARTUP ) );

    }

    private void setControlsEnabled ( boolean enabled ) {

        grblHelpButton.setEnabled ( enabled );
        grblSettingsButton.setEnabled ( enabled );
        grblCoordinatesButton.setEnabled ( enabled );
        grblModesButton.setEnabled ( enabled );
        grblInfoButton.setEnabled ( enabled );
        grblStartupBlocksButton.setEnabled ( enabled );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvent.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) Object dummy ) {

        LOG.trace ( "scanStartNotified:" );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvent.AUTOLEVEL_STOP) Object dummy ) {

        LOG.trace ( "scanStopNotified:" );

        setControlsEnabled ( true );

    }

}
