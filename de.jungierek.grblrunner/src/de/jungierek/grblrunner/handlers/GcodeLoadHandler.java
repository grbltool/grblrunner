package de.jungierek.grblrunner.handlers;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tools.IPersistenceKeys;

public class GcodeLoadHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLoadHandler.class );

    @Inject
    private EPartService partService;

    @Inject
    private EModelService modelService;

    @Execute
    public void execute ( MApplication application, Shell shell ) {

        LOG.debug ( "execute:" );

        FileDialog dialog = new FileDialog ( shell, SWT.OPEN );
        dialog.setFilterExtensions ( IPreferences.GCODE_FILE_EXTENSIONS );

        String filterPath = application.getPersistedState ().get ( IPersistenceKeys.KEY_GCODE_PATH );
        if ( filterPath == null ) filterPath = IPreferences.INITIAL_GCODE_PATH;
        dialog.setFilterPath ( filterPath );

        String result = dialog.open ();
        if ( result != null ) {
            
            MPart part = partService.createPart ( IConstants.EDITOR_PARTDESCRIPTOR_ID );

            MPartStack partStack = (MPartStack) modelService.find ( IConstants.EDITOR_PARTSTACK_ID, application );
            partStack.getChildren ().add ( part );

            // it instanciates also the part object class
            partService.showPart ( part, PartState.ACTIVATE );

            IGcodeProgram gcodeProgram = part.getContext ().get ( IGcodeProgram.class );

            gcodeProgram.loadGcodeProgram ( new File ( result ) );
            part.setLabel ( gcodeProgram.getGcodeProgramName () );

            application.getPersistedState ().put ( IPersistenceKeys.KEY_GCODE_PATH, filterPath );

        }

    }

    // @Execute
    public void execute2 ( MApplication application, Shell shell, @Named(IServiceConstants.ACTIVE_PART) MPart part,
            @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "execute:" );

        FileDialog dialog = new FileDialog ( shell, SWT.OPEN );

        // TODO file extension to preference
        dialog.setFilterExtensions ( IPreferences.GCODE_FILE_EXTENSIONS );

        // TODO_PREF base file path to preference
        String filterPath = application.getPersistedState ().get ( IPersistenceKeys.KEY_GCODE_PATH );
        if ( filterPath == null ) filterPath = IPreferences.INITIAL_GCODE_PATH;
        dialog.setFilterPath ( filterPath );

        String result = dialog.open ();
        if ( result != null ) {
            gcodeProgram.loadGcodeProgram ( new File ( result ) );
            final int start = result.lastIndexOf ( File.separator ) + 1;
            final int end = result.lastIndexOf ( '.' );
            String name = result.substring ( start, end );
            part.setLabel ( name );
            application.getPersistedState ().put ( IPersistenceKeys.KEY_GCODE_PATH, filterPath );
        }

    }

    @CanExecute
    public boolean canExecute ( IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isScanning () );

        return !gcodeService.isPlaying () && !gcodeService.isScanning ();

    }

}
