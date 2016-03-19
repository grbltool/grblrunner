package de.jungierek.grblrunner.handlers;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
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
import de.jungierek.grblrunner.constants.IPersistenceKeys;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;

public class GcodeLoadHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLoadHandler.class );

    @Inject
    private EPartService partService;

    @Inject
    private EModelService modelService;

    @Execute
    public void execute ( MApplication application, Shell shell, @Preference(nodePath = IConstants.PREFERENCE_NODE, value = IPreferenceKey.GCODE_PATH) String gcodePath ) {

        LOG.debug ( "execute:" );

        FileDialog dialog = new FileDialog ( shell, SWT.OPEN );
        dialog.setFilterExtensions ( IConstants.GCODE_FILE_EXTENSIONS );

        String filterPath = application.getPersistedState ().get ( IPersistenceKeys.KEY_GCODE_PATH );
        if ( filterPath == null ) filterPath = gcodePath;
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

    @CanExecute
    public boolean canExecute ( IGcodeService gcodeService, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram gcodeProgram ) {

        LOG.debug ( "canExecute: program=" + gcodeProgram + " isPLaying=" + gcodeService.isPlaying () + " isscanning=" + gcodeService.isAutolevelScan () );

        // return !gcodeService.isPlaying () && !gcodeService.isScanning ();

        // new gcodes are always possible
        return true;

    }

}
