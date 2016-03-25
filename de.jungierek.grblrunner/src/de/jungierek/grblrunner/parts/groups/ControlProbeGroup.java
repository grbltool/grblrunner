package de.jungierek.grblrunner.parts.groups;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.CommandParameterCallback;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class ControlProbeGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlProbeGroup.class );

    private static final String GROUP_NAME = "Z-Probe";

    @Inject
    private PartTools partTools;

    @Inject
    private IGcodeService gcodeService;

    private Button probeStartButton;
    private Text probeDepthText;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.PROBE_DEPTH) double probeDepth ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 3, false ) );

        GuiFactory.createHeadingLabel ( group, "Depth", 1 );
        probeDepthText = GuiFactory.createDoubleText ( group, String.format ( IConstants.FORMAT_HEIGHT, probeDepth ), 1, true );
        probeStartButton = GuiFactory.createArrowButton ( group, SWT.DOWN );

        probeStartButton.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.PROBE_ACTION, new HashMap<String, Object> (), this ) );

        // probeStartButton.addSelectionListener ( new SelectionAdapter () {
        //
        // @Override
        // public void widgetSelected ( SelectionEvent evt ) {
        // gcodeService.sendCommandSuppressInTerminal ( "G90G38.2Z" + probeDepthText.getText () + "F" + IPreferences.PROBE_FEEDRATE );
        // }
        //
        // } );

    }

    private void setControlsEnabled ( boolean enabled ) {

        probeDepthText.setEnabled ( enabled );
        probeStartButton.setEnabled ( enabled );

    }

    @Override
    public Map<String, Object> getParameter () {

        Map<String, Object> result = new HashMap<String, Object> ();
        result.put ( ICommandID.PROBE_ACTION_DEPTH_PARAMETER, probeDepthText.getText () );

        return result;

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

}
