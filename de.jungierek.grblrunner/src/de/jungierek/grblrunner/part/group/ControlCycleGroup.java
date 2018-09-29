package de.jungierek.grblrunner.part.group;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

public class ControlCycleGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlCycleGroup.class );

    private static final String GROUP_NAME = "Cycle";

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    private Toolbox toolbox;

    private Button cycleHoldButton;
    private Button cycleStartButton;
    private Button cycleResetButton;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 3, false ) );

        GuiFactory.createHiddenLabel ( group );
        cycleStartButton = GuiFactory.createPushButton ( group, "start", SWT.FILL, true );
        cycleStartButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_GREEN ) );
        GuiFactory.createHiddenLabel ( group );

        GuiFactory.createHiddenLabel ( group );
        cycleHoldButton = GuiFactory.createPushButton ( group, "hold", SWT.FILL, true );
        cycleHoldButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_YELLOW ) );
        GuiFactory.createHiddenLabel ( group );

        GuiFactory.createHiddenLabel ( group );
        cycleResetButton = GuiFactory.createPushButton ( group, "reset", SWT.FILL, true );
        cycleResetButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_RED ) );
        GuiFactory.createHiddenLabel ( group );

        cycleStartButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.CYCLE_START ) );
        cycleHoldButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.CYCLE_PAUSE ) );
        cycleResetButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.CYCLE_RESET ) );

    }

    private void setControlsEnabled ( boolean enabled ) {

        cycleHoldButton.setEnabled ( enabled );
        cycleStartButton.setEnabled ( enabled );
        cycleResetButton.setEnabled ( enabled );

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

}
