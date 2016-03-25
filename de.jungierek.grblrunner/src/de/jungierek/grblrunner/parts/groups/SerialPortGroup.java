package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.GuiFactory;

public class SerialPortGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialPortGroup.class );

    private static final String GROUP_NAME = "Ports";

    @Inject
    private ISerialService serial;

    private Combo portCombo;

    // @Focus
    // public void SetFocusToPortCombo () {
    //
    // portCombo.setFocus ();
    //
    // }
    //
    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_COLS) int groupCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        final int cols = 1;
        group.setLayout ( new GridLayout ( cols, true ) );

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

    @Inject
    @Optional
    public void portsDetectingNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTING) Object dummy ) {

        LOG.debug ( "portsDetectedNotified:" );

        portCombo.setEnabled ( false );

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.debug ( "portsDetectedNotified: ports=" + ports );

        portCombo.setItems ( ports );
        portCombo.setEnabled ( true );

    }

    @Inject
    @Optional
    public void portSelectedNotified ( @UIEventTopic(IEvent.SERIAL_PORT_SELECTED) String port ) {

        LOG.debug ( "portSelectedNotified: port=" + port );

        portCombo.setText ( port );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        portCombo.setEnabled ( false );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );

        portCombo.setEnabled ( true );

    }

}
