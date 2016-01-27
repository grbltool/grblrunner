package de.jungierek.grblrunner.parts;

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

import de.jungierek.grblrunner.parts.groups.StateCoolantGroup;
import de.jungierek.grblrunner.parts.groups.StateCoordinatesGroup;
import de.jungierek.grblrunner.parts.groups.StateDistanceGroup;
import de.jungierek.grblrunner.parts.groups.StateFeedrateGroup;
import de.jungierek.grblrunner.parts.groups.StateGroup;
import de.jungierek.grblrunner.parts.groups.StateModalModeGroup;
import de.jungierek.grblrunner.parts.groups.StatePlaneGroup;
import de.jungierek.grblrunner.parts.groups.StateSpindleGroup;
import de.jungierek.grblrunner.parts.groups.StateToolGroup;
import de.jungierek.grblrunner.parts.groups.StateUnitGroup;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class StatePart {

    private static final Logger LOG = LoggerFactory.getLogger ( StatePart.class );

    @Inject
    IGcodeService gcode;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    // prevent groups from garbage collection
    private StateCoordinatesGroup stateCoordinatesGroup;
    private StateGroup stateGroup;
    private StateFeedrateGroup stateFeedrateGroup;
    private StateToolGroup stateToolGroup;
    private StateSpindleGroup stateSpindleGroup;
    private StateCoolantGroup stateCoolantGroup;
    private StateModalModeGroup stateModalModeGroup;
    private StatePlaneGroup statePlaneGroup;
    private StateUnitGroup stateUnitsGroup;
    private StateDistanceGroup stateDistanceGroup;

    @Inject
    public StatePart () {}

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );

        // collect groups
        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );

        // state
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 1 );
        stateGroup = ContextInjectionFactory.make ( StateGroup.class, context );

        // coordinates
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_ROWS, 3 );
        stateCoordinatesGroup = ContextInjectionFactory.make ( StateCoordinatesGroup.class, context );

        // all following groups allocating only 1 column
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 1 );

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