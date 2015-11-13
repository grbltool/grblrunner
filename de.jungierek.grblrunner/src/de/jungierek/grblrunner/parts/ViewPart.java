package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.parts.groups.GcodeViewGroup;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class ViewPart {

    private static final Logger LOG = LoggerFactory.getLogger ( ViewPart.class );

    // prevent from garbage collection
    @SuppressWarnings("unused")
    private GcodeViewGroup gcodeViewGroup;

    @Inject
    public ViewPart () {}

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {
        
        LOG.debug ( "createGui:" );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, true ) );
        context.set ( IPersistenceKeys.KEY_PART_COLS, cols );
        context.set ( IPersistenceKeys.KEY_PART_GROUP_COLS, 1 ); // all groups have a width of 1 column

        // collect groups
        gcodeViewGroup = ContextInjectionFactory.make ( GcodeViewGroup.class, context );

    }

    public GcodeViewGroup getGcodeViewGroup () {

        return gcodeViewGroup;

    }
    

}