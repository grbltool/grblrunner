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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
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
public class ControlProbeGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlProbeGroup.class );

    private static final String GROUP_NAME = "Z-Probe";

    @Inject
    private Toolbox toolbox;

    private Button probeStartButton;
    private Text probeDepthText;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols, @Preference(
            nodePath = IConstant.PREFERENCE_NODE,
            value = IPreferenceKey.PROBE_DEPTH) double probeDepth ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 3, false ) );

        GuiFactory.createHeadingLabel ( group, "Depth", 1 );
        probeDepthText = GuiFactory.createDoubleText ( group, String.format ( IConstant.FORMAT_HEIGHT, probeDepth ), 1, true );
        probeStartButton = GuiFactory.createArrowButton ( group, SWT.DOWN );

        probeStartButton.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.PROBE_ACTION, new HashMap<String, Object> (), this ) );

    }

    private void setControlsEnabled ( boolean enabled ) {

        probeDepthText.setEnabled ( enabled );
        probeStartButton.setEnabled ( enabled );

    }

    @Override
    public Map<String, Object> getParameter () {

        Map<String, Object> result = new HashMap<> ();
        result.put ( ICommandId.PROBE_ACTION_DEPTH_PARAMETER, probeDepthText.getText () );

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
