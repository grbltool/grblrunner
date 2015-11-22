package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandIDs;
import de.jungierek.grblrunner.tools.PartTools;

public class ControlPart {
    
    private static final Logger LOG = LoggerFactory.getLogger ( ControlPart.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private IGcodeService gcodeService;

    @Inject
    private PartTools partTools; 
    
    private Button probeStartButton;
    private Slider spindleVelocitySlider;
    private Button spindleStartButton;
    private Button spindleStopButton;
    private Button grblHelpButton;
    private Button grblSettingsButton;
    private Button grblParameterButton;
    private Button grblParserButton;
    private Button grblInfoButton;
    private Button grblStartupBlocksButton;
    private Button grblHomeButton;
    private Button grblUnlockButton;
    private Button grblCheckButton;
    private Button cycleHoldButton;
    private Button cycleStartButton;
    private Button cycleResetButton;
    private Button moveYForwardButton;
    private Button moveZUpButton;
    private Button moveXBackwardButton;
    private Button moveXForwardButton;
    private Button goZeroXYButton;
    private Button goZeroZButton;
    private CCombo rangeCombo;
    private Button moveYBackwardButton;
    private Button moveZDownButton;

    private Button scanStartButton;
    private Button scanClearButton;
    private Button loadProbeDataButton;
    private Button saveProbeDataButton;

    private boolean ignoreSpindleSpeedUpdate = false;
    private boolean ignoreStepTextModifyListener = false;

    // this are model vars
    private Text probeDepthText;
    private Text scanStepXText;
    private Text scanStepYText;
    private Label scanStepWidthXLabel;
    private Label scanStepWidthYLabel;
    private Text scanMaxZText;
    private Text scanMinZText;
    private Text scanClearanceZText;
    private Text scanFeedrateText;

    private IGcodeProgram gcodeProgram;

    @Inject
	public ControlPart() {}
	
    @PostConstruct
    public void createGui ( Composite parent ) {

	    int cols = 8;
        parent.setLayout ( new GridLayout ( cols, true ) ); // equal width column
        
        // settings $$, parameters $#, parser $G, build $I, startup $N
        createGroupSettings ( parent, "Înfo", cols, 1 );
        
        // cycle start, feed hold, reset
        createGroupCycle ( parent, "Cycle", cols / 4, 2 );

        // XY
        createGroupMove ( parent, "Move", cols/2, 2 );
        
        // home $H, unlock $X, check $C
        createGroupGrbl ( parent, "Basics", cols / 4, 2 );

        // spindle
        createGroupSpindle ( parent, "Spindle", 5, 1 );
        
        // probe
        createGroupProbe ( parent, "Z-Probe", 3, 1 );
        
        // scan
        createGroupScan ( parent, "Autolevel", cols, 1 );

    }
	
    @Focus
    public void focus () {}

    private void setGridFields () {
    
        if ( gcodeProgram == null ) return;

        ignoreStepTextModifyListener = true;
        scanStepXText.setText ( "" + gcodeProgram.getXSteps () );
        scanStepYText.setText ( "" + gcodeProgram.getYSteps () );
        ignoreStepTextModifyListener = false;
    
        scanStepWidthXLabel.setText ( String.format ( "%.3f", gcodeProgram.getStepWidthX () ) );
        scanStepWidthYLabel.setText ( String.format ( "%.3f", gcodeProgram.getStepWidthY () ) );
    
    }

    @Inject
    public void setGcodeProgram ( @Optional @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.debug ( "setGcodeProgram: program=" + program );

        gcodeProgram = program;
        setGridFields ();

    }

    private class SimpleGrblCommandSelectionListener extends SelectionAdapter {
        
        private String simpleCommand = "NO_COMMAND";
        
        public SimpleGrblCommandSelectionListener ( String simpleCommand ) {
            
            this.simpleCommand = simpleCommand;
            
        }

        @Override
        public void widgetSelected ( SelectionEvent evt ) {

            gcodeService.sendCommand ( simpleCommand );
            
        }

    }

    private class SimpleGrblCommandSelectionListenerSuppressedInTerminal extends SelectionAdapter {

        private String simpleCommand = "NO_COMMAND";

        public SimpleGrblCommandSelectionListenerSuppressedInTerminal ( String simpleCommand ) {

            this.simpleCommand = simpleCommand;

        }

        @Override
        public void widgetSelected ( SelectionEvent evt ) {

            gcodeService.sendCommandSuppressInTerminal ( simpleCommand );

        }

    }

    private class MoveGrblCommandSelectionListener extends SelectionAdapter {
        
        private char axis;
        private char dirSign;
        
        public MoveGrblCommandSelectionListener ( char axis, char dirSign ) {
            
            this.axis = axis;
            this.dirSign = dirSign;
            
        }
        
        @Override
        public void widgetSelected ( SelectionEvent evt ) {
            
            gcodeService.sendCommandSuppressInTerminal ( "G91G0" + axis + (dirSign == ' ' ? "" : dirSign) + rangeCombo.getText () );
            
        }
        
    }

    private void updateGrid () {

        int xSteps = partTools.parseIntegerField ( scanStepXText, 1 );
        int ySteps = partTools.parseIntegerField ( scanStepYText, 1 );

        // TODO test gcodeProgram for != null?
        gcodeProgram.prepareAutolevelScan ( xSteps, ySteps ); // resets scan completed
        scanStepWidthXLabel.setText ( String.format ( IConstants.FORMAT_COORDINATE, gcodeProgram.getStepWidthX () ) );
        scanStepWidthYLabel.setText ( String.format ( IConstants.FORMAT_COORDINATE, gcodeProgram.getStepWidthY () ) );
        
        redrawGcode ();

    }

    private void redrawGcode () {

        eventBroker.send ( IEvents.REDRAW, null );

    }

    private final ModifyListener updateViewModifyListener = new ModifyListener () {
        @Override
        public void modifyText ( ModifyEvent evt ) {
            if ( !ignoreStepTextModifyListener ) updateGrid ();
        }
    };

    private void createGroupScan ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {

        Group groupScan = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        final int cols = 5;
        groupScan.setLayout ( new GridLayout ( cols, true ) );

        GuiFactory.createHeadingLabel ( groupScan, "X Steps", 1, false );
        scanStepXText = GuiFactory.createIntegerText ( groupScan, "" + IPreferences.INITIAL_XSTEPS, 1, true, 0 );
        scanStepWidthXLabel = GuiFactory.createCoordinateLabel ( groupScan );
        GuiFactory.createHeadingLabel ( groupScan, "Z clear", 1, false );
        scanClearanceZText = GuiFactory.createDoubleText ( groupScan, String.format ( "%+.1f", IPreferences.PROBE_Z_CLEARANCE ), 1, true );

        GuiFactory.createHeadingLabel ( groupScan, "Y Steps", 1, false );
        scanStepYText = GuiFactory.createIntegerText ( groupScan, "" + IPreferences.INITIAL_YSTEPS, 1, true, 0 );
        scanStepWidthYLabel = GuiFactory.createCoordinateLabel ( groupScan );
        GuiFactory.createHeadingLabel ( groupScan, "Z max", 1, false );
        scanMaxZText = GuiFactory.createDoubleText ( groupScan, String.format ( "%+.1f", IPreferences.PROBE_Z_MAX ), 1, true );

        GuiFactory.createHiddenLabel ( groupScan, 3, true );
        GuiFactory.createHeadingLabel ( groupScan, "Z min", 1, false );
        scanMinZText = GuiFactory.createDoubleText ( groupScan, String.format ( "%+.1f", IPreferences.PROBE_Z_MIN ), 1, true );

        GuiFactory.createHiddenLabel ( groupScan, 3, true );
        GuiFactory.createHeadingLabel ( groupScan, "feedrate", 1, false );
        scanFeedrateText = GuiFactory.createDoubleText ( groupScan, String.format ( "%+.1f", IPreferences.PROBE_FEEDRATE ), 1, true );

        GuiFactory.createHiddenLabel ( groupScan, cols, true );

        GuiFactory.createHiddenLabel ( groupScan, 1, true );
        loadProbeDataButton = GuiFactory.createPushButton ( groupScan, "load", SWT.FILL, true );
        saveProbeDataButton = GuiFactory.createPushButton ( groupScan, "save", SWT.FILL, true );
        scanClearButton = GuiFactory.createPushButton ( groupScan, "clear", SWT.FILL, true );
        scanStartButton = GuiFactory.createPushButton ( groupScan, "start", SWT.FILL, true );

        scanStepXText.addModifyListener ( updateViewModifyListener );
        scanStepYText.addModifyListener ( updateViewModifyListener );

        scanStartButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                updateGrid ();
                // @formatter:off
                gcodeService.scanAutolevelData (
                        gcodeProgram, // current selected gcode program
                        partTools.parseDoubleField ( scanMinZText, IPreferences.PROBE_Z_MIN ), 
                        partTools.parseDoubleField ( scanMaxZText, IPreferences.PROBE_Z_MAX ),
                        partTools.parseDoubleField ( scanClearanceZText, IPreferences.PROBE_Z_CLEARANCE ),
                        partTools.parseDoubleField ( scanFeedrateText, IPreferences.PROBE_FEEDRATE )   
                );
                // @formatter:on
            }
        } );

        scanClearButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_AUTOLEVEL_CLEAR ) );
        loadProbeDataButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_AUTOLEVEL_LOAD ) );
        saveProbeDataButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_AUTOLEVEL_SAVE ) );

    }

    private void createGroupProbe ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
        
        Group groupProbe = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        groupProbe.setLayout ( new GridLayout ( 3, false ) );
        
        GuiFactory.createHeadingLabel ( groupProbe, "Depth", 1 );
        probeDepthText = GuiFactory.createDoubleText ( groupProbe, "-3.0", 1, true );
        probeStartButton = GuiFactory.createArrowButton ( groupProbe, SWT.DOWN );

        probeStartButton.addSelectionListener ( new SelectionAdapter () {
            
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                gcodeService.sendCommandSuppressInTerminal ( "G90G38.2Z" + probeDepthText.getText () + "F" + IPreferences.PROBE_FEEDRATE );
            }

        } );

    }
    
    private void createGroupSpindle ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
        
        Group groupSpindle = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        groupSpindle.setLayout ( new GridLayout ( 3, false  ) );
        
        spindleVelocitySlider = new Slider ( groupSpindle, horizontalSpan );
        spindleVelocitySlider.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false ) );
        spindleVelocitySlider.setEnabled ( false );
        spindleVelocitySlider.setMinimum ( IPreferences.SPINDLE_MIN_RPM );
        spindleVelocitySlider.setMaximum ( IPreferences.SPINDLE_MAX_RPM );
        spindleVelocitySlider.setIncrement ( 1000 );
        spindleStartButton = GuiFactory.createPushButton ( groupSpindle, "start", SWT.CENTER, false );
        spindleStopButton = GuiFactory.createPushButton ( groupSpindle, "stop", SWT.CENTER, false );
        
        spindleVelocitySlider.addSelectionListener ( new SelectionAdapter() {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                // determine slider selection in UI Thread!!!
                final int speed = spindleVelocitySlider.getSelection ();
                // to prevent deadlocks in UI Thread
                ignoreSpindleSpeedUpdate = true;
                new Thread ( ( ) -> gcodeService.sendCommandSuppressInTerminal ( "S" + speed ) ).start ();
            }
        } );
        spindleStartButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "M3" ) );
        spindleStopButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "M5" ) );

    }

    private void createGroupSettings ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
        
        Group groupSettings = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        groupSettings.setLayout ( new GridLayout ( 6, true ) );
        
        grblHelpButton = GuiFactory.createPushButton ( groupSettings, "$", SWT.FILL, true );
        grblSettingsButton = GuiFactory.createPushButton ( groupSettings, "$$", SWT.FILL, true );
        grblParameterButton = GuiFactory.createPushButton ( groupSettings, "$#", SWT.FILL, true );
        grblParserButton = GuiFactory.createPushButton ( groupSettings, "$G", SWT.FILL, true );
        grblInfoButton = GuiFactory.createPushButton ( groupSettings, "$I", SWT.FILL, true );
        grblStartupBlocksButton = GuiFactory.createPushButton ( groupSettings, "$N", SWT.FILL, true );

        grblHelpButton.addSelectionListener ( new SimpleGrblCommandSelectionListener ( "$" ) );
        grblSettingsButton.addSelectionListener ( new SimpleGrblCommandSelectionListener ( "$$" ) );
        grblParameterButton.addSelectionListener ( new SimpleGrblCommandSelectionListener ( "$#" ) );
        grblParserButton.addSelectionListener ( new SimpleGrblCommandSelectionListener ( "$G" ) );
        grblInfoButton.addSelectionListener ( new SimpleGrblCommandSelectionListener ( "$I" ) );
        grblStartupBlocksButton.addSelectionListener ( new SimpleGrblCommandSelectionListener ( "$N" ) );

    }

    private void createGroupGrbl ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
        
        Group groupGrbl = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        groupGrbl.setLayout ( new GridLayout ( 3, false ) );
        
        GuiFactory.createHiddenLabel ( groupGrbl );
        grblHomeButton = GuiFactory.createPushButton ( groupGrbl, "home", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( groupGrbl );

        Label label = GuiFactory.createHiddenLabel ( groupGrbl, 1 );
        label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
        grblUnlockButton = GuiFactory.createPushButton ( groupGrbl, "unlock", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( groupGrbl );

        GuiFactory.createHiddenLabel ( groupGrbl );
        grblCheckButton = GuiFactory.createPushButton ( groupGrbl, "check", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( groupGrbl );
        
        // TODO if i am using handledmenuitem in execute, this call never finds the appropriate handler
        // grblHomeButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_GRBL_HOME ) );
        // grblUnlockButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_GRBL_UNLOCK ) );
        // grblCheckButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_GRBL_CHECK ) );

        grblHomeButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "$H" ) );
        grblUnlockButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "$X" ) );
        grblCheckButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "$C" ) );

    }

    private void createGroupCycle ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
        
        Group groupCycle = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        groupCycle.setLayout ( new GridLayout ( 3, false ) );
        
        GuiFactory.createHiddenLabel ( groupCycle );
        cycleStartButton = GuiFactory.createPushButton ( groupCycle, "start", SWT.FILL, true );
        cycleStartButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_GREEN ) );
        GuiFactory.createHiddenLabel ( groupCycle );

        GuiFactory.createHiddenLabel ( groupCycle );
        cycleHoldButton = GuiFactory.createPushButton ( groupCycle, "hold", SWT.FILL, true );
        cycleHoldButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_YELLOW ) );
        GuiFactory.createHiddenLabel ( groupCycle );

        GuiFactory.createHiddenLabel ( groupCycle );
        cycleResetButton = GuiFactory.createPushButton ( groupCycle, "reset", SWT.FILL, true );
        cycleResetButton.setBackground ( shell.getDisplay ().getSystemColor ( SWT.COLOR_RED ) );
        GuiFactory.createHiddenLabel ( groupCycle );

        cycleStartButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_CYCLE_START ) );
        cycleHoldButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_CYCLE_PAUSE ) );
        cycleResetButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandIDs.COMMAND_CYCLE_RESET ) );

    }

    private void createGroupMove ( Composite parent, String name, int horizontalSpan, int verticalSpan ) {
        
        Group groupMove = GuiFactory.createGroup ( parent, name, horizontalSpan, verticalSpan, true );
        groupMove.setLayout ( new GridLayout ( 9, false ) );
        
        // line 1
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        GuiFactory.createHeadingLabel ( groupMove, "XY", 3, false );
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        GuiFactory.createHeadingLabel ( groupMove, "Z", 1, false );
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        GuiFactory.createHeadingLabel ( groupMove, "Range", 1, false );
        GuiFactory.createHiddenLabel ( groupMove, 1, true );

        // line 2
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        GuiFactory.createHiddenLabel ( groupMove, 1 );
        moveYForwardButton = GuiFactory.createArrowButton ( groupMove, SWT.UP );
        GuiFactory.createHiddenLabel ( groupMove, 2 );
        moveZUpButton = GuiFactory.createArrowButton ( groupMove, SWT.UP );
        GuiFactory.createHiddenLabel ( groupMove, 3 );

        // line 3
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        moveXBackwardButton = GuiFactory.createArrowButton ( groupMove, SWT.LEFT );
        goZeroXYButton = GuiFactory.createButton ( groupMove, SWT.PUSH, "0", SWT.FILL, SWT.FILL, false, false );
        //GuiFactory.createHiddenLabel ( groupMoveXY, 1 );
        moveXForwardButton = GuiFactory.createArrowButton ( groupMove, SWT.RIGHT );
        GuiFactory.createHiddenLabel ( groupMove );
        goZeroZButton = GuiFactory.createButton ( groupMove, SWT.PUSH, "0", SWT.FILL, SWT.FILL, false, false );
        GuiFactory.createHiddenLabel ( groupMove );
        rangeCombo = new CCombo ( groupMove, SWT.DROP_DOWN | SWT.BORDER | SWT.RIGHT );
        rangeCombo.setLayoutData ( new GridData ( SWT.RIGHT, SWT.CENTER, false, false ) );
        rangeCombo.setEnabled ( false );
        rangeCombo.setItems ( new String [] { "0.01", "0.1", "1.0", "10.0" } );
        rangeCombo.select ( 2 );
        rangeCombo.addVerifyListener ( new GuiFactory.DoubleVerifyer () );
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        
        // line 4
        GuiFactory.createHiddenLabel ( groupMove, 1, true );
        GuiFactory.createHiddenLabel ( groupMove );
        moveYBackwardButton = GuiFactory.createArrowButton ( groupMove, SWT.DOWN );
        GuiFactory.createHiddenLabel ( groupMove, 2 );
        moveZDownButton = GuiFactory.createArrowButton ( groupMove, SWT.DOWN );
        GuiFactory.createHiddenLabel ( groupMove, 3 );
        
        // factor out suppress flag to preferences
        moveXForwardButton.addSelectionListener ( new MoveGrblCommandSelectionListener ( 'X', '+' ) );
        moveXBackwardButton.addSelectionListener ( new MoveGrblCommandSelectionListener ( 'X', '-' ) );
        moveYForwardButton.addSelectionListener ( new MoveGrblCommandSelectionListener ( 'Y', '+' ) );
        moveYBackwardButton.addSelectionListener ( new MoveGrblCommandSelectionListener ( 'Y', '-' ) );
        moveZUpButton.addSelectionListener ( new MoveGrblCommandSelectionListener ( 'Z', '+' ) );
        moveZDownButton.addSelectionListener ( new MoveGrblCommandSelectionListener ( 'Z', '-' ) );
        
        goZeroXYButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "G90G0X0Y0" ) );
        goZeroZButton.addSelectionListener ( new SimpleGrblCommandSelectionListenerSuppressedInTerminal ( "G90G0Z0" ) );
        
    }
    
    private void setControlsEnabled ( boolean enabled ) {
        
        setNormalControlsEnabled ( enabled );
        setCycleControlsEnabled ( enabled );
        
    }

    private void setCycleControlsEnabled ( boolean enabled ) {
        
        cycleHoldButton.setEnabled ( enabled );
        cycleStartButton.setEnabled ( enabled );
        cycleResetButton.setEnabled ( enabled );
        
    }

    private void setAutolevelControlsEnabled ( boolean enabled ) {
        
        scanStepXText.setEnabled ( enabled );
        scanStepYText.setEnabled ( enabled );
        scanMinZText.setEnabled ( enabled );
        scanMaxZText.setEnabled ( enabled );
        scanClearanceZText.setEnabled ( enabled );
        scanFeedrateText.setEnabled ( enabled );

        scanStartButton.setEnabled ( enabled && gcodeProgram != null && gcodeProgram.isLoaded () && !gcodeProgram.isAutolevelScanComplete () );
        scanClearButton.setEnabled ( enabled && gcodeProgram != null && gcodeProgram.isAutolevelScanComplete () );
        loadProbeDataButton.setEnabled ( enabled && gcodeProgram != null && gcodeProgram.isLoaded () && gcodeProgram.getAutolevelDataFile ().isFile () );
        saveProbeDataButton.setEnabled ( enabled && gcodeProgram != null && gcodeProgram.isAutolevelScanComplete () );
        
    }

    private void setNormalControlsEnabled ( boolean enabled ) {
        
        probeDepthText.setEnabled ( enabled );
        probeStartButton.setEnabled ( enabled );
        
        spindleVelocitySlider.setEnabled ( enabled );
        spindleStartButton.setEnabled ( enabled );
        spindleStopButton.setEnabled ( enabled );
        
        grblHelpButton.setEnabled ( enabled );
        grblSettingsButton.setEnabled ( enabled );
        grblParameterButton.setEnabled ( enabled );
        grblParserButton.setEnabled ( enabled );
        grblInfoButton.setEnabled ( enabled );
        grblStartupBlocksButton.setEnabled ( enabled );
        
        grblHomeButton.setEnabled ( enabled );
        grblUnlockButton.setEnabled ( enabled );
        grblCheckButton.setEnabled ( enabled );
        
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

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.GRBL_ALARM) String line ) {
    
        LOG.trace ( "alarmNotified: line=" + line );
    
        setNormalControlsEnabled ( false );
        setAutolevelControlsEnabled ( false );
    
    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvents.GRBL_RESTARTED) String line ) {
    
        LOG.trace ( "grblRestartedNotified: line=" + line );
    
        setNormalControlsEnabled ( true );
        setAutolevelControlsEnabled ( true );
    
    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );
        setControlsEnabled ( true );
        // commandText.setFocus ();

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );
        setControlsEnabled ( false );
        setAutolevelControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.PLAYER_LOADED) String fileName ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        setAutolevelControlsEnabled ( true );

        updateGrid ();

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvents.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        setNormalControlsEnabled ( false );
        setAutolevelControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvents.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        setNormalControlsEnabled ( true );
        setAutolevelControlsEnabled ( true );

    }
    
    @Inject
    @Optional
    public void probeDataLoadedNotified ( @UIEventTopic(IEvents.AUTOLEVEL_DATA_LOADED) String fileName ) {

        LOG.trace ( "probeDataloadedNotified: fileName=" + fileName );

        setGridFields ();

        setAutolevelControlsEnabled ( true );
        redrawGcode ();

    }

    @Inject
    @Optional
    public void probeDataSavedNotified ( @UIEventTopic(IEvents.AUTOLEVEL_DATA_SAVED) String fileName ) {

        LOG.trace ( "probeDataSavedNotified: fileName=" + fileName );

    }

    @Inject
    @Optional
    public void probeDataClearedNotified ( @UIEventTopic(IEvents.AUTOLEVEL_DATA_CLEARED) String fileName ) {

        LOG.trace ( "probeDataClearedNotified: fileName=" + fileName );

        setAutolevelControlsEnabled ( true ); // side effect is to disable probe save button
        // redrawGcode ();
        updateGrid ();

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.AUTOLEVEL_START) Object dummy ) {

        LOG.trace ( "scanStartNotified:" );

        setNormalControlsEnabled ( false );
        setAutolevelControlsEnabled ( false );
        
    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.AUTOLEVEL_STOP) Object dummy ) {
    
        LOG.trace ( "scanStopNotified:" );
    
        setNormalControlsEnabled ( true );
        setAutolevelControlsEnabled ( true );

        redrawGcode ();
    
    }

    @Inject
    @Optional
    public void updateSpindlespeedNotified ( @UIEventTopic(IEvents.UPDATE_SPINDLESPEED) String spindlespeed ) {
    
        LOG.trace ( "updateSpindlespeedNotified: spindlespeed=" + spindlespeed );
    
        if ( !ignoreSpindleSpeedUpdate ) {
            ignoreSpindleSpeedUpdate = false;
            int speed = Integer.parseInt ( spindlespeed );
            spindleVelocitySlider.setSelection ( speed );
        }
    
    }

}