package de.jungierek.grblrunner.service.gcode.impl;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class ServiceContextFunction extends ContextFunction {
    
    private static final Logger LOG = LoggerFactory.getLogger ( ServiceContextFunction.class );

    @Override
    public Object compute ( IEclipseContext context, String contextKey ) {
        
        LOG.debug ( "compute: context=" + context + " contextKey=" + contextKey );

        //System.out.println ( "gcode context function called contextKey=" + contextKey );

        Object result = null;
        
        if ( IGcodeService.class.getName ().equals ( contextKey ) ) {
            result = createGcodeService ( context );
        }

        return result;

    }

    private IGcodeService createGcodeService ( IEclipseContext context ) {

        IGcodeService result = ContextInjectionFactory.make ( GcodeServiceImpl.class, context );
        context.get ( MApplication.class ).getContext ().set ( IGcodeService.class, result );

        return result;

    }

}
