package de.jungierek.grblrunner.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class GcodePlayHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodePlayHandler.class );

    @Execute
    public void execute ( IGcodeService gcode ) {

        LOG.debug ( "execute:" );

        gcode.play ();

    }

    @CanExecute
    public boolean canExecute ( IGcodeService gcode, IGcodeModel model, ISerialService serial ) {

        LOG.debug ( "canExecute:" );
        // LOG.info ( "canExecute: isPLaying=" + gcode.isPlaying () + " isscanning=" + gcode.isScanning () );

        return serial.isOpen () && model.isGcodeProgramLoaded () && !gcode.isPlaying () && !gcode.isScanning ();

    }

}
