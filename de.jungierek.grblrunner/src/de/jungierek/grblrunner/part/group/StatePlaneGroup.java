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

public class StatePlaneGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StatePlaneGroup.class );

    private static final String GROUP_NAME = "Plane";

    private Label planeLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 1, false ) );
        planeLabel = GuiFactory.createHeadingLabel ( group, SWT.CENTER, "", 1 );

    }

    @Inject
    @Optional
    public void updatePlaneNotified ( @UIEventTopic(IEvent.UPDATE_PLANE) String plane ) {

        LOG.trace ( "updatePlaneNotified: plane=" + plane );
        planeLabel.setText ( plane );

    }

}
