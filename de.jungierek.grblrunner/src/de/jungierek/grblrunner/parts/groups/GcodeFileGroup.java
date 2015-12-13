package de.jungierek.grblrunner.parts.groups;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.GuiFactory;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.PartTools;

public class GcodeFileGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeFileGroup.class );

    private static final String GROUP_NAME = "Gcode";

    @Inject
    private PartTools partTools;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private Label gcodeMinLabel;
    private Label gcodeMaxLabel;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int partCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_COLS )).intValue ();
        int groupCols = ((Integer) context.get ( IPersistenceKeys.KEY_PART_GROUP_COLS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        final int cols = 9;
        group.setLayout ( new GridLayout ( cols, true ) );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "min", 1 );
        gcodeMinLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "max", 1 );
        gcodeMaxLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

    }

    @Inject
    @Optional
    public void macroGeneratedNotified ( @UIEventTopic(IEvents.GCODE_MACRO_GENERATED) Object dummy ) {

        LOG.debug ( "macroGeneratedNotified:" );

        gcodeMinLabel.setText ( "" + gcodeProgram.getMin () );
        gcodeMaxLabel.setText ( "" + gcodeProgram.getMax () );

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvents.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        gcodeMinLabel.setText ( "" + gcodeProgram.getMin () );
        gcodeMaxLabel.setText ( "" + gcodeProgram.getMax () );

    }

}
