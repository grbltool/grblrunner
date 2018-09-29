package de.jungierek.grblrunner.part.group;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.tool.GuiFactory;

public class StateGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateGroup.class );

    private static final String GROUP_NAME = "Grbl State";

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private static final HashMap<EGrblState, int []> stateColors;
    // TODO colors to pref
    static {
        stateColors = new HashMap<> ();
        stateColors.put ( EGrblState.IDLE, new int [] { SWT.COLOR_BLACK, SWT.COLOR_GRAY } );
        stateColors.put ( EGrblState.RUN, new int [] { SWT.COLOR_BLACK, SWT.COLOR_GREEN } );
        stateColors.put ( EGrblState.HOLD, new int [] { SWT.COLOR_BLACK, SWT.COLOR_YELLOW } );
        stateColors.put ( EGrblState.HOME, new int [] { SWT.COLOR_BLACK, SWT.COLOR_MAGENTA } );
        stateColors.put ( EGrblState.ALARM, new int [] { SWT.COLOR_WHITE, SWT.COLOR_RED } );
        stateColors.put ( EGrblState.CHECK, new int [] { SWT.COLOR_BLACK, SWT.COLOR_CYAN } );
        stateColors.put ( EGrblState.SLEEP, new int [] { SWT.COLOR_DARK_GRAY, SWT.COLOR_GRAY } );
        stateColors.put ( EGrblState.DOOR, new int [] { SWT.COLOR_BLACK, SWT.COLOR_DARK_YELLOW } );
        stateColors.put ( EGrblState.JOG, new int [] { SWT.COLOR_WHITE, SWT.COLOR_DARK_BLUE } );
    };

    private Label stateLabel;
    private Label [] extraStateLabel = new Label [3];
    private Color [] unconnectedColors = new Color [2];

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        final FillLayout l = new FillLayout ();
        l.type = SWT.VERTICAL;
        group.setLayout ( l );
        stateLabel = new Label ( group, SWT.CENTER );
        stateLabel.setText ( IConstant.UNCONNECTED_TEXT );
        unconnectedColors [0] = stateLabel.getForeground ();
        unconnectedColors [1] = stateLabel.getBackground ();
        
        for ( int i = 0; i < extraStateLabel.length; i++ ) {
            extraStateLabel [i] = new Label ( group, SWT.CENTER );
            extraStateLabel [i].setVisible ( false );
        }

    }

    private void setStateLabel ( EGrblState state ) {

        LOG.trace ( "setStateLabel: state=" + state );

        stateLabel.setText ( state.getText () );

        int [] colors = stateColors.get ( state );
        stateLabel.setForeground ( shell.getDisplay ().getSystemColor ( colors [0] ) );
        stateLabel.setBackground ( shell.getDisplay ().getSystemColor ( colors [1] ) );

    }

    private void resetStateLabel () {

        LOG.trace ( "resetStateLabel:" );

        stateLabel.setText ( IConstant.UNCONNECTED_TEXT );
        stateLabel.setForeground ( unconnectedColors [0] );
        stateLabel.setBackground ( unconnectedColors [1] );

    }

    private void hideExtraStateLabels () {

        LOG.trace ( "resetExtraStateLabels:" );

        for ( int i = 0; i < extraStateLabel.length; i++ ) {
            extraStateLabel [i].setVisible ( false );
        }

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String [] line ) {

        LOG.debug ( "alarmNotified: start" );
        setStateLabel ( EGrblState.ALARM );

        hideExtraStateLabels ();

        int [] colors = stateColors.get ( EGrblState.ALARM );
        for ( int i = 0; i < line.length; i++ ) {
            if ( i - 1 < extraStateLabel.length ) {
                extraStateLabel [i].setVisible ( true );
                extraStateLabel [i].setText ( line [i] );
                extraStateLabel [i].setForeground ( shell.getDisplay ().getSystemColor ( colors [0] ) );
                extraStateLabel [i].setBackground ( shell.getDisplay ().getSystemColor ( colors [1] ) );
            }
            else {
                LOG.error ( "alarmNotified: alarm messages exceeds extra labels i=" + i + " line=" + line [i] );
            }
        }

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );

        resetStateLabel ();
        hideExtraStateLabels ();

    }

    @Inject
    @Optional
    public void updateStateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState grblState ) {

        LOG.debug ( "updateStateNotified: grblState=" + grblState );
        setStateLabel ( grblState.getGrblState () );
        if ( !EGrblState.ALARM.equals ( grblState.getGrblState () ) ) {
            hideExtraStateLabels ();
        }

    }

}
