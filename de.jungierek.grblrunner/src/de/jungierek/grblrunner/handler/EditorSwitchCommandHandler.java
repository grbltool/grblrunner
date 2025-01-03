package de.jungierek.grblrunner.handler;

import java.util.List;

import jakarta.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class EditorSwitchCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( EditorSwitchCommandHandler.class );

    @Execute
    public void execute ( ParameterizedCommand command, MPart part, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram activeProgram, EPartService partService,
            EModelService modelService, ESelectionService selectionService, MApplication application ) {

        LOG.debug ( "execute: cmd=" + command.getId () + " part=" + part.getElementId () );

        MPartStack partStack = (MPartStack) modelService.find ( IConstant.EDITOR_PARTSTACK_ID, application );
        List<MStackElement> children = partStack.getChildren ();
        MPart [] editors = children.toArray ( new MPart [children.size ()] );

        int found = -1;
        for ( int i = 0; i < editors.length; i++ ) {

            final MPart editor = editors[i];
            IEclipseContext context = editor.getContext ();
            if ( context == null ) {
                partService.activate ( editor, false );
                context = editor.getContext ();
            }
            IGcodeProgram program = context.get ( IGcodeProgram.class );
            if ( program == activeProgram ) found = i;

        }

        if ( found > -1 ) {

            String commandId = command.getId ();
            String type = commandId.substring ( commandId.lastIndexOf ( '.' ) + 1 );
            switch ( type ) {
                case "next":
                    if ( found < editors.length - 1 ) found++;
                    break;
                case "previous":
                    if ( found > 0 ) found--;
                    break;

                default:
                    break;
            }

            partService.showPart ( editors[found], PartState.ACTIVATE );
            // selectionService.setSelection ( editors[found].getContext ().get ( IGcodeProgram.class ) );

        }

    }

    @CanExecute
    public boolean canExecute ( ISerialService serial, IGcodeService gcodeService ) {

        LOG.trace ( "canExecute:" );

        return true;

    }

}
