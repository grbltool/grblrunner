package de.jungierek.grblrunner.part.group;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.constants.IPersistenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tool.CommandParameterCallback;
import de.jungierek.grblrunner.tool.GuiFactory;
import de.jungierek.grblrunner.tool.Toolbox;

public class StateCoordinatesGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( StateCoordinatesGroup.class );

    private static final String GROUP_NAME = "Coordinates";

    private static final String [] AXIS = { "X", "Y", "Z" }; // TO Constants

    @Inject
    private MApplication application;

    @Inject
    private IGcodeService gcodeService;

    @Inject
    private Toolbox toolbox;

    private CCombo coordSystemCombo;

    private Label [] machineCoordLabel = new Label [3];
    private Label [] workCoordLabel = new Label [3];
    private Button [] setZeroButton = new Button [3];
    private Button [] resetZeroButton = new Button [3];

    public void setFocus () {

        coordSystemCombo.setFocus ();

    }

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 5, true ) );

        GuiFactory.createHiddenLabel ( group, 1 );
        GuiFactory.createHeadingLabel ( group, "Machine", 1 );
        GuiFactory.createHeadingLabel ( group, "Work", 1 );

        coordSystemCombo = new CCombo ( group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER | SWT.CENTER );
        coordSystemCombo.setEnabled ( false );
        coordSystemCombo.setItems ( IConstant.COORDINATE_SYSTEMS );
        coordSystemCombo.select ( 0 ); // G54
        coordSystemCombo.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        coordSystemCombo.addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.SET_COORDINATE_SYSTEM, new HashMap<String, Object> (), this ) );

        for ( int i = 0; i < AXIS.length; i++ ) {

            final String axisLetter = AXIS[i];

            GuiFactory.createHeadingLabel ( group, axisLetter, 1 );

            machineCoordLabel[i] = GuiFactory.createCoordinateLabel ( group );
            workCoordLabel[i] = GuiFactory.createCoordinateLabel ( group );

            setZeroButton[i] = GuiFactory.createPushButton ( group, "zero " + axisLetter );
            HashMap<String, Object> parameter1 = new HashMap<> ();
            parameter1.put ( ICommandId.COORDINATE_OFFSET_PARAMETER, axisLetter );
            setZeroButton[i].addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.SET_COORDINATE_OFFSET, parameter1 ) );

            resetZeroButton[i] = GuiFactory.createPushButton ( group, "reset " + axisLetter );
            HashMap<String, Object> parameter2 = new HashMap<> ();
            parameter2.put ( ICommandId.COORDINATE_OFFSET_PARAMETER, axisLetter );
            resetZeroButton[i].addSelectionListener ( toolbox.createCommandExecuteSelectionListener ( ICommandId.RESET_COORDINATE_OFFSET, parameter2 ) );

        }

    }

    private void setControlsEnabled ( boolean enabled ) {

        coordSystemCombo.setEnabled ( enabled );

        for ( int i = 0; i < AXIS.length; i++ ) {
            setZeroButton[i].setEnabled ( enabled );
            resetZeroButton[i].setEnabled ( enabled );
        }

    }

    private void saveCoordinateSystem ( String coordSelect ) {

        application.getPersistedState ().put ( IPersistenceKey.LAST_COORDINATE_SYSTEM, coordSelect );

    }

    private void restoreCoordinateSystem () {

        String coordSelect = application.getPersistedState ().get ( IPersistenceKey.LAST_COORDINATE_SYSTEM );
        if ( coordSelect != null && coordSelect.length () == 3 ) {
            try {
                gcodeService.sendCommandSuppressInTerminal ( coordSelect );
            }
            catch ( InterruptedException exc ) {
                LOG.info ( "execute: interrupted exception in line=" + coordSelect );
            }
        }

    }

    private void setCoordLabels ( double [] coords, Label [] labels ) {

        for ( int i = 0; i < coords.length; i++ ) {
            labels[i].setText ( String.format ( IGcodePoint.FORMAT_COORDINATE, coords[i] ) );
        }

    }

    @Override
    public Map<String, Object> getParameter () {

        Map<String, Object> result = new HashMap<> ();
        result.put ( ICommandId.SET_COORDINATE_SYSTEM_PARAMETER, "" + (toolbox.parseInteger ( coordSystemCombo.getText ().substring ( 1 ), 54 ) - 53) );

        return result;

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

        restoreCoordinateSystem ();

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void updateCoordSelectNotified ( @UIEventTopic(IEvent.UPDATE_FIXTURE) String coordSelect ) {

        LOG.trace ( "updateCoordSelectNotified: coordSelect=" + coordSelect );

        coordSystemCombo.select ( coordSelect.charAt ( 2 ) - '4' );
        saveCoordinateSystem ( coordSelect );

    }

    @Inject
    @Optional
    public void updateStateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState grblState ) {

        LOG.debug ( "updateStateNotified: grblState=" + grblState );

        setCoordLabels ( grblState.getMachineCoordindates ().getCooridnates (), machineCoordLabel );
        setCoordLabels ( grblState.getWorkCoordindates ().getCooridnates (), workCoordLabel );

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
