package de.jungierek.grblrunner.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.ICommandID;
import de.jungierek.grblrunner.tools.PartTools;

public class GcodeCoordinateCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeCoordinateCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, PartTools partTools, @Optional @Named(ICommandID.SET_COORDINATE_SYSTEM_PARAMETER) String systemNo ) {

        LOG.debug ( "execute: systemNo=" + systemNo );
        
        int n = partTools.parseInteger ( systemNo, 1 );
        if ( n > 0 && n < 7 ) { // G54 .. G59
            gcodeService.sendCommandSuppressInTerminal ( "G" + (53 + n) );
        }


    }

}
