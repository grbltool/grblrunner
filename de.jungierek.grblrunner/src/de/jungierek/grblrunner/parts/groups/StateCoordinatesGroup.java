package de.jungierek.grblrunner.parts.groups;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
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

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.constants.IPersistenceKeys;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.gcode.IGrblRequest;
import de.jungierek.grblrunner.service.gcode.IGrblResponse;
import de.jungierek.grblrunner.tools.CommandParameterCallback;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class StateCoordinatesGroup implements CommandParameterCallback {

    private static final Logger LOG = LoggerFactory.getLogger ( StateCoordinatesGroup.class );

    private static final String GROUP_NAME = "Coordinates";

    private static final String [] AXIS = { "X", "Y", "Z" }; // TO Constants

    @Inject
    private MApplication application;

    @Inject
    private IGcodeService gcodeService;

    @Inject
    private PartTools partTools;

    private CCombo coordSystemCombo;

    private Label [] machineCoordLabel = new Label [3];
    private Label [] workCoordLabel = new Label [3];
    private Button [] setZeroButton = new Button [3];
    private Button [] resetZeroButton = new Button [3];

    public void setFocus () {

        coordSystemCombo.setFocus ();

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IContextKey.PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 5, true ) );

        GuiFactory.createHiddenLabel ( group, 1 );
        GuiFactory.createHeadingLabel ( group, "Machine", 1 );
        GuiFactory.createHeadingLabel ( group, "Work", 1 );

        coordSystemCombo = new CCombo ( group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER | SWT.CENTER );
        coordSystemCombo.setEnabled ( false );
        coordSystemCombo.setItems ( IConstants.COORDINATE_SYSTEMS );
        coordSystemCombo.select ( 0 ); // G54
        coordSystemCombo.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        coordSystemCombo.addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.SET_COORDINATE_SYSTEM, new HashMap<String, Object> (), this ) );
        // coordSystemCombo.addSelectionListener ( new SelectionAdapter () {
        // @Override
        // public void widgetSelected ( SelectionEvent evt ) {
        // gcodeService.sendCommandSuppressInTerminal ( ((CCombo) evt.getSource ()).getText () );
        // }
        // } );

        for ( int i = 0; i < AXIS.length; i++ ) {

            final String axisLetter = AXIS[i];

            GuiFactory.createHeadingLabel ( group, axisLetter, 1 );

            machineCoordLabel[i] = GuiFactory.createCoordinateLabel ( group );
            workCoordLabel[i] = GuiFactory.createCoordinateLabel ( group );

            setZeroButton[i] = GuiFactory.createPushButton ( group, "zero " + axisLetter );
            HashMap<String, Object> parameter1 = new HashMap<String, Object> ();
            parameter1.put ( ICommandID.COORDINATE_OFFSET_PARAMETER, axisLetter );
            setZeroButton[i].addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.SET_COORDINATE_OFFSET, parameter1 ) );

            resetZeroButton[i] = GuiFactory.createPushButton ( group, "reset " + axisLetter );
            HashMap<String, Object> parameter2 = new HashMap<String, Object> ();
            parameter2.put ( ICommandID.COORDINATE_OFFSET_PARAMETER, axisLetter );
            resetZeroButton[i].addSelectionListener ( partTools.createCommandExecuteSelectionListener ( ICommandID.RESET_COORDINATE_OFFSET, parameter2 ) );

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

        application.getPersistedState ().put ( IPersistenceKeys.KEY_LAST_COORDINATE_SYSTEM, coordSelect );

    }

    private void restoreCoordinateSystem () {

        String coordSelect = application.getPersistedState ().get ( IPersistenceKeys.KEY_LAST_COORDINATE_SYSTEM );
        if ( coordSelect != null && coordSelect.length () == 3 ) {
            gcodeService.sendCommandSuppressInTerminal ( coordSelect );
            // gcode.sendCommand ( coordSelect );
        }

    }

    private void setCoordLabels ( double [] coords, Label [] labels ) {

        for ( int i = 0; i < coords.length; i++ ) {
            labels[i].setText ( String.format ( IGcodePoint.FORMAT_COORDINATE, coords[i] ) );
        }

    }

    @Override
    public Map<String, Object> getParameter () {

        Map<String, Object> result = new HashMap<String, Object> ();
        result.put ( ICommandID.SET_COORDINATE_SYSTEM_PARAMETER, "" + (partTools.parseInteger ( coordSystemCombo.getText ().substring ( 1 ), 54 ) - 53) );

        return result;

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.debug ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

        restoreCoordinateSystem ();

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );

        setControlsEnabled ( false );

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
    public void updateStateNotified ( @UIEventTopic(IEvents.UPDATE_STATE) IGcodeGrblState grblState ) {

        LOG.debug ( "updateStateNotified: grblState=" + grblState );

        setCoordLabels ( grblState.getMachineCoordindates ().getCooridnates (), machineCoordLabel );
        setCoordLabels ( grblState.getWorkCoordindates ().getCooridnates (), workCoordLabel );

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

    // TODO check this code or delete it

    boolean unlockSent = false;

    @Inject
    @Optional
    public void sentNotified ( @UIEventTopic(IEvents.GRBL_SENT) IGrblRequest command ) {

        LOG.trace ( "sentNotified: command=" + command );

        if ( command != null && command.isUnlock () ) { // unlock
            unlockSent = true;
        }

    }

    @Inject
    @Optional
    public void receivedNotified ( @UIEventTopic(IEvents.GRBL_RECEIVED) IGrblResponse response ) {

        if ( response == null ) {
            LOG.warn ( "receivedNotified: response == null" );
            return;
        }

        if ( response.getMessage () == null ) {
            LOG.warn ( "receivedNotified: line == null" );
            return;
        }

        if ( !response.getMessage ().startsWith ( "<" ) ) {
            LOG.trace ( "receivedNotified: response=" + response + " unlockSent=" + unlockSent );
        }

        if ( unlockSent && response.getMessage ().startsWith ( "ok" ) ) {
            unlockSent = false;
            // first 'ok' after alarm comes with unlock an periodic $G command, then the coordinate system will be set

            // restoreCoordinateSystem ();
        }

    }

}
