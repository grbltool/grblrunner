 
package de.jungierek.grblrunner.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.part.group.SerialActionsGroup;
import de.jungierek.grblrunner.part.group.SerialAutoConnectGroup;
import de.jungierek.grblrunner.part.group.SerialPortGroup;
import de.jungierek.grblrunner.service.serial.ISerialService;

// TODO_PREF Auswahlfeld für Baudrate

@SuppressWarnings("restriction")
public class SerialPart {
    
    private static final Logger LOG = LoggerFactory.getLogger ( SerialPart.class );

    // prevrent from garbage collection
    @SuppressWarnings("unused")
    private SerialActionsGroup serialActionsGroup;

    @SuppressWarnings("unused")
    private SerialPortGroup serialPortGroup;

    @SuppressWarnings("unused")
    private SerialAutoConnectGroup serialAutoConnectGroup;
    
    @Inject
    public void setBaudrate ( ISerialService serialService, @Preference(nodePath = IConstant.PREFERENCE_NODE, value = IPreferenceKey.BAUDRATE) int baudrate ) {

        LOG.info ( "setBaudrate : " + baudrate );
        serialService.setBaudrate ( baudrate );

    }
    
    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui:" );

        createGuiV1 ( parent, context );

    }

    public void createGuiV3 ( Composite parent, IEclipseContext context ) {

        final int cols = 3;
        parent.setLayout ( new GridLayout ( cols, true ) );

        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 ); // for all groups

        context.set ( IContextKey.PART_GROUP_COLS, cols - 1 );
        serialPortGroup = ContextInjectionFactory.make ( SerialPortGroup.class, context );

        context.set ( IContextKey.PART_GROUP_COLS, 1 );
        serialAutoConnectGroup = ContextInjectionFactory.make ( SerialAutoConnectGroup.class, context );

        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ORIENTATION, SWT.HORIZONTAL );
        serialActionsGroup = ContextInjectionFactory.make ( SerialActionsGroup.class, context );

    }

    public void createGuiV2 ( Composite parent, IEclipseContext context ) {

        final int cols = 3;
        parent.setLayout ( new GridLayout ( cols, true ) );

        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 ); // for all groups

        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ORIENTATION, SWT.HORIZONTAL );
        serialActionsGroup = ContextInjectionFactory.make ( SerialActionsGroup.class, context );

        context.set ( IContextKey.PART_GROUP_COLS, cols - 1 );
        serialPortGroup = ContextInjectionFactory.make ( SerialPortGroup.class, context );

        context.set ( IContextKey.PART_GROUP_COLS, 1 );
        serialAutoConnectGroup = ContextInjectionFactory.make ( SerialAutoConnectGroup.class, context );

    }

    public void createGuiV1 ( Composite parent, IEclipseContext context ) {

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );

        context.set ( IContextKey.PART_COLS, cols );

        // collect groups

        context.set ( IContextKey.PART_GROUP_COLS, cols - 1 );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        serialPortGroup = ContextInjectionFactory.make ( SerialPortGroup.class, context );

        context.set ( IContextKey.PART_GROUP_COLS, 1 );
        context.set ( IContextKey.PART_GROUP_ROWS, 2 );
        context.set ( IContextKey.PART_GROUP_ORIENTATION, SWT.VERTICAL );
        serialActionsGroup = ContextInjectionFactory.make ( SerialActionsGroup.class, context );

        context.set ( IContextKey.PART_GROUP_COLS, cols - 1 );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        serialAutoConnectGroup = ContextInjectionFactory.make ( SerialAutoConnectGroup.class, context );

    }

}