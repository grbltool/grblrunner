package de.jungierek.grblrunner.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.part.group.ControlAutolevelGroup;
import de.jungierek.grblrunner.part.group.ControlMoveGroup;
import de.jungierek.grblrunner.part.group.ControlOverrideGroup;
import de.jungierek.grblrunner.part.group.ControlProbeGroup;
import de.jungierek.grblrunner.part.group.ControlSpindleGroup;
import de.jungierek.grblrunner.part.group.StateCoolantGroup;
import de.jungierek.grblrunner.part.group.StateCoordinatesGroup;
import de.jungierek.grblrunner.part.group.StateDistanceGroup;
import de.jungierek.grblrunner.part.group.StateFeedrateGroup;
import de.jungierek.grblrunner.part.group.StateGroup;
import de.jungierek.grblrunner.part.group.StateModalModeGroup;
import de.jungierek.grblrunner.part.group.StatePlaneGroup;
import de.jungierek.grblrunner.part.group.StateSpindleGroup;
import de.jungierek.grblrunner.part.group.StateToolGroup;
import de.jungierek.grblrunner.part.group.StateUnitGroup;

public class GrblPart {

    private static final Logger LOG = LoggerFactory.getLogger ( GrblPart.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    // prevent groups from garbage collection
    @SuppressWarnings("unused")
    private StateCoordinatesGroup stateCoordinatesGroup;

    @SuppressWarnings("unused")
    private StateGroup stateGroup;

    @SuppressWarnings("unused")
    private ControlOverrideGroup overrideGroup;

    @SuppressWarnings("unused")
    private StateFeedrateGroup stateFeedrateGroup;

    @SuppressWarnings("unused")
    private StateToolGroup stateToolGroup;

    @SuppressWarnings("unused")
    private StateSpindleGroup stateSpindleGroup;

    @SuppressWarnings("unused")
    private StateCoolantGroup stateCoolantGroup;

    @SuppressWarnings("unused")
    private StateModalModeGroup stateModalModeGroup;

    @SuppressWarnings("unused")
    private StatePlaneGroup statePlaneGroup;

    @SuppressWarnings("unused")
    private StateUnitGroup stateUnitsGroup;

    @SuppressWarnings("unused")
    private StateDistanceGroup stateDistanceGroup;

    @SuppressWarnings("unused")
    private ControlMoveGroup controlMoveGroup;

    @SuppressWarnings("unused")
    private ControlSpindleGroup controlSpindleGroup;

    @SuppressWarnings("unused")
    private ControlProbeGroup controlProbeGroup;

    @SuppressWarnings("unused")
    private ControlAutolevelGroup controlAutolevelGroup;

    @Inject
    public GrblPart () {}

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui:" );

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );

        // collect groups
        context.set ( IContextKey.PART_COLS, cols );

        // state
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        stateGroup = ContextInjectionFactory.make ( StateGroup.class, context );

        // coordinates
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 3 );
        stateCoordinatesGroup = ContextInjectionFactory.make ( StateCoordinatesGroup.class, context );

        // XY
        context.set ( IContextKey.PART_GROUP_COLS, cols / 2 );
        context.set ( IContextKey.PART_GROUP_ROWS, 2 );
        controlMoveGroup = ContextInjectionFactory.make ( ControlMoveGroup.class, context );

        // spindle
        context.set ( IContextKey.PART_GROUP_COLS, cols / 2 );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlSpindleGroup = ContextInjectionFactory.make ( ControlSpindleGroup.class, context );

        // probe
        context.set ( IContextKey.PART_GROUP_COLS, cols / 2 );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlProbeGroup = ContextInjectionFactory.make ( ControlProbeGroup.class, context );

        // all following groups allocating only 1 column
        context.set ( IContextKey.PART_GROUP_COLS, 1 );

        // feedrate
        stateFeedrateGroup = ContextInjectionFactory.make ( StateFeedrateGroup.class, context );

        // tool
        stateToolGroup = ContextInjectionFactory.make ( StateToolGroup.class, context );

        // spindle
        stateSpindleGroup = ContextInjectionFactory.make ( StateSpindleGroup.class, context );

        // coolant
        stateCoolantGroup = ContextInjectionFactory.make ( StateCoolantGroup.class, context );

        // modal mode
        stateModalModeGroup = ContextInjectionFactory.make ( StateModalModeGroup.class, context );

        // Plane
        statePlaneGroup = ContextInjectionFactory.make ( StatePlaneGroup.class, context );

        // Units
        stateUnitsGroup = ContextInjectionFactory.make ( StateUnitGroup.class, context );

        // Distance
        stateDistanceGroup = ContextInjectionFactory.make ( StateDistanceGroup.class, context );

        // overrides
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        overrideGroup = ContextInjectionFactory.make ( ControlOverrideGroup.class, context );

        // scan
        context.set ( IContextKey.PART_GROUP_COLS, cols );
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );
        controlAutolevelGroup = ContextInjectionFactory.make ( ControlAutolevelGroup.class, context );

    }
    
}