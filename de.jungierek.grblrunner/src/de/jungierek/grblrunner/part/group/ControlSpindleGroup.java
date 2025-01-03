package de.jungierek.grblrunner.part.group;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.CommandParameterCallback;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

@SuppressWarnings("restriction")
public class ControlSpindleGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlSpindleGroup.class );

    private static final String GROUP_NAME = "Spindle";

    @Inject
    private Toolbox toolbox;

    private Slider spindleVelocitySlider;
    private Button spindleStartButton;
    private Button spindleStopButton;

    // preferences
    private int spindle_min_rpm;
    private int spindle_max_rpm;

    private boolean ignoreSpindleSpeedUpdate = false;
    
    @Inject
    public void setSpindleMinRpm ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_MIN) int min ) {

        spindle_min_rpm = min;

    }

    @Inject
    public void setSpindleMaxRpm ( @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_MAX) int max ) {

        spindle_max_rpm = max;

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 3, false ) );

        spindleVelocitySlider = new Slider ( group, SWT.HORIZONTAL );
        spindleVelocitySlider.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false ) );
        spindleVelocitySlider.setEnabled ( false );
        spindleVelocitySlider.setMinimum ( spindle_min_rpm );
        spindleVelocitySlider.setMaximum ( spindle_max_rpm + 10 ); // why ever?
        spindleVelocitySlider.setIncrement ( 1000 );
        spindleVelocitySlider.setPageIncrement ( 100 );
        spindleStartButton = GuiFactory.createPushButton ( group, "start", SWT.CENTER, false );
        spindleStopButton = GuiFactory.createPushButton ( group, "stop", SWT.CENTER, false );

        spindleVelocitySlider.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.SPINDLE_SPEED, this ) );
        spindleStartButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.SPINDLE_START ) );
        spindleStopButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.SPINDLE_STOP ) );

    }

    @Override
    public Map<String, Object> getParameter () {
        
        LOG.debug ( "getParameter: speed=" + spindleVelocitySlider.getSelection () );

        // to prevent deadlocks in UI Thread
        ignoreSpindleSpeedUpdate = true;

        Map<String, Object> result = new HashMap<> ();
        result.put ( ICommandId.SPINDLE_SPEED_PARAMETER, "" + spindleVelocitySlider.getSelection () );

        return result;

    }

    private void setControlsEnabled ( boolean enabled ) {

        spindleVelocitySlider.setEnabled ( enabled );
        spindleStartButton.setEnabled ( enabled );
        spindleStopButton.setEnabled ( enabled );

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

    @Inject
    @Optional
    public void updateSpindlespeedNotified ( @UIEventTopic(IEvent.UPDATE_SPINDLESPEED) String spindlespeed ) {

        LOG.trace ( "updateSpindlespeedNotified: spindlespeed=" + spindlespeed );

        if ( !ignoreSpindleSpeedUpdate ) {
            int speed = Integer.parseInt ( spindlespeed );
            spindleVelocitySlider.setSelection ( speed );
        }

        ignoreSpindleSpeedUpdate = false;

    }

}
