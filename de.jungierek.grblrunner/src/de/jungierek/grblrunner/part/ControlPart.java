package de.jungierek.grblrunner.part;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.part.group.ControlAutolevelGroup;
import de.jungierek.grblrunner.part.group.ControlCycleGroup;
import de.jungierek.grblrunner.part.group.ControlGrblGroup;
import de.jungierek.grblrunner.part.group.ControlInfoGroup;
import de.jungierek.grblrunner.part.group.ControlMoveGroup;
import de.jungierek.grblrunner.part.group.ControlOverrideGroup;
import de.jungierek.grblrunner.part.group.ControlProbeGroup;
import de.jungierek.grblrunner.part.group.ControlSpindleGroup;

public class ControlPart {
    
    private static final Logger LOG = LoggerFactory.getLogger ( ControlPart.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    // prevent groups from garbage collection
    @SuppressWarnings("unused")
    private ControlInfoGroup controlInfoGroup;

    @SuppressWarnings("unused")
    private ControlCycleGroup controlCycleGroup;

    @SuppressWarnings("unused")
    private ControlMoveGroup controlMoveGroup;

    @SuppressWarnings("unused")
    private ControlGrblGroup controlBasicsGroup;

    @SuppressWarnings("unused")
    private ControlSpindleGroup controlSpindleGroup;

    @SuppressWarnings("unused")
    private ControlProbeGroup controlProbeGroup;

    @SuppressWarnings("unused")
    private ControlOverrideGroup controlOverrideGroup;

    @SuppressWarnings("unused")
    private ControlAutolevelGroup controlAutolevelGroup;

    // ********************************************************

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui:" );

	    int cols = 8;
        parent.setLayout ( new GridLayout ( cols, true ) ); // equal width column
        
        // collect groups
        context.set ( IContextKey.PART_COLS, cols );

        // settings $$, parameters $#, parser $G, build $I, startup $N
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlInfoGroup = ContextInjectionFactory.make ( ControlInfoGroup.class, context );
        
        // cycle start, feed hold, reset
        context.set ( IContextKey.PART_GROUP_COLS, cols / 4 );
        context.set ( IContextKey.PART_GROUP_ROWS, 2 );
        controlCycleGroup = ContextInjectionFactory.make ( ControlCycleGroup.class, context );

        // XY
        context.set ( IContextKey.PART_GROUP_COLS, cols / 2 );
        context.set ( IContextKey.PART_GROUP_ROWS, 2 );
        controlMoveGroup = ContextInjectionFactory.make ( ControlMoveGroup.class, context );
        
        // home $H, unlock $X, check $C
        context.set ( IContextKey.PART_GROUP_COLS, cols / 4 );
        context.set ( IContextKey.PART_GROUP_ROWS, 2 );
        controlBasicsGroup = ContextInjectionFactory.make ( ControlGrblGroup.class, context );

        // spindle
        context.set ( IContextKey.PART_GROUP_COLS, 5 );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlSpindleGroup = ContextInjectionFactory.make ( ControlSpindleGroup.class, context );
        
        // probe
        context.set ( IContextKey.PART_GROUP_COLS, 3 );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlProbeGroup = ContextInjectionFactory.make ( ControlProbeGroup.class, context );
        
        // override
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlOverrideGroup = ContextInjectionFactory.make ( ControlOverrideGroup.class, context );

        // scan
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlAutolevelGroup = ContextInjectionFactory.make ( ControlAutolevelGroup.class, context );

    }
	
    @Focus
    public void focus () {}

}