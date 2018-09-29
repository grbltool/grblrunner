package de.jungierek.grblrunner.part.group;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.tool.GuiFactory;

public class StateCoolantGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( StateCoolantGroup.class );

    private static final String GROUP_NAME = "Coolant";

    private Label coolantLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 1, false ) );
        coolantLabel = GuiFactory.createHeadingLabel ( group, "", 1 );

    }

    @Inject
    @Optional
    public void updateCoolantModeNotified ( @UIEventTopic(IEvent.UPDATE_COOLANT_MODE) String coolantMode ) {

        LOG.trace ( "updateCoolantModeNotified: coolantMode=" + coolantMode );
        coolantLabel.setText ( coolantMode );

    }

}
