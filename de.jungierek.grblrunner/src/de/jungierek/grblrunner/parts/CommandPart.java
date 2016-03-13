package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.parts.groups.CommandGroup;

public class CommandPart {

    private static final Logger LOG = LoggerFactory.getLogger ( CommandPart.class );
    
    private CommandGroup commandGroup;

    @Focus
    public void SetFocusToCommandText () {
        
        commandGroup.setFocus ();
        
    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {
        
        LOG.info ( "createGui:" );

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, false ) );
        context.set ( IContextKey.KEY_PART_COLS, cols );
        context.set ( IContextKey.KEY_PART_GROUP_COLS, cols ); // all groups have a width of 1 column

        // collect groups
        commandGroup = ContextInjectionFactory.make ( CommandGroup.class, context );

    }

}