package de.jungierek.grblrunner.part;

import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.part.group.GcodeViewGroup;

public class GcodeViewPart {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewPart.class );

    // prevent from garbage collection
    private GcodeViewGroup gcodeViewGroup;

    @Inject
    public GcodeViewPart () {}

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context, MPart part ) {
        
        LOG.debug ( "createGui:" );

        final int cols = 1;
        parent.setLayout ( new GridLayout ( cols, true ) );
        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_COLS, 1 ); // all groups have a width of 1 column

        // collect groups
        gcodeViewGroup = ContextInjectionFactory.make ( GcodeViewGroup.class, context );
        
        restoreViewState ( part );

    }

    public GcodeViewGroup getGcodeViewGroup () {

        return gcodeViewGroup;

    }
    
    private void restoreViewState ( MPart part ) {

        LOG.debug ( "restoreViewState: part=" + part );

        boolean viewGrid = false;
        boolean viewGcode = false;
        boolean viewAltitude = false;
        boolean viewWorkArea = false;

        List<MToolBarElement> children = part.getToolbar ().getChildren ();
        for ( Iterator<MToolBarElement> iterator = children.iterator (); iterator.hasNext (); ) {
            MToolBarElement element = iterator.next ();
            if ( element instanceof MDirectToolItem ) {
                MDirectToolItem item = (MDirectToolItem) element;
                String type = item.getPersistedState ().get ( "type" );
                if ( type != null ) {
                    switch ( type ) {
                        case "grid":
                            viewGrid = item.isSelected ();
                            break;
                        case "gcode":
                            viewGcode = item.isSelected ();
                            break;
                        case "altitude":
                            viewAltitude = item.isSelected ();
                            break;
                        case "workarea":
                            viewWorkArea = item.isSelected ();
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        gcodeViewGroup.setInitialViewFlags ( viewGrid, viewGcode, viewAltitude, viewWorkArea );

    }

    @PersistState
    public void persistState () {

        LOG.debug ( "persistState:" );

        gcodeViewGroup.savePersistedState ();

    }
    
}