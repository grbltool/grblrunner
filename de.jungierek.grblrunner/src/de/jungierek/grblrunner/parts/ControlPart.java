package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.groups.ControlAutolevelGroup;
import de.jungierek.grblrunner.parts.groups.ControlCycleGroup;
import de.jungierek.grblrunner.parts.groups.ControlGrblGroup;
import de.jungierek.grblrunner.parts.groups.ControlInfoGroup;
import de.jungierek.grblrunner.parts.groups.ControlMoveGroup;
import de.jungierek.grblrunner.parts.groups.ControlProbeGroup;
import de.jungierek.grblrunner.parts.groups.ControlSpindleGroup;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class ControlPart {
    
    private static final Logger LOG = LoggerFactory.getLogger ( ControlPart.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    // prevent groups from garbage collection
    private ControlInfoGroup controlInfoGroup;
    private ControlCycleGroup controlCycleGroup;
    private ControlMoveGroup controlMoveGroup;
    private ControlGrblGroup controlBasicsGroup;
    private ControlSpindleGroup controlSpindleGroup;
    private ControlProbeGroup controlProbeGroup;
    private ControlAutolevelGroup controlAutolevelGroup;

    // ********************************************************

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui:" );

	    int cols = 8;
        parent.setLayout ( new GridLayout ( cols, true ) ); // equal width column
        
        // collect groups
        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );

        // settings $$, parameters $#, parser $G, build $I, startup $N
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        controlInfoGroup = ContextInjectionFactory.make ( ControlInfoGroup.class, context );
        
        // cycle start, feed hold, reset
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols / 4 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 2 );
        controlCycleGroup = ContextInjectionFactory.make ( ControlCycleGroup.class, context );

        // XY
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols / 2 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 2 );
        controlMoveGroup = ContextInjectionFactory.make ( ControlMoveGroup.class, context );
        
        // home $H, unlock $X, check $C
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols / 4 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 2 );
        controlBasicsGroup = ContextInjectionFactory.make ( ControlGrblGroup.class, context );

        // spindle
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 5 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        controlSpindleGroup = ContextInjectionFactory.make ( ControlSpindleGroup.class, context );
        
        // probe
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 3 );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        controlProbeGroup = ContextInjectionFactory.make ( ControlProbeGroup.class, context );
        
        // scan
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        controlAutolevelGroup = ContextInjectionFactory.make ( ControlAutolevelGroup.class, context );

    }
	
    @Focus
    public void focus () {}

}