package de.jungierek.grblrunner.service.gcode.impl;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;

import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class ServiceContextFunction extends ContextFunction {
    
    @Override
    public Object compute ( IEclipseContext context, String contextKey ) {
        
        //System.out.println ( "gcode context function called contextKey=" + contextKey );

        Object result = null;
        
        if ( IGcodeService.class.getName ().equals ( contextKey ) ) {
            result = createGcodeService ( context );
        }
        else if ( IGcodeModel.class.getName ().equals ( contextKey ) ) {
            result = createModelService ( context );
        }

        return result;

    }

    private IGcodeService createGcodeService ( IEclipseContext context ) {
        IGcodeService result = ContextInjectionFactory.make ( GcodeServiceImpl.class, context );
        context.get ( MApplication.class ).getContext ().set ( IGcodeService.class, result );
        return result;
    }

    private IGcodeModel createModelService ( IEclipseContext context ) {
        IGcodeModel result = ContextInjectionFactory.make ( GcodeModelImpl.class, context );
        context.get ( MApplication.class ).getContext ().set ( IGcodeModel.class, result );
        return result;
    }

}
