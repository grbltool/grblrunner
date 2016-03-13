package de.jungierek.grblrunner.parts.groups;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.tools.GuiFactory;

public class StateGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateGroup.class );

    private static final String GROUP_NAME = "GRBL State";

    private static final String UNCONNECTED_TEXT = "UNCONNECTED";

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

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

    private Label stateLabel;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IContextKey.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IContextKey.KEY_PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.KEY_PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new FillLayout () );
        stateLabel = new Label ( group, SWT.CENTER );
        stateLabel.setText ( UNCONNECTED_TEXT );

    }

    private void setStateLabel ( EGrblState state ) {

        LOG.trace ( "setStateLabel: state=" + state );

        stateLabel.setText ( state.getText () );

        int [] colors = stateColors.get ( state );
        stateLabel.setForeground ( shell.getDisplay ().getSystemColor ( colors[0] ) );
        stateLabel.setBackground ( shell.getDisplay ().getSystemColor ( colors[1] ) );

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
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.debug ( "disconnectedNotified: param=" + param );
        stateLabel.setText ( UNCONNECTED_TEXT );

    }

    @Inject
    @Optional
    public void updateStateNotified ( @UIEventTopic(IEvents.UPDATE_STATE) IGcodeGrblState grblState ) {

        LOG.debug ( "updateStateNotified: grblState=" + grblState );
        setStateLabel ( grblState.getGrblState () );

    }

}
