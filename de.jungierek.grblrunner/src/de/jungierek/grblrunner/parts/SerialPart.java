 
package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.groups.SerialActionsGroup;
import de.jungierek.grblrunner.parts.groups.SerialAutoConnectGroup;
import de.jungierek.grblrunner.parts.groups.SerialPortGroup;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

// TODO_PREF Auswahlfeld für Baudrate

public class SerialPart {
    
    private static final Logger LOG = LoggerFactory.getLogger ( SerialPart.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    Shell shell;

    private SerialActionsGroup serialActionsGroup;
    private SerialPortGroup serialPortGroup;
    private SerialAutoConnectGroup serialAutoConnectGroup;
    
    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        createGuiV1 ( parent, context );

    }

    public void createGuiV3 ( Composite parent, IEclipseContext context ) {

        final int cols = 3;
        parent.setLayout ( new GridLayout ( cols, true ) );

        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 ); // for all groups

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols - 1 );
        serialPortGroup = ContextInjectionFactory.make ( SerialPortGroup.class, context );

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 1 );
        serialAutoConnectGroup = ContextInjectionFactory.make ( SerialAutoConnectGroup.class, context );

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ORIENTATION, SWT.HORIZONTAL );
        serialActionsGroup = ContextInjectionFactory.make ( SerialActionsGroup.class, context );

    }

    public void createGuiV2 ( Composite parent, IEclipseContext context ) {

        final int cols = 3;
        parent.setLayout ( new GridLayout ( cols, true ) );

        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 ); // for all groups

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ORIENTATION, SWT.HORIZONTAL );
        serialActionsGroup = ContextInjectionFactory.make ( SerialActionsGroup.class, context );

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols - 1 );
        serialPortGroup = ContextInjectionFactory.make ( SerialPortGroup.class, context );

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 1 );
        serialAutoConnectGroup = ContextInjectionFactory.make ( SerialAutoConnectGroup.class, context );

    }

    public void createGuiV1 ( Composite parent, IEclipseContext context ) {

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );

        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );

        // collect groups

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols - 1 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        serialPortGroup = ContextInjectionFactory.make ( SerialPortGroup.class, context );

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 1 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 2 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ORIENTATION, SWT.VERTICAL );
        serialActionsGroup = ContextInjectionFactory.make ( SerialActionsGroup.class, context );

        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols - 1 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        serialAutoConnectGroup = ContextInjectionFactory.make ( SerialAutoConnectGroup.class, context );

    }

}