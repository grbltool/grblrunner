package de.jungierek.grblrunner.part.group;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
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

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tool.GuiFactory;

public class GcodeFileGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeFileGroup.class );

    private static final String GROUP_NAME = "Gcode";

    @Inject
    private IGcodeProgram gcodeProgram;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    private Label gcodeMinLabel;
    private Label gcodeMaxLabel;
    private Label gcodeDimLabel;
    private Label gcodeTimeLabel;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_COLS) int partCols, @Named(IContextKey.PART_GROUP_ROWS) int groupRows, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        final int cols = 11;
        group.setLayout ( new GridLayout ( cols, true ) );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "min:", 1 );
        gcodeMinLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "max:", 1 );
        gcodeMaxLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "dim:", 1 );
        gcodeDimLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 2 );

        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "time:", 1 );
        gcodeTimeLabel = GuiFactory.createHeadingLabel ( group, SWT.LEFT, "", 1 );

    }

    private void refreshGuiData () {

        final IGcodePoint min = gcodeProgram.getMin ();
        final IGcodePoint max = gcodeProgram.getMax ();

        gcodeMinLabel.setText ( "" + min );
        gcodeMaxLabel.setText ( "" + max );
        gcodeDimLabel.setText ( "" + max.sub ( min ) );
        gcodeTimeLabel.setText ( "" + gcodeProgram.getDuration () + "min" );

    }

    @Inject
    @Optional
    public void macroGeneratedNotified ( @UIEventTopic(IEvent.GCODE_MACRO_GENERATED) Object dummy ) {

        LOG.debug ( "macroGeneratedNotified:" );

        refreshGuiData ();

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        refreshGuiData ();

    }

    @Inject
    @Optional
    public void programOptimizedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_OPTIMIZED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        refreshGuiData ();

    }

}
