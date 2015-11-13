 
package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandIDs;
import de.jungierek.grblrunner.tools.IEvents;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.PartTools;

// TODO_PREF Auswahlfeld für Baudrate

public class SerialPart {
    
    private static final Logger LOG = LoggerFactory.getLogger ( SerialPart.class );

    private Button connectButton;
    private Button disconnectButton;
    private Button updateButton;

    // GUI Model
    private Combo portCombo;
    private Button autoConnectCheckButton;
    private Label autoConnectPortLabel;
    
    @Inject
    private ISerialService serial;

    @Inject
    private MApplication application;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    Shell shell;

    @Inject
    private PartTools partTools;

    @Focus
    public void SetFocusToPortCombo () {
        
        portCombo.setFocus ();
        
    }
    
    // @PostConstruct
    public void createGuiV2 ( Composite parent ) {

        final int cols = 3;
        parent.setLayout ( new GridLayout ( cols, true ) );

        createGroupActions ( parent, cols, "Actions", cols, 1 );
        createGroupPorts ( parent, "Ports", cols - 1, 1 );
        createGroupAutoConnect ( parent, "Auto Connect", 1, 1 );

    }

    @PostConstruct
    public void createGuiV1 ( Composite parent ) {

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );

        createGroupPorts ( parent, "Ports", cols - 1, 1 );
        createGroupActions ( parent, 1, "Actions", 1, 2 );
        createGroupAutoConnect ( parent, "Auto Connect", cols - 1, 1 );

    }

    private void createGroupPorts ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {

        Group group = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        group.setLayout ( new GridLayout ( 1, true ) );

        portCombo = new Combo ( group, SWT.DROP_DOWN | SWT.READ_ONLY );
        portCombo.setEnabled ( false );
        portCombo.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 4, 1 ) );

        portCombo.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                // TODO prove using command with parameter port
                serial.setPortName ( ((Combo) evt.getSource ()).getText () );
            }
        } );

    }

    private void createGroupAutoConnect ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
    
        String autoConnect = application.getPersistedState ().get ( IPersistenceKeys.KEY_AUTO_CONNECT );
        String autoConnectPort = application.getPersistedState ().get ( IPersistenceKeys.KEY_AUTO_CONNECT_PORT );
        // autoConnectPort = "COMX"; // for Design View
        LOG.debug ( "createGui: auto=" + autoConnect + " port=" + autoConnectPort );
    
        Group group = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, false );
        group.setLayout ( new GridLayout ( 2, false ) );
    
        autoConnectCheckButton = GuiFactory.createButton ( group, SWT.CHECK, null, SWT.FILL, SWT.CENTER, false, false );
        if ( IPersistenceKeys.AUTO_CONNECT_ON.equals ( autoConnect ) ) autoConnectCheckButton.setSelection ( true );
        autoConnectPortLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, autoConnectPort );
    
        autoConnectCheckButton.addSelectionListener ( new SelectionAdapter () {
    
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
    
                // auto connect check box goes on and connection is established
                final boolean autoConnect = ((Button) evt.getSource ()).getSelection ();
                application.getPersistedState ().put ( IPersistenceKeys.KEY_AUTO_CONNECT, autoConnect ? IPersistenceKeys.AUTO_CONNECT_ON : null );
                // if ( autoConnect && !connectButton.isEnabled () ) {
                if ( autoConnect && serial.isOpen () ) {
                    String port = serial.getPortName ();
                    if ( port != null && !port.equals ( "" ) ) {
                        autoConnectPortLabel.setText ( port );
                        application.getPersistedState ().put ( IPersistenceKeys.KEY_AUTO_CONNECT_PORT, port );
                    }
                }
    
            }
    
        } );
    
    }

    private void createGroupActions ( Composite parent, int cols, String name, int horizontalSpan, int verticalSpan ) {

        Group group = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, false );
        group.setLayout ( new GridLayout ( cols, true ) );

        connectButton = GuiFactory.createPushButton ( group, "connect", SWT.FILL, true );
        connectButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_GREEN ) );

        disconnectButton = GuiFactory.createPushButton ( group, "disconnect", SWT.FILL, true );
        disconnectButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_RED ) );

        updateButton = GuiFactory.createPushButton ( group, "update", SWT.FILL, true );

        connectButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_SERIAL_CONNECT ) );
        disconnectButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_SERIAL_DISCONNECT ) );
        updateButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_SERIAL_UPDATE ) );

    }

    @Inject
    @Optional
    public void portsDetectingNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_PORTS_DETECTING) Object dummy ) {

        LOG.debug ( "portsDetectedNotified:" );

        portCombo.setEnabled ( false );
        connectButton.setEnabled ( false );
        updateButton.setEnabled ( false );
        autoConnectCheckButton.setEnabled ( false );

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.debug ( "portsDetectedNotified: ports=" + ports );

        portCombo.setItems ( ports );
        portCombo.setEnabled ( true );
        updateButton.setEnabled ( true );
        autoConnectCheckButton.setEnabled ( true );

        String autoConnectPort = autoConnectPortLabel.getText ();
        if ( autoConnectCheckButton.getSelection () ) {
            for ( String port : ports ) {
                if ( autoConnectPort.equals ( port ) ) {
                    LOG.debug ( "portsDetectedNotified: autoconnect port=" + autoConnectPort );
                    serial.setPortName ( autoConnectPort );
                    serial.connect ();
                }
            }
        }

    }

    @Inject
    @Optional
    public void portSelectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_PORT_SELECTED) String port ) {

        LOG.debug ( "portSelectedNotified: port=" + port );

        connectButton.setEnabled ( true );
        disconnectButton.setEnabled ( false );
        portCombo.setText ( port );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        portCombo.setEnabled ( false );
        connectButton.setEnabled ( false );
        disconnectButton.setEnabled ( true );
        updateButton.setEnabled ( false );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );

        portCombo.setEnabled ( true );
        connectButton.setEnabled ( true );
        disconnectButton.setEnabled ( false );
        updateButton.setEnabled ( true );

    }

}