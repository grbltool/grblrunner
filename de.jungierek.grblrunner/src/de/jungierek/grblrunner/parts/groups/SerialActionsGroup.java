package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class SerialActionsGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialActionsGroup.class );

    private static final String GROUP_NAME = "Actions";

    @Inject
    private PartTools partTools;
    
    @Inject
    private Display display;

    private Button connectButton;
    private Button disconnectButton;
    private Button updateButton;
    
    private Color connectButtonColor; // from prefrerences
    private Color disconnectButtonColor; // from prefrerences
    
    @Inject
    public void setConnectButtonColor ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.COLOR_CONNECT) String rgbText ) {
        
        connectButtonColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        if ( connectButton != null ) connectButton.setBackground ( connectButtonColor );

    }

    @Inject
    public void setDisconnectButtonColor ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.COLOR_DISCONNECT) String rgbText ) {

        disconnectButtonColor = new Color ( display, StringConverter.asRGB ( rgbText ) );
        if ( disconnectButton != null ) disconnectButton.setBackground ( disconnectButtonColor );

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.KEY_PART_COLS) int partCols, @Named(IContextKey.KEY_PART_GROUP_COLS) int groupCols, @Named(IContextKey.KEY_PART_GROUP_ROWS) int groupRows, @Named(IContextKey.KEY_PART_GROUP_ORIENTATION) int orientation ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        int cols = 1;
        if ( orientation == SWT.HORIZONTAL ) {
            cols = 3;
        }
        group.setLayout ( new GridLayout ( cols, true ) );

        connectButton = GuiFactory.createPushButton ( group, "connect", SWT.FILL, true );
        connectButton.setBackground ( connectButtonColor );

        disconnectButton = GuiFactory.createPushButton ( group, "disconnect", SWT.FILL, true );
        disconnectButton.setBackground ( disconnectButtonColor );

        updateButton = GuiFactory.createPushButton ( group, "update", SWT.FILL, true );

        connectButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.SERIAL_CONNECT ) );
        disconnectButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.SERIAL_DISCONNECT ) );
        updateButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.SERIAL_UPDATE ) );

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
