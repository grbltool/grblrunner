package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandIDs;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.PartTools;

public class SerialActionsGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialActionsGroup.class );

    private static final String GROUP_NAME = "Actions";

    @Inject
    private Display display;

    @Inject
    private PartTools partTools;

    private Button connectButton;
    private Button disconnectButton;
    private Button updateButton;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, ISerialService serialService, IGcodeService gcodeService ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_ROWS )).intValue ();
        int orientation = ((Integer) context.getActive ( IPersistenceKeys.KEY_PART_GROUP_ORIENTATION ));
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        int cols = 1;
        if ( orientation == SWT.HORIZONTAL ) {
            cols = 3;
        }
        group.setLayout ( new GridLayout ( cols, true ) );

        connectButton = GuiFactory.createPushButton ( group, "connect", SWT.FILL, true );
        connectButton.setBackground ( display.getSystemColor ( SWT.COLOR_GREEN ) );

        disconnectButton = GuiFactory.createPushButton ( group, "disconnect", SWT.FILL, true );
        disconnectButton.setBackground ( display.getSystemColor ( SWT.COLOR_RED ) );

        updateButton = GuiFactory.createPushButton ( group, "update", SWT.FILL, true );

        connectButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_SERIAL_CONNECT ) );
        disconnectButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_SERIAL_DISCONNECT ) );
        updateButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_SERIAL_UPDATE ) );

    }

    @Inject
    @Optional
    public void portsDetectingNotified ( @UIEventTopic(IEvents.SERIAL_PORTS_DETECTING) Object dummy ) {

        LOG.debug ( "portsDetectedNotified:" );

        connectButton.setEnabled ( false );
        updateButton.setEnabled ( false );

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvents.SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.debug ( "portsDetectedNotified: ports=" + ports );

        updateButton.setEnabled ( true );

    }

    @Inject
    @Optional
    public void portSelectedNotified ( @UIEventTopic(IEvents.SERIAL_PORT_SELECTED) String port ) {

        LOG.debug ( "portSelectedNotified: port=" + port );

        connectButton.setEnabled ( true );
        disconnectButton.setEnabled ( false );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        connectButton.setEnabled ( false );
        disconnectButton.setEnabled ( true );
        updateButton.setEnabled ( false );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );

        connectButton.setEnabled ( true );
        disconnectButton.setEnabled ( false );
        updateButton.setEnabled ( true );

    }

}
