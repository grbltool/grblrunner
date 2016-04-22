package de.jungierek.grblrunner.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.part.group.CommandGroup;
import de.jungierek.grblrunner.part.group.GcodeLargeGroup;
import de.jungierek.grblrunner.part.group.ProgressGroup;
import de.jungierek.grblrunner.part.group.StatusLineGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class StatusLinePart {

    private static final Logger LOG = LoggerFactory.getLogger ( StatusLinePart.class );

    @Inject
    private IGcodeService gcodeService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    UISynchronize sync;

    private GcodeLargeGroup gcodeLargeGroup;
    private CommandGroup commandGroup;
    private StatusLineGroup statusLineGroup;
    private ProgressGroup progressGroup;

    @Focus
    public void SetFocusToCommandText () {

        commandGroup.setFocus ();

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent + " layout=" + parent.getLayout () );

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );
        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_COLS, 1 ); // all groups have a width of 1 column

        // collect groups and hold reference to prevent garbage collection
        gcodeLargeGroup = ContextInjectionFactory.make ( GcodeLargeGroup.class, context );
        commandGroup = ContextInjectionFactory.make ( CommandGroup.class, context );
        statusLineGroup = ContextInjectionFactory.make ( StatusLineGroup.class, context );
        progressGroup = ContextInjectionFactory.make ( ProgressGroup.class, context );

    }

    @Inject
    @Optional
    public void msgErrorNotified ( @UIEventTopic(IEvent.MESSAGE_ERROR) String msg ) {

        LOG.trace ( "msgErrorNotified: msg=" + msg );

        MessageDialog.openError ( shell, "Error", msg );

    }

}