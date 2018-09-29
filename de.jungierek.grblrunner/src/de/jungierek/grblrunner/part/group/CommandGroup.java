package de.jungierek.grblrunner.part.group;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
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

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.gcode.IGrblResponse;
import de.jungierek.grblrunner.tool.GuiFactory;

@SuppressWarnings("restriction")
public class CommandGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( CommandGroup.class );

    private static final String GROUP_NAME = "Command";

    @Inject
    private IGcodeService gcodeService;

    private Text commandText;
    private Button commandSendButton;

    private CommandProcessor commandHistory;
    private Command currentCommand;
    private Command lastExecutedCommand;

    // preference
    private int historyDepth = 20;
    private boolean historyWithNoError = true;

    public void setFocus () {

        commandText.setFocus ();

    }

    @Inject
    void setHistoryDepth ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COMMAND_HISTORY_DEPTH) int historyDepth ) {

        LOG.debug ( "setHistoryDepth: depth=" + historyDepth + " history=" + commandHistory );

        this.historyDepth = historyDepth;
        if ( commandHistory != null ) commandHistory.adjustDepth ();

    }

    @Inject
    public void setHistoryWithNoError ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.COMMAND_HISTORY_WITHNOWERROR) boolean historyWithNoErrir ) {

        historyWithNoError = historyWithNoErrir;

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        final int cols = 8;
        group.setLayout ( new GridLayout ( cols, true ) );

        commandText = new Text ( group, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        commandText.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, cols - 1, 1 ) );
        commandText.setEnabled ( false );
        commandSendButton = GuiFactory.createPushButton ( group, "send", SWT.FILL, true );

        commandHistory = new CommandProcessor ();

        commandText.addTraverseListener ( new TraverseListener () {

            @Override
            public void keyTraversed ( TraverseEvent evt ) {

                // LOG.info ( "keyTraversed: evt.detail=" +/ evt.detail + " evt.keyCode=" + evt.keyCode + "swt=" + SWT.ARROW_RIGHT );

                if ( evt.detail == SWT.TRAVERSE_RETURN ) {
                    final String line = commandText.getText ().trim ();
                    if ( !line.isEmpty () ) {
                        lastExecutedCommand = new Command ( line );
                        commandHistory.append ( lastExecutedCommand ); // append executes command
                        commandText.setText ( "" );
                        currentCommand = null;
                    }
                }
                else if ( evt.detail == SWT.TRAVERSE_ESCAPE ) {
                    commandText.setText ( "" );
                    currentCommand = null;
                }
                else if ( evt.detail == SWT.TRAVERSE_ARROW_PREVIOUS && evt.keyCode == SWT.ARROW_UP ) {
                    currentCommand = commandHistory.getPrevious ( currentCommand );
                    if ( currentCommand == null ) commandText.setText ( "" );
                    else commandText.setText ( currentCommand.getCommand () );
                    evt.doit = false;
                }
                else if ( evt.detail == SWT.TRAVERSE_ARROW_NEXT && evt.keyCode == SWT.ARROW_DOWN ) {
                    currentCommand = commandHistory.getNext ( currentCommand );
                    if ( currentCommand == null ) commandText.setText ( "" );
                    else commandText.setText ( currentCommand.getCommand () );
                    evt.doit = false;
                }
            }

        } );

        commandSendButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                final String line = commandText.getText ().trim ();
                if ( !line.isEmpty () ) {
                    lastExecutedCommand = new Command ( line );
                    commandHistory.append ( lastExecutedCommand ); // append executes command
                    commandText.setText ( "" );
                    currentCommand = null;
                }
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
    public void receivedNotified ( @UIEventTopic(IEvent.GRBL_RECEIVED) IGrblResponse response ) {

        LOG.trace ( "receivedNotified: response=" + response );

        if ( response == null || response.getMessage () == null ) return;

        if ( lastExecutedCommand != null ) {
            String line = response.getMessage ();
            if ( line.startsWith ( "ok" ) ) {
                lastExecutedCommand = null;
            }
            else if ( line.startsWith ( "error" ) ) {
                if ( historyWithNoError ) commandHistory.remove ( lastExecutedCommand );
                lastExecutedCommand = null;
            }
        }

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

        LOG.trace ( "connectedNotified: port=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "disconnectedNotified: param=" + param );

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

        LOG.trace ( "scanStopNotified: " );

        setControlsEnabled ( true );

    }

    private class Command {

        private final Logger LOG = LoggerFactory.getLogger ( CommandGroup.Command.class );

        private String command;

        public Command ( String command ) {

            this.command = command;

        }

        public void execute () {
            
            try {
                gcodeService.sendCommand ( command );
            }
            catch ( InterruptedException exc ) {
                LOG.info ( "execute: interrupted exception for command=" + command );
            }

        }

        public String getCommand () {

            return command;

        }

    }

    private class CommandProcessor {

        private ArrayList<Command> history = new ArrayList<> ( historyDepth );

        public CommandProcessor () {}

        public void append ( Command command ) {

            if ( history.size () >= historyDepth ) {
                history.remove ( 0 );
            }
            history.add ( command );

            command.execute ();

        }
        
        public void adjustDepth () {

            while ( history.size () > historyDepth )
                history.remove ( 0 );

        }

        public String [] list () {
            
            String [] result = new String [history.size ()];
            
            Command [] h = history.toArray ( new Command[history.size ()] );

            for ( int i = 0; i < h.length; i++ ) {
                result[i] = h[i].getCommand ();
            }

            return result;

        }

        public Command getPrevious ( Command command ) {

            if ( history.size () == 0 ) return null;
            if ( command == null ) return history.get ( history.size () - 1 ); // last

            int i = history.indexOf ( command );
            if ( i == -1 ) return null;

            if ( i > 0 ) i--;
            return history.get ( i );

        }

        public Command getNext ( Command command ) {

            if ( command == null || history.size () == 0 ) return null;

            int i = history.indexOf ( command );
            if ( i == -1 ) return null;

            if ( i < history.size () - 1 ) i++;
            return history.get ( i );

        }

        public void remove ( Command command ) {

            if ( command == null ) return;

            history.remove ( command );

        }

    }

}
