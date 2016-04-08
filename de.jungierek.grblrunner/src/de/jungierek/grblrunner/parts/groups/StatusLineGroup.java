package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tools.GuiFactory;

public class StatusLineGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StatusLineGroup.class );

    private static final String GROUP_NAME = "Status";

    private Label statusLabel;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IContextKey.PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        final int cols = 1;
        group.setLayout ( new GridLayout ( cols, true ) );

        statusLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "Status", 1 );

    }

    @Inject
    @Optional
    public void applicationStartedNotified ( @UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Object event ) {

        LOG.trace ( "applicationStarted: event=" + event ); // MApplication

        statusLabel.setText ( "retrieving COM-Ports ..." );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.debug ( "alarmNotified: line=" + line );

        statusLabel.setText ( line );

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTED) String [] ports ) {

        LOG.trace ( "portsDetectedNotified: ports=" + ports );

        String msg = "COM-Ports detected";
        if ( ports.length == 0 ) msg = "no " + msg;

        statusLabel.setText ( msg );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String port ) {

        LOG.trace ( "connectedNotified: port=" + port );

        statusLabel.setText ( "grbl connected on port " + port );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String port ) {

        LOG.trace ( "connectedNotified: port=" + port );

        statusLabel.setText ( "grbl disconnected" );

    }

    private String startMsg;

    @SuppressWarnings("deprecation")
    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String timestamp ) {

        LOG.trace ( "playerStartNotified: timestamp=" + timestamp );

        startMsg = "runnning gcode program since " + timestamp + " ... ";
        statusLabel.setText ( startMsg );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String timestamp ) {

        LOG.trace ( "playerStopNotified: timestamp=" + timestamp );

        statusLabel.setText ( startMsg + "finsihed at " + timestamp );
        startMsg = null;

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) String timestamp ) {

        LOG.trace ( "scanStartNotified:" );

        startMsg = "scanning probe data since " + timestamp + " ... ";
        statusLabel.setText ( startMsg );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvent.AUTOLEVEL_STOP) String timestamp ) {

        LOG.trace ( "scanStopNotified: " );

        statusLabel.setText ( startMsg + "finsihed at " + timestamp );

    }

}
