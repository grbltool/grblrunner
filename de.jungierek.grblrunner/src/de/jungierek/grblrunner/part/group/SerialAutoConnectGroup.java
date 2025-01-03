package de.jungierek.grblrunner.part.group;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPersistenceKey;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tool.GuiFactory;

public class SerialAutoConnectGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialAutoConnectGroup.class );

    private static final String GROUP_NAME = "Autoconnect";

    @Inject
    private MApplication application;

    @Inject
    private ISerialService serial;

    private Button autoConnectCheckButton;
    private Label autoConnectPortLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        String autoConnect = application.getPersistedState ().get ( IPersistenceKey.AUTO_CONNECT );
        String autoConnectPort = application.getPersistedState ().get ( IPersistenceKey.AUTO_CONNECT_PORT );
        // autoConnectPort = "COMX"; // for Design View
        LOG.debug ( "createGui: auto=" + autoConnect + " port=" + autoConnectPort );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        final int cols = 2;
        group.setLayout ( new GridLayout ( cols, false ) );

        autoConnectCheckButton = GuiFactory.createButton ( group, SWT.CHECK, null, SWT.FILL, SWT.CENTER, false, false );
        if ( IPersistenceKey.AUTO_CONNECT_ON.equals ( autoConnect ) ) autoConnectCheckButton.setSelection ( true );
        autoConnectPortLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, autoConnectPort );

        autoConnectCheckButton.addSelectionListener ( new SelectionAdapter () {

            @Override
            public void widgetSelected ( SelectionEvent evt ) {

                // auto connect check box goes on and connection is established
                final boolean autoConnect = ((Button) evt.getSource ()).getSelection ();
                application.getPersistedState ().put ( IPersistenceKey.AUTO_CONNECT, autoConnect ? IPersistenceKey.AUTO_CONNECT_ON : null );
                // if ( autoConnect && !connectButton.isEnabled () ) {
                if ( autoConnect && serial.isOpen () ) {
                    String port = serial.getPortName ();
                    if ( port != null && !port.equals ( "" ) ) {
                        autoConnectPortLabel.setText ( port );
                        application.getPersistedState ().put ( IPersistenceKey.AUTO_CONNECT_PORT, port );
                    }
                }

            }

        } );

    }

    @Inject
    @Optional
    public void portsDetectingNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTING) Object dummy ) {

        LOG.debug ( "portsDetectedNotified:" );

        autoConnectCheckButton.setEnabled ( false );

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.debug ( "portsDetectedNotified: ports=" + ports );

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

}
