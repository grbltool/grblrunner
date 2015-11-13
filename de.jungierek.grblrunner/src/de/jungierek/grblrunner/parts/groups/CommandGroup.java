package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.IEvents;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

// TODO annotation for group @PartGroup
public class CommandGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( CommandGroup.class );

    private static final String GROUP_NAME = "Command";

    private Text commandText;
    private Button commandSendButton;

    public void setFocus () {

        commandText.setFocus ();

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, ISerialService serialService, IGcodeService gcodeService ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        final int cols = 8;
        group.setLayout ( new GridLayout ( cols, true ) );

        commandText = new Text ( group, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        commandText.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, cols - 1, 1 ) );
        commandText.setEnabled ( false );
        commandSendButton = GuiFactory.createPushButton ( group, "send", SWT.FILL, true );

        commandText.addTraverseListener ( new TraverseListener () {

            @Override
            public void keyTraversed ( TraverseEvent evt ) {
                if ( evt.detail == SWT.TRAVERSE_RETURN ) {
                    gcodeService.sendCommand ( commandText.getText () );
                    commandText.setText ( "" );
                }
                else if ( evt.detail == SWT.TRAVERSE_ESCAPE ) {
                    commandText.setText ( "" );
                }
            }

        } );

        commandSendButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                gcodeService.sendCommand ( commandText.getText () );
                commandText.setText ( "" );
                commandText.setFocus ();
            }
        } );

    }

    private void setControlsEnabled ( boolean enabled ) {

        commandSendButton.setEnabled ( enabled );
        commandText.setEnabled ( enabled );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.EVENT_GCODE_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: port=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "disconnectedNotified: param=" + param );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvents.EVENT_GCODE_GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvents.EVENT_GCODE_PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvents.EVENT_GCODE_PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.EVENT_GCODE_SCAN_START) Object dummy ) {

        LOG.trace ( "scanStartNotified:" );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.EVENT_GCODE_SCAN_STOP) Object dummy ) {

        LOG.trace ( "scanStopNotified: " );

        setControlsEnabled ( true );

    }

}
