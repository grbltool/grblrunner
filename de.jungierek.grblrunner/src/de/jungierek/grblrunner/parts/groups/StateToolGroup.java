package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class StateToolGroup {
    
    private static final Logger LOG = LoggerFactory.getLogger ( StateToolGroup.class );

    private static final String GROUP_NAME = "Tool";

    private Label toolLabel;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        group.setLayout ( new GridLayout ( 1, false ) );
        toolLabel = GuiFactory.createHeadingLabel ( group, "", 1 );

    }

    @Inject
    @Optional
    public void updateToolNotified ( @UIEventTopic(IEvents.UPDATE_TOOL) String tool ) {

        LOG.trace ( "updateToolNotified: tool=" + tool );
        toolLabel.setText ( tool );

    }

}
