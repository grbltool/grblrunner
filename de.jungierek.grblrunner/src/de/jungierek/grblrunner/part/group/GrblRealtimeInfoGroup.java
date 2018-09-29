package de.jungierek.grblrunner.part.group;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.tool.GuiFactory;

public class GrblRealtimeInfoGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblRealtimeInfoGroup.class );

    private static final String GROUP_NAME = "Realtime Info";

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    Shell shell;

    @Inject
    private Display display;

    private Label feedOverrideLabel;
    private Label spindleOverrideLabel;
    private Label feedRateLabel;
    private Label spindleSpeedLabel;
    private Label rxBufferLabel;
    private Label plannerBufferLabel;
    private Label pinStateLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        final int cols = 12;
        group.setLayout ( new GridLayout ( cols, true ) );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feed rate:", 2 );
        feedRateLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "spindle speed:", 2 );
        spindleSpeedLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "rx buffer:", 2 );
        rxBufferLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "override:", 2 );
        feedOverrideLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "override:", 2 );
        spindleOverrideLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "planner buffer:", 2 );
        plannerBufferLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 1 );
        pinStateLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 1 );

    }

    @Inject
    @Optional
    public void stateUpdateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState state ) {

        LOG.debug ( "stateUpdateNotified: state=" + state );

        // update overrides
        int feedOverride = state.getFeedOverride ();
        if ( feedOverride > 0 ) {
            feedOverrideLabel.setText ( "" + feedOverride + "%" );
        }
        int spindleOverride = state.getSpindleOverride ();
        if ( spindleOverride > 0 ) {
            spindleOverrideLabel.setText ( "" + spindleOverride + "%" );
        }

        // update speeds
        feedRateLabel.setText ( "" + state.getFeedRate () + " " + IConstant.SPEED_MM_MIN_TEXT );
        spindleSpeedLabel.setText ( "" + state.getSpindleSpeed () + " " + IConstant.SPEED_RPM_TEXT );

        // update buffers
        rxBufferLabel.setText ( "" + state.getAvailableRxBufferSize () );
        plannerBufferLabel.setText ( "" + state.getAvailablePlannerBufferSize () );

        String pins = state.getPinState ();
        if ( pins != null ) {
            pinStateLabel.setText ( pins );
        }

    }

}
