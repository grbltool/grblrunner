package de.jungierek.grblrunner.handler;

import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tool.Toolbox;

public class GcodeCoordinateCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeCoordinateCommandHandler.class );

    @Execute
    public void execute ( IGcodeService gcodeService, Toolbox toolbox, @Optional @Named(ICommandId.SET_COORDINATE_SYSTEM_PARAMETER) String systemNo ) {

        LOG.debug ( "execute: systemNo=" + systemNo );
        
        int n = toolbox.parseInteger ( systemNo, 1 );
        if ( n > 0 && n < 7 ) { // G54 .. G59
            final String line = "G" + (53 + n);
            try {
                gcodeService.sendCommandSuppressInTerminal ( line );
            }
            catch ( InterruptedException exc ) {
                LOG.info ( "execute: interrupted exception in line=" + line );
            }
        }


    }

}
