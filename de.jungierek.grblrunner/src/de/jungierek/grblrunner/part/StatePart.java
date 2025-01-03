package de.jungierek.grblrunner.part;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
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

public class StatePart {

    private static final Logger LOG = LoggerFactory.getLogger ( StatePart.class );

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    // prevent groups from garbage collection
    @SuppressWarnings("unused")
    private StateCoordinatesGroup stateCoordinatesGroup;

    @SuppressWarnings("unused")
    private StateGroup stateGroup;

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

    @Inject
    public StatePart () {}

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

    }
    
}