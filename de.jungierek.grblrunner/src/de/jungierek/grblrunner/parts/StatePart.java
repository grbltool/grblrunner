package de.jungierek.grblrunner.parts;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeResponse;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class StatePart {

    private static final Logger LOG = LoggerFactory.getLogger ( StatePart.class );

    @Inject
    IGcodeService gcode;

    @Inject
    private MApplication application;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private static final String UNCONNECTED_TEXT = "UNCONNECTED";

    private static final String [] AXIS = { "X", "Y", "Z" };

    private static final HashMap<EGrblState, int []> stateColors;
    // TODO colors to pref
    static {
        stateColors = new HashMap<EGrblState, int []> ();
        stateColors.put ( EGrblState.IDLE, new int [] { SWT.COLOR_BLACK, SWT.COLOR_GRAY } );
        stateColors.put ( EGrblState.QUEUE, new int [] { SWT.COLOR_BLACK, SWT.COLOR_YELLOW } );
        stateColors.put ( EGrblState.RUN, new int [] { SWT.COLOR_BLACK, SWT.COLOR_GREEN } );
        stateColors.put ( EGrblState.HOLD, new int [] { SWT.COLOR_BLACK, SWT.COLOR_YELLOW } );
        stateColors.put ( EGrblState.HOME, new int [] { SWT.COLOR_BLACK, SWT.COLOR_MAGENTA } );
        stateColors.put ( EGrblState.ALARM, new int [] { SWT.COLOR_WHITE, SWT.COLOR_RED } );
        stateColors.put ( EGrblState.CHECK, new int [] { SWT.COLOR_BLACK, SWT.COLOR_CYAN } );
    };
    
    private Label [] machineCoordLabel = new Label [3];
    private Label [] workCoordLabel = new Label [3];
    private Button [] setZeroButton = new Button [3];
    private Button [] resetZeroButton = new Button [3];

    private CCombo coordSystemCombo;

    private static final String [] COORDINATE_SYSTEMS = { "G54", "G55", "G56", "G57", "G58", "G59" };

    private Label stateLabel;
    private Label feedrateLabel;
    private Label toolLabel;
    private Label spindleModeLabel;
    private Label spindlespeedLabel;
    private Label coolantLabel;
    private Label motionModeLabel;
    private Label planeLabel;
    private Label unitLabel;
    private Label distanceModeLabel;


    @Inject
    public StatePart () {}

    @PostConstruct
    public void createGui ( Composite parent ) {

        parent.setLayout ( new GridLayout ( 4, true ) );

        // state
        Group groupState = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupState.setText ( "GRBL State" );
        groupState.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 4, 1 ) );

        groupState.setLayout ( new FillLayout () );
        stateLabel = new Label ( groupState, SWT.CENTER );
        stateLabel.setText ( UNCONNECTED_TEXT );

        // coordinates
        Group groupCoordinates = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupCoordinates.setText ( "Coordinates" );
        groupCoordinates.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 4, 3 ) );
        groupCoordinates.setLayout ( new GridLayout ( 5, true ) );

        GuiFactory.createHiddenLabel ( groupCoordinates, 1 );
        GuiFactory.createHeadingLabel ( groupCoordinates, "Machine", 1 );
        GuiFactory.createHeadingLabel ( groupCoordinates, "Work", 1 );

        coordSystemCombo = new CCombo ( groupCoordinates, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER | SWT.CENTER );
        coordSystemCombo.setEnabled ( false );
        coordSystemCombo.setItems ( COORDINATE_SYSTEMS );
        coordSystemCombo.select ( 0 ); // G54
        coordSystemCombo.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        coordSystemCombo.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                gcode.sendCommandSuppressInTerminal ( ((CCombo) evt.getSource ()).getText () );
            }
        } );

        for ( int i = 0; i < AXIS.length; i++ ) {
            final String axisLetter = AXIS[i];
            GuiFactory.createHeadingLabel ( groupCoordinates, axisLetter, 1 );
            machineCoordLabel[i] = GuiFactory.createCoordinateLabel ( groupCoordinates );
            workCoordLabel[i] = GuiFactory.createCoordinateLabel ( groupCoordinates );
            setZeroButton[i] = GuiFactory.createPushButton ( groupCoordinates, "zero " + axisLetter );
            setZeroButton[i].addSelectionListener ( new SelectionAdapter () {
                @Override
                public void widgetSelected ( SelectionEvent evt ) {
                    gcode.sendCommandSuppressInTerminal ( "G10 L20 " + axisLetter + "0" );
                }
            } );
            resetZeroButton[i] = GuiFactory.createPushButton ( groupCoordinates, "reset " + AXIS[i] );
            resetZeroButton[i].addSelectionListener ( new SelectionAdapter () {
                @Override
                public void widgetSelected ( SelectionEvent evt ) {
                    gcode.sendCommandSuppressInTerminal ( "G10 L2 " + axisLetter + "0" );
                }
            } );
        }

        // feedrate
        Group groupFeedrate = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupFeedrate.setText ( "Feedrate" );
        groupFeedrate.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupFeedrate.setLayout ( new GridLayout ( 2, false ) );
        feedrateLabel = GuiFactory.createHeadingLabel ( groupFeedrate, SWT.RIGHT, "0", 1 );
        new Label ( groupFeedrate, SWT.LEFT ).setText ( "mm/min" );

        // tool
        Group groupTool = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupTool.setText ( "Tool" );
        groupTool.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupTool.setLayout ( new GridLayout ( 1, false ) );
        toolLabel = GuiFactory.createHeadingLabel ( groupTool, "", 1 );

        // spindle
        Group groupSpindle = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupSpindle.setText ( "Spindle" );
        groupSpindle.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupSpindle.setLayout ( new GridLayout ( 3, false ) );
        spindleModeLabel = GuiFactory.createHeadingLabel ( groupSpindle, SWT.LEFT, "", 1 );
        spindlespeedLabel = GuiFactory.createHeadingLabel ( groupSpindle, SWT.RIGHT, "0", 1 );
        new Label ( groupSpindle, SWT.LEFT ).setText ( "rpm" );

        // coolant
        Group groupCoolant = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupCoolant.setText ( "Coolant" );
        groupCoolant.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupCoolant.setLayout ( new GridLayout ( 1, false ) );
        coolantLabel = GuiFactory.createHeadingLabel ( groupCoolant, "", 1 );

        // modal mode
        Group groupModalMode = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupModalMode.setText ( "Modal Mode" );
        groupModalMode.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupModalMode.setLayout ( new GridLayout ( 1, false ) );
        motionModeLabel = GuiFactory.createHeadingLabel ( groupModalMode, SWT.CENTER, "", 1 );

        // Plane
        Group groupPlane = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupPlane.setText ( "Plane" );
        groupPlane.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupPlane.setLayout ( new GridLayout ( 1, false ) );
        planeLabel = GuiFactory.createHeadingLabel ( groupPlane, SWT.CENTER, "", 1 );

        // Units
        Group groupUnit = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupUnit.setText ( "Unit" );
        groupUnit.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupUnit.setLayout ( new GridLayout ( 1, false ) );
        unitLabel = GuiFactory.createHeadingLabel ( groupUnit, SWT.CENTER, "", 1 );

        // Distance
        Group groupDistance = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        groupDistance.setText ( "Distance" );
        groupDistance.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

        groupDistance.setLayout ( new GridLayout ( 1, false ) );
        distanceModeLabel = GuiFactory.createHeadingLabel ( groupDistance, SWT.CENTER, "", 1 );

    }
    
    private void setControlsEnabled ( boolean enabled ) {

        coordSystemCombo.setEnabled ( enabled );
        for ( int i = 0; i < AXIS.length; i++ ) {
            setZeroButton[i].setEnabled ( enabled );
            resetZeroButton[i].setEnabled ( enabled );
        }

    }

    private void setStateLabel ( EGrblState state ) {
        
        LOG.trace ( "setStateLabel: state=" + state );

        stateLabel.setText ( state.getText () );

        int [] colors = stateColors.get ( state );
        stateLabel.setForeground ( shell.getDisplay ().getSystemColor ( colors[0] ) );
        stateLabel.setBackground ( shell.getDisplay ().getSystemColor ( colors[1] ) );

    }

    private void saveCoordinateSystem ( String coordSelect ) {
        application.getPersistedState ().put ( IPersistenceKeys.KEY_LAST_COORDINATE_SYSTEM, coordSelect );
    }

    private void restoreCoordinateSystem () {
        String coordSelect = application.getPersistedState ().get ( IPersistenceKeys.KEY_LAST_COORDINATE_SYSTEM );
        if ( coordSelect != null && coordSelect.length () == 3 ) {
            gcode.sendCommandSuppressInTerminal ( coordSelect );
            // gcode.sendCommand ( coordSelect );
        }
    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.GRBL_ALARM) String line ) {
    
        LOG.debug ( "alarmNotified: start" );
        setStateLabel ( EGrblState.ALARM );
        // inform about whole message
        stateLabel.setText ( line );
    
    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

        restoreCoordinateSystem ();

    }

    boolean unlockSent = false;

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {
    
        LOG.debug ( "disconnectedNotified: param=" + param );
        
        stateLabel.setText ( UNCONNECTED_TEXT );
        setControlsEnabled ( false );
    
    }

    @Inject
    @Optional
    public void sentNotified ( @UIEventTopic(IEvents.GRBL_SENT) IGcodeResponse command ) {

        LOG.trace ( "sentNotified: command=" + command );

        if ( command != null && command.getLine () != null && command.getLine ().startsWith ( "$X" ) ) { // unlock
            unlockSent = true;
        }

    }

    @Inject
    @Optional
    public void receivedNotified ( @UIEventTopic(IEvents.GRBL_RECEIVED) IGcodeResponse response ) {

        if ( response == null ) {
            LOG.warn ( "receivedNotified: response == null" );
            return;
        }

        if ( response.getLine () == null ) {
            LOG.warn ( "receivedNotified: line == null" );
            return;
        }

        if ( !response.getLine ().startsWith ( "<" ) ) {
            LOG.trace ( "receivedNotified: response=" + response + " unlockSent=" + unlockSent );
        }

        if ( unlockSent && response.getLine ().startsWith ( "ok" ) ) {
            unlockSent = false;
            // first 'ok' after alarm comes with unlock an periodic $G command, then the coordinate system will be set

            // restoreCoordinateSystem ();
        }

    }

    @Inject
    @Optional
    public void updateStateNotified ( @UIEventTopic(IEvents.UPDATE_STATE) IGcodeGrblState grblState ) {

        LOG.debug ( "updateStateNotified: grblState=" + grblState );
        
        setStateLabel ( grblState.getGrblState () );
        setCoordLabels ( grblState.getMachineCoordindates ().getCooridnates (), machineCoordLabel );
        setCoordLabels ( grblState.getWorkCoordindates ().getCooridnates (), workCoordLabel );

    }

    private void setCoordLabels ( double [] coords, Label [] labels ) {
        
        for ( int i = 0; i < coords.length; i++ ) {
            labels[i].setText ( String.format ( IGcodePoint.FORMAT_COORDINATE, coords[i] ) );
        }
        
    }
    
    @Inject
    @Optional
    public void updateMotionModeNotified ( @UIEventTopic(IEvents.UPDATE_MOTION_MODE) String motionMode ) {

        LOG.trace ( "updateMotionModeNotified: motionMode=" + motionMode );
        motionModeLabel.setText ( motionMode );

    }
    
    @Inject
    @Optional
    public void updateCoordSelectNotified ( @UIEventTopic(IEvents.UPDATE_FIXTURE) String coordSelect ) {

        LOG.trace ( "updateCoordSelectNotified: coordSelect=" + coordSelect );
        coordSystemCombo.select ( coordSelect.charAt ( 2 ) - '4' );
        saveCoordinateSystem ( coordSelect );

    }

    @Inject
    @Optional
    public void updatePlaneNotified ( @UIEventTopic(IEvents.UPDATE_PLANE) String plane ) {

        LOG.trace ( "updatePlaneNotified: plane=" + plane );
        planeLabel.setText ( plane );

    }
    
    @Inject
    @Optional
    public void updateMetricModeNotified ( @UIEventTopic(IEvents.UPDATE_METRIC_MODE) String metricMode ) {

        LOG.trace ( "updateMetricModeNotified: metricMode=" + metricMode );
        unitLabel.setText ( metricMode );

    }
    
    @Inject
    @Optional
    public void updateToolNotified ( @UIEventTopic(IEvents.UPDATE_TOOL) String tool ) {

        LOG.trace ( "updateToolNotified: tool=" + tool );
        toolLabel.setText ( tool );

    }

    @Inject
    @Optional
    public void updateCoolantModeNotified ( @UIEventTopic(IEvents.UPDATE_COOLANT_MODE) String coolantMode ) {

        LOG.trace ( "updateCoolantModeNotified: coolantMode=" + coolantMode );
        coolantLabel.setText ( coolantMode );

    }

    @Inject
    @Optional
    public void updateSpindleModeNotified ( @UIEventTopic(IEvents.UPDATE_SPINDLE_MODE) String spindleMode ) {

        LOG.trace ( "updateSpindleModeNotified: tool=" + spindleMode );

        spindleModeLabel.setText ( spindleMode );

    }

    @Inject
    @Optional
    public void updateFeedrateNotified ( @UIEventTopic(IEvents.UPDATE_FEEDRATE) String feedrate ) {

        LOG.trace ( "updateFeedrateNotified: feedrate=" + feedrate );
        feedrateLabel.setText ( feedrate );

    }
    
    @Inject
    @Optional
    public void updateSpindlespeedNotified ( @UIEventTopic(IEvents.UPDATE_SPINDLESPEED) String spindlespeed ) {

        LOG.trace ( "updateSpindlespeedNotified: spindlespeed=" + spindlespeed );
        spindlespeedLabel.setText ( spindlespeed );

    }
    
    @Inject
    @Optional
    public void updateDistanceModeNotified ( @UIEventTopic(IEvents.UPDATE_DISTANCE_MODE) String distanceMode ) {

        LOG.trace ( "updateDistanceModeNotified: distanceMode=" + distanceMode );
        distanceModeLabel.setText ( distanceMode );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvents.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );
        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvents.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );
        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.AUTOLEVEL_START) Object dummy ) {

        LOG.trace ( "scanStartNotified:" );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.AUTOLEVEL_STOP) Object dummy ) {

        LOG.trace ( "scanStopNotified:" );

        setControlsEnabled ( true );

    }

}