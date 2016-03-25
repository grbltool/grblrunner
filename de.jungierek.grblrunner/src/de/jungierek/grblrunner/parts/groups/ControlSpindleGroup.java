package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class ControlSpindleGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlSpindleGroup.class );

    private static final String GROUP_NAME = "Spindle";

    @Inject
    private PartTools partTools;

    @Inject
    private IGcodeService gcodeService;

    private Slider spindleVelocitySlider;
    private Button spindleStartButton;
    private Button spindleStopButton;

    // preferences
    private int spindle_min_rpm;
    private int spindle_max_rpm;

    private boolean ignoreSpindleSpeedUpdate = false;
    
    @Inject
    public void setSpindleMinRpm ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_MIN) int min ) {

        spindle_min_rpm = min;

    }

    @Inject
    public void setSpindleMaxRpm ( @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.SPINDLE_MAX) int max ) {

        spindle_max_rpm = max;

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.PART_GROUP_ROWS )).intValue ();
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

        spindleVelocitySlider.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                // determine slider selection in UI Thread!!!
                final int speed = spindleVelocitySlider.getSelection ();
                // to prevent deadlocks in UI Thread
                ignoreSpindleSpeedUpdate = true;
                new Thread ( ( ) -> gcodeService.sendCommandSuppressInTerminal ( "S" + speed ) ).start ();
            }
        } );

        spindleStartButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.GRBL_SPINDLE_START ) );
        spindleStopButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.GRBL_SPINDLE_STOP ) );

    }

    private void setControlsEnabled ( boolean enabled ) {

        spindleVelocitySlider.setEnabled ( enabled );
        spindleStartButton.setEnabled ( enabled );
        spindleStopButton.setEnabled ( enabled );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.GRBL_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvents.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

        setControlsEnabled ( false );

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

    @Inject
    @Optional
    public void updateSpindlespeedNotified ( @UIEventTopic(IEvents.UPDATE_SPINDLESPEED) String spindlespeed ) {

        LOG.trace ( "updateSpindlespeedNotified: spindlespeed=" + spindlespeed );

        if ( !ignoreSpindleSpeedUpdate ) {
            int speed = Integer.parseInt ( spindlespeed );
            spindleVelocitySlider.setSelection ( speed );
        }

        ignoreSpindleSpeedUpdate = false;

    }

}
