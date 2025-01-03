package de.jungierek.grblrunner.part.group;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tool.CommandParameterCallback;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

@SuppressWarnings("restriction")
public class ControlAutolevelGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlAutolevelGroup.class );

    private static final String GROUP_NAME = "Autolevel";

    @Inject
    private Toolbox toolbox;

    @Inject
    private ISerialService serialService;

    @Inject
    private IEventBroker eventBroker;

    private IGcodeProgram gcodeProgram;

    private Button scanStartButton;
    private Button scanClearButton;
    private Button loadProbeDataButton;
    private Button saveProbeDataButton;

    private boolean ignoreStepTextModifyListener = false;

    // this are model vars
    private Text scanStepXText;
    private Text scanStepYText;
    private Label scanStepWidthXLabel;
    private Label scanStepWidthYLabel;
    private Text scanMaxZText;
    private Text scanMinZText;
    private Text scanClearanceZText;
    private Text scanFeedrateText;

// @formatter:off
    @PostConstruct
    public void createGui ( 
            Composite parent, 
            IEclipseContext context,
            @Named(IContextKey.PART_GROUP_COLS) int groupCols,
            @Named(IContextKey.PART_GROUP_ROWS) int groupRows,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.Z_CLEARANCE) double zClearance,
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_DEPTH) double probeDepth, 
            @Preference (nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_Z_MAX) double probeMaxZ, 
            @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.PROBE_FEEDRATE) double probeFeedrate 
    ) {
// @formatter:on

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        int cols = 5;
        group.setLayout ( new GridLayout ( cols, false ) );

        GuiFactory.createHeadingLabel ( group, "X Steps", 1, false );
        scanStepXText = GuiFactory.createIntegerText ( group, "" + IConstant.INITIAL_XSTEPS, 1, true, 0 );
        scanStepWidthXLabel = GuiFactory.createCoordinateLabel ( group );
        GuiFactory.createHeadingLabel ( group, "Z clear", 1, false );
        scanClearanceZText = GuiFactory.createDoubleText ( group, String.format ( IConstant.FORMAT_HEIGHT, zClearance ), 1, true );

        GuiFactory.createHeadingLabel ( group, "Y Steps", 1, false );
        scanStepYText = GuiFactory.createIntegerText ( group, "" + IConstant.INITIAL_YSTEPS, 1, true, 0 );
        scanStepWidthYLabel = GuiFactory.createCoordinateLabel ( group );
        GuiFactory.createHeadingLabel ( group, "Z max", 1, false );
        scanMaxZText = GuiFactory.createDoubleText ( group, String.format ( IConstant.FORMAT_HEIGHT, probeMaxZ ), 1, true );

        GuiFactory.createHiddenLabel ( group, 3, true );
        GuiFactory.createHeadingLabel ( group, "Z min", 1, false );
        scanMinZText = GuiFactory.createDoubleText ( group, String.format ( IConstant.FORMAT_HEIGHT, probeDepth ), 1, true );

        GuiFactory.createHiddenLabel ( group, 3, true );
        GuiFactory.createHeadingLabel ( group, "feedrate", 1, false );
        scanFeedrateText = GuiFactory.createDoubleText ( group, String.format ( IConstant.FORMAT_HEIGHT, probeFeedrate ), 1, true );

        GuiFactory.createHiddenLabel ( group, cols, true );

        GuiFactory.createHiddenLabel ( group, 1, true );
        loadProbeDataButton = GuiFactory.createPushButton ( group, "load", SWT.FILL, true );
        saveProbeDataButton = GuiFactory.createPushButton ( group, "save", SWT.FILL, true );
        scanClearButton = GuiFactory.createPushButton ( group, "clear", SWT.FILL, true );
        scanStartButton = GuiFactory.createPushButton ( group, "start", SWT.FILL, true );

        scanStepXText.addModifyListener ( updateViewModifyListener );
        scanStepYText.addModifyListener ( updateViewModifyListener );

        scanStartButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.AUTOLEVEL_SCAN, new HashMap<String, Object> (), this ) );
        scanClearButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.AUTOLEVEL_CLEAR ) );
        loadProbeDataButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.AUTOLEVEL_LOAD ) );
        saveProbeDataButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.AUTOLEVEL_SAVE ) );

    }

    private void setGridFields () {

        if ( gcodeProgram == null ) return;

        ignoreStepTextModifyListener = true;
        if ( scanStepXText != null && !scanStepXText.isDisposed () ) scanStepXText.setText ( "" + gcodeProgram.getXSteps () );
        if ( scanStepYText != null && !scanStepYText.isDisposed () ) scanStepYText.setText ( "" + gcodeProgram.getYSteps () );
        ignoreStepTextModifyListener = false;

        if ( scanStepWidthXLabel != null && !scanStepWidthXLabel.isDisposed () )
            scanStepWidthXLabel.setText ( String.format ( IConstant.FORMAT_COORDINATE, gcodeProgram.getStepWidthX () ) );
        if ( scanStepWidthYLabel != null && !scanStepWidthYLabel.isDisposed () )
            scanStepWidthYLabel.setText ( String.format ( IConstant.FORMAT_COORDINATE, gcodeProgram.getStepWidthY () ) );

    }

    @Inject
    public void setGcodeProgram ( @Optional @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.debug ( "setGcodeProgram: program=" + program );

        gcodeProgram = program;
        setGridFields ();

        if ( scanClearanceZText != null && !scanClearanceZText.isDisposed () ) { // gui created
            setControlsEnabled ( program != null );
        }

    }

    private void updateGrid () {

        // probeDataCleared is called after closing a editor part and the next one is not initialized
        // on gcode loading the probe data are always reseted
        if ( gcodeProgram != null ) {

            int xSteps = toolbox.parseIntegerField ( scanStepXText, 1 );
            int ySteps = toolbox.parseIntegerField ( scanStepYText, 1 );

            gcodeProgram.prepareAutolevelScan ( xSteps, ySteps ); // resets scan completed
            scanStepWidthXLabel.setText ( String.format ( IConstant.FORMAT_COORDINATE, gcodeProgram.getStepWidthX () ) );
            scanStepWidthYLabel.setText ( String.format ( IConstant.FORMAT_COORDINATE, gcodeProgram.getStepWidthY () ) );

        }

        redrawGcode (); // redraw gcode every time, if gcodeProgram is null, then the view is "cleared"

    }

    private void redrawGcode () {

        eventBroker.send ( IEvent.REDRAW, null );

    }

    private final ModifyListener updateViewModifyListener = new ModifyListener () {
        @Override
        public void modifyText ( ModifyEvent evt ) {
            if ( !ignoreStepTextModifyListener ) {
                updateGrid ();
                setControlsEnabled ( true );
            }
        }
    };

    private void setControlsEnabled ( boolean enabled ) {

        boolean textFieldEnabled = enabled && gcodeProgram != null && (serialService.isOpen () || IConstant.AUTOLEVEL_ENABLE_WITHOUT_SERIAL);
        scanStepXText.setEnabled ( textFieldEnabled );
        scanStepYText.setEnabled ( textFieldEnabled );
        scanMinZText.setEnabled ( textFieldEnabled );
        scanMaxZText.setEnabled ( textFieldEnabled );
        scanClearanceZText.setEnabled ( textFieldEnabled );
        scanFeedrateText.setEnabled ( textFieldEnabled );

        // @formatter:off
        scanStartButton.setEnabled ( 
                enabled && 
                gcodeProgram != null && 
                    gcodeProgram.getGcodeProgramFile () != null && 
                    gcodeProgram.isLoaded () && 
                    !gcodeProgram.isAutolevelScanComplete () &&
                    (serialService.isOpen () || IConstant.AUTOLEVEL_ENABLE_WITHOUT_SERIAL)
        );
        
        scanClearButton.setEnabled ( 
                enabled && 
                gcodeProgram != null && 
                    gcodeProgram.getGcodeProgramFile () != null && 
                    gcodeProgram.isAutolevelScanComplete () 
        );
        
        loadProbeDataButton.setEnabled ( 
                enabled && 
                gcodeProgram != null && 
                    gcodeProgram.getGcodeProgramFile () != null && 
                    gcodeProgram.isLoaded () && 
                    gcodeProgram.getAutolevelDataFile () != null && 
                    gcodeProgram.getAutolevelDataFile ().isFile () 
         );
        
        saveProbeDataButton.setEnabled ( 
                enabled && 
                gcodeProgram != null && 
                    gcodeProgram.getGcodeProgramFile () != null && 
                    gcodeProgram.isAutolevelScanComplete () 
        );
        // @formatter:on

    }

    @Override
    public Map<String, Object> getParameter () {

        Map<String, Object> result = new HashMap<> ();
        result.put ( ICommandId.AUTOLEVEL_ZMIN_PARAMETER, scanMinZText.getText () );
        result.put ( ICommandId.AUTOLEVEL_ZMAX_PARAMETER, scanMaxZText.getText () );
        result.put ( ICommandId.AUTOLEVEL_ZCLEARANCE_PARAMETER, scanClearanceZText.getText () );
        result.put ( ICommandId.AUTOLEVEL_PROBEFEEDRATE_PARAMETER, scanFeedrateText.getText () );

        return result;

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.info ( "alarmNotified: line=" + line );

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
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        setGridFields ();

        setControlsEnabled ( true );

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
    public void probeDataLoadedNotified ( @UIEventTopic(IEvent.AUTOLEVEL_DATA_LOADED) String fileName ) {

        LOG.trace ( "probeDataloadedNotified: fileName=" + fileName );

        setGridFields ();

        setControlsEnabled ( true );
        redrawGcode ();

    }

    @Inject
    @Optional
    public void probeDataSavedNotified ( @UIEventTopic(IEvent.AUTOLEVEL_DATA_SAVED) String fileName ) {

        LOG.trace ( "probeDataSavedNotified: fileName=" + fileName );

    }

    @Inject
    @Optional
    public void probeDataClearedNotified ( @UIEventTopic(IEvent.AUTOLEVEL_DATA_CLEARED) String fileName ) {

        LOG.trace ( "probeDataClearedNotified: fileName=" + fileName );

        setControlsEnabled ( true ); // side effect is to disable probe save button
        // redrawGcode ();
        updateGrid ();

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

        redrawGcode ();

    }

}
