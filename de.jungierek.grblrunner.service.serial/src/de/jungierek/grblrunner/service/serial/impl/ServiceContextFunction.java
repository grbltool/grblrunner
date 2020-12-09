package de.jungierek.grblrunner.service.serial.impl;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.serial.ISerialService;

public class ServiceContextFunction extends ContextFunction {
    
    private static final Logger LOG = LoggerFactory.getLogger ( ServiceContextFunction.class );

    @Override
    public Object compute ( IEclipseContext context, String contextKey ) {
        
        LOG.debug ( "serial context function called contextKey=" + contextKey );
        
        Object result = null;
        
        if ( ISerialService.class.getName ().equals ( contextKey ) ) {
            result = createSerialService ( context );
        }

        return result;
        
    }

    private ISerialService createSerialService ( IEclipseContext context ) {

        ISerialService result;

        final boolean useRXTX = false;
        if ( useRXTX ) {
            LOG.info ( "Using RXTXSerial" );
            result = ContextInjectionFactory.make ( RXTXSerialServiceImpl.class, context );
        }
        else {
            LOG.info ( "Using JSerial" );
            result = ContextInjectionFactory.make ( JSerialServiceImpl.class, context );
        }
        context.get ( MApplication.class ).getContext ().set ( ISerialService.class, result );

        return result;

    }

}
