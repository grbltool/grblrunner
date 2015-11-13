package de.jungierek.grblrunner.handlers;

import java.io.File;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.IPersistenceKeys;
import de.jungierek.grblrunner.tools.IPreferences;

public class GcodeLoadHandler {

    private static final String INITIAL_GCODE_PATH = "C:\\Users\\Andreas\\Documents\\eagle";

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLoadHandler.class );

    @Execute
    public void execute ( MApplication application, Shell shell, @Named(IServiceConstants.ACTIVE_PART) MPart part, IGcodeService gcode ) {

        LOG.debug ( "execute:" );

        FileDialog dialog = new FileDialog ( shell, SWT.OPEN );

        // TODO file extension to preference
        dialog.setFilterExtensions ( IPreferences.GCODE_FILE_EXTENSION );

        // TODO_PREF base file path to preference
        String filterPath = application.getPersistedState ().get ( IPersistenceKeys.KEY_GCODE_PATH );
        if ( filterPath == null ) filterPath = INITIAL_GCODE_PATH;
        dialog.setFilterPath ( filterPath );

        String result = dialog.open ();
        if ( result != null ) {
            gcode.load ( new File ( result ) );
            final int start = result.lastIndexOf ( File.separator ) + 1;
            final int end = result.lastIndexOf ( '.' );
            String name = result.substring ( start, end );
            part.setLabel ( name );
            application.getPersistedState ().put ( IPersistenceKeys.KEY_GCODE_PATH, filterPath );
        }

    }

    @CanExecute
    public boolean canExecute ( IGcodeService gcode, IGcodeModel model, ISerialService serial ) {

        LOG.trace ( "canExecute:" );

        return serial.isOpen () && !gcode.isPlaying () && !gcode.isScanning ();

    }

}
