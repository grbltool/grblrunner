package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
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
import de.jungierek.grblrunner.tools.GuiFactory;

public class StateSpindleGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateSpindleGroup.class );

    private static final String GROUP_NAME = "Spindle";

    private Label spindleModeLabel;
    private Label spindlespeedLabel;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

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
