package de.jungierek.grblrunner.service.webcam.impl;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.webcam.IWebcamService;

public class ServiceContextFunction extends ContextFunction {
    
    private static final Logger LOG = LoggerFactory.getLogger ( ServiceContextFunction.class );

    @Override
    public Object compute ( IEclipseContext context, String contextKey ) {
        
        LOG.debug ( "webcam context function called contextKey=" + contextKey );
        
        Object result = null;
        
        if ( IWebcamService.class.getName ().equals ( contextKey ) ) {
            result = createWebcamService ( context );
        }

        return result;
        
    }

    private IWebcamService createWebcamService ( IEclipseContext context ) {
        IWebcamService result = ContextInjectionFactory.make ( WebcamServiceImpl.class, context );
        context.get ( MApplication.class ).getContext ().set ( IWebcamService.class, result );
        return result;
    }

}
