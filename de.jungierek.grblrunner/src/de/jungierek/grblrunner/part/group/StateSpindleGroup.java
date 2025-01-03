package de.jungierek.grblrunner.part.group;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tool.GuiFactory;

public class StateSpindleGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateSpindleGroup.class );

    private static final String GROUP_NAME = "Spindle";

    private Label spindleModeLabel;
    private Label spindlespeedLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 3, false ) );

        spindleModeLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 1 );
        spindlespeedLabel = GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "0", 1 );
        new Label ( group, SWT.LEFT ).setText ( "rpm" );

    }

    @Inject
    @Optional
    public void updateSpindleModeNotified ( @UIEventTopic(IEvent.UPDATE_SPINDLE_MODE) String spindleMode ) {

        LOG.trace ( "updateSpindleModeNotified: tool=" + spindleMode );

        spindleModeLabel.setText ( spindleMode );

    }

    @Inject
    @Optional
    public void updateSpindlespeedNotified ( @UIEventTopic(IEvent.UPDATE_SPINDLESPEED) String spindlespeed ) {

        LOG.trace ( "updateSpindlespeedNotified: spindlespeed=" + spindlespeed );
        spindlespeedLabel.setText ( spindlespeed );

    }

}
