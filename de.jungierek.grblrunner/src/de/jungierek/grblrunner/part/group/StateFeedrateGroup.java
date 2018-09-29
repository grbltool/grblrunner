package de.jungierek.grblrunner.part.group;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tool.GuiFactory;

public class StateFeedrateGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateFeedrateGroup.class );

    private static final String GROUP_NAME = "Feedrate";

    private Label feedrateLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 2, false ) );
        feedrateLabel = GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "0", 1 );
        new Label ( group, SWT.LEFT ).setText ( IConstant.SPEED_MM_MIN_TEXT );

    }

    @Inject
    @Optional
    public void updateFeedrateNotified ( @UIEventTopic(IEvent.UPDATE_FEEDRATE) String feedrate ) {

        LOG.trace ( "updateFeedrateNotified: feedrate=" + feedrate );
        feedrateLabel.setText ( feedrate );

    }

}
