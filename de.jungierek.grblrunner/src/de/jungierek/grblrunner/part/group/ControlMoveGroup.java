package de.jungierek.grblrunner.part.group;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tool.CommandParameterCallback;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

public class ControlMoveGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlMoveGroup.class );

    private static final String GROUP_NAME = "Move";

    @Inject
    private Toolbox toolbox;

    private Button moveYForwardButton;
    private Button moveZUpButton;
    private Button moveXBackwardButton;
    private Button moveXForwardButton;
    private Button goZeroXYButton;
    private Button goZeroZButton;
    private CCombo rangeCombo;
    private Button moveYBackwardButton;
    private Button moveZDownButton;

    private static class MoveParameterRecord {

        public final Button button;
        public final String axis;
        public final String direction;

        public MoveParameterRecord ( Button button, String axis, String direction ) {
            
            this.button = button;
            this.axis = axis;
            this.direction = direction;
            
        }

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 9, false ) );

        // line 1
        GuiFactory.createHiddenLabel ( group, 1, true );
        GuiFactory.createHeadingLabel ( group, "XY", 3, false );
        GuiFactory.createHiddenLabel ( group, 1, true );
        GuiFactory.createHeadingLabel ( group, "Z", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, true );
        GuiFactory.createHeadingLabel ( group, "Range", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, true );

        // line 2
        GuiFactory.createHiddenLabel ( group, 1, true );
        GuiFactory.createHiddenLabel ( group, 1 );
        moveYForwardButton = GuiFactory.createArrowButton ( group, SWT.UP );
        GuiFactory.createHiddenLabel ( group, 2 );
        moveZUpButton = GuiFactory.createArrowButton ( group, SWT.UP );
        GuiFactory.createHiddenLabel ( group, 3 );

        // line 3
        GuiFactory.createHiddenLabel ( group, 1, true );
        moveXBackwardButton = GuiFactory.createArrowButton ( group, SWT.LEFT );
        goZeroXYButton = GuiFactory.createButton ( group, SWT.PUSH, "0", SWT.FILL, SWT.FILL, false, false );
        // GuiFactory.createHiddenLabel ( groupMoveXY, 1 );
        moveXForwardButton = GuiFactory.createArrowButton ( group, SWT.RIGHT );
        GuiFactory.createHiddenLabel ( group );
        goZeroZButton = GuiFactory.createButton ( group, SWT.PUSH, "0", SWT.FILL, SWT.FILL, false, false );
        GuiFactory.createHiddenLabel ( group );
        rangeCombo = new CCombo ( group, SWT.DROP_DOWN | SWT.BORDER | SWT.RIGHT );
        rangeCombo.setLayoutData ( new GridData ( SWT.RIGHT, SWT.CENTER, false, false ) );
        rangeCombo.setEnabled ( false );
        rangeCombo.setItems ( new String [] { "0.01", "0.1", "1.0", "10.0" } );
        rangeCombo.select ( 2 );
        rangeCombo.addVerifyListener ( new GuiFactory.DoubleVerifyer () );
        GuiFactory.createHiddenLabel ( group, 1, true );

        // line 4
        GuiFactory.createHiddenLabel ( group, 1, true );
        GuiFactory.createHiddenLabel ( group );
        moveYBackwardButton = GuiFactory.createArrowButton ( group, SWT.DOWN );
        GuiFactory.createHiddenLabel ( group, 2 );
        moveZDownButton = GuiFactory.createArrowButton ( group, SWT.DOWN );
        GuiFactory.createHiddenLabel ( group, 3 );

        // @formatter:off
        final MoveParameterRecord [] moveParameter = new MoveParameterRecord [] {
                new MoveParameterRecord ( moveXForwardButton, "X", "+" ),
                new MoveParameterRecord ( moveXBackwardButton, "X", "-" ),
                new MoveParameterRecord ( moveYForwardButton, "Y", "+" ),
                new MoveParameterRecord ( moveYBackwardButton, "Y", "-" ),
                new MoveParameterRecord ( moveZUpButton, "Z", "+" ),
                new MoveParameterRecord ( moveZDownButton, "Z", "-" ),
        };

        for ( MoveParameterRecord moveParameterRecord : moveParameter ) {
            moveParameterRecord.button.addSelectionListener ( 
                    toolbox.createCommandExecuteSelectionListener ( 
                            ICommandId.GRBL_MOVE, 
                            createParameterMap ( moveParameterRecord.axis, moveParameterRecord.direction ),
                            this 
                    ) 
            );
        }
        
        goZeroXYButton.addSelectionListener ( 
                toolbox.createCommandExecuteSelectionListener ( 
                        ICommandId.GRBL_MOVE_ZERO, 
                        createParameterMap ( "XY" ) 
                ) 
        );

        goZeroZButton.addSelectionListener ( 
                toolbox.createCommandExecuteSelectionListener ( 
                        ICommandId.GRBL_MOVE_ZERO, 
                        createParameterMap ( "Z" ) 
                ) 
        );
        // @formatter:on

    }
    
    private Map<String, Object> createParameterMap ( String axis, String direction ) {
        
        Map<String,Object> result = new HashMap<> ();
        result.put ( ICommandId.GRBL_MOVE_AXIS_PARAMETER, axis );
        result.put ( ICommandId.GRBL_MOVE_DIRECTION_PARAMETER, direction );
        
        return result;
        
    }

    private Map<String, Object> createParameterMap ( String axis ) {

        Map<String, Object> result = new HashMap<> ();
        result.put ( ICommandId.GRBL_MOVE_ZERO_AXIS_PARAMETER, axis );

        return result;

    }

    private void setControlsEnabled ( boolean enabled ) {

        moveXForwardButton.setEnabled ( enabled );
        moveXBackwardButton.setEnabled ( enabled );
        moveYForwardButton.setEnabled ( enabled );
        moveYBackwardButton.setEnabled ( enabled );
        moveZUpButton.setEnabled ( enabled );
        moveZDownButton.setEnabled ( enabled );
        goZeroXYButton.setEnabled ( enabled );
        goZeroZButton.setEnabled ( enabled );
        rangeCombo.setEnabled ( enabled );

    }

    @Override
    public Map<String, Object> getParameter () {

        Map<String, Object> result = new HashMap<> ();
        result.put ( ICommandId.GRBL_MOVE_DISTANCE_PARAMETER, rangeCombo.getText () );

        return result;

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

        LOG.trace ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

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

        LOG.trace ( "scanStopNotified:" );

        setControlsEnabled ( true );

    }

}
