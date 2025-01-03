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

public class StateDistanceGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateDistanceGroup.class );

    private static final String GROUP_NAME = "Distance";

    private Label distanceModeLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 1, false ) );
        distanceModeLabel = GuiFactory.createHeadingLabel ( group, SWT.CENTER, "", 1 );

    }

    @Inject
    @Optional
    public void updateDistanceModeNotified ( @UIEventTopic(IEvent.UPDATE_DISTANCE_MODE) String distanceMode ) {

        LOG.trace ( "updateDistanceModeNotified: distanceMode=" + distanceMode );
        distanceModeLabel.setText ( distanceMode );

    }

}
