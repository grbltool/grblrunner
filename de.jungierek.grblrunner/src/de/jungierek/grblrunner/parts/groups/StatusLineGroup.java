package de.jungierek.grblrunner.parts.groups;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tools.GuiFactory;

public class StatusLineGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StatusLineGroup.class );

    private static final String GROUP_NAME = "Status";

    @Inject
    private Shell shell;

    @Inject
    private Display display;

    private Label statusLabel;
    private Label statusHistoryLabel;

    private int statusHistoryDepth;
    private ArrayList<String> statusHistory;

    @Inject
    public void setStatusHistoryDepth ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.STATUS_HISTORY_DEPTH) int depth ) {

        LOG.debug ( "setStatusHistoryDepth: depth=" + depth );
        statusHistoryDepth = depth;
        trimStatusHistory ();

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );
        
        statusHistory = new ArrayList<String> ( statusHistoryDepth + 1 );

        int partCols = ((Integer) context.get ( IContextKey.PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        final int cols = 1;
        group.setLayout ( new GridLayout ( cols, true ) );

        statusLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "Status", 1 );

        statusLabel.addMouseListener ( new MouseAdapter () {

            private Shell statusHistoryDialog;

            @Override
            public void mouseUp ( MouseEvent evt ) {

                LOG.debug ( "mouseUp:" );

                statusHistoryDialog.close ();

            }

            @Override
            public void mouseDown ( MouseEvent evt ) {

                LOG.debug ( "mouseDown:" );
                
                statusHistoryDialog = new Shell ( shell, SWT.NO_TRIM | SWT.BORDER );
                statusHistoryDialog.setBackground ( display.getSystemColor ( SWT.COLOR_GRAY ) );
                statusHistoryDialog.setLayout ( new GridLayout () );

                statusHistoryLabel = new Label ( statusHistoryDialog, SWT.NONE );
                statusHistoryLabel.setLayoutData ( new GridData ( SWT.FILL, SWT.TOP, true, false ) );

                String text = null;
                for ( String t : statusHistory ) {
                    if ( text == null ) text = t;
                    else text += "\n" + t;
                }
                statusHistoryLabel.setText ( text );
                
                final Control refWidget = statusLabel.getParent ();
                Point groupSize = refWidget.getSize ();
                Point p = refWidget.toDisplay ( 0, 0 );
                Point dialogSize = statusHistoryDialog.computeSize ( groupSize.x, SWT.DEFAULT );
                statusHistoryDialog.setLocation ( p.x, p.y - dialogSize.y );
                statusHistoryDialog.setSize ( dialogSize );

                statusHistoryDialog.open ();

            }

        } );

    }

    private void setStatusLine ( String line ) {

        statusLabel.setText ( line );
        
        statusHistory.add ( line );
        trimStatusHistory ();

    }

    private void trimStatusHistory () {

        if ( statusHistory == null ) return;

        while ( statusHistory.size () > statusHistoryDepth ) {
            statusHistory.remove ( 0 ); // remove oldest first
        }

    }

    @Inject
    @Optional
    public void applicationStartedNotified ( @UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Object event ) {

        LOG.trace ( "applicationStarted: event=" + event ); // MApplication

        setStatusLine ( "retrieving COM-Ports ..." );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.debug ( "alarmNotified: line=" + line );

        setStatusLine ( line );

    }

    @Inject
    @Optional
    public void portsDetectedNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTED) String [] portNames ) {

        LOG.trace ( "portsDetectedNotified: ports=" + portNames );

        String msg = "COM-Ports detected";
        if ( portNames.length == 0 ) {
            msg = "no " + msg;
        }
        else {
            String ports = null;
            for ( String port : portNames ) {
                if ( ports == null ) ports = port;
                else ports += "," + port;
            }
            msg += " " + ports;
        }

        setStatusLine ( msg );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String port ) {

        LOG.trace ( "connectedNotified: port=" + port );

        setStatusLine ( "grbl connected on port " + port );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String port ) {

        LOG.trace ( "connectedNotified: port=" + port );

        setStatusLine ( "grbl disconnected" );

    }

    private String startMsg;

    @SuppressWarnings("deprecation")
    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String timestamp ) {

        LOG.trace ( "playerStartNotified: timestamp=" + timestamp );

        startMsg = "runnning gcode program since " + timestamp + " ... ";
        setStatusLine ( startMsg );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String timestamp ) {

        LOG.trace ( "playerStopNotified: timestamp=" + timestamp );

        setStatusLine ( startMsg + "finsihed at " + timestamp );
        startMsg = null;

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) String timestamp ) {

        LOG.trace ( "scanStartNotified:" );

        startMsg = "scanning probe data since " + timestamp + " ... ";
        setStatusLine ( startMsg );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvent.AUTOLEVEL_STOP) String timestamp ) {

        LOG.trace ( "scanStopNotified: " );

        setStatusLine ( startMsg + "finsihed at " + timestamp );

    }

}
