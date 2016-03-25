 
package de.jungierek.grblrunner.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;

public class GcodeMarcoHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeMarcoHandler.class );

    @Inject
    private EPartService partService;

    @Inject
    private EModelService modelService;

    public static class MacroDescription {

        final public String listItem;
        final public String contextValue;

        public MacroDescription ( String listItem, String contextValue ) {

            this.listItem = listItem;
            this.contextValue = contextValue;

        }

    }

    // @formatter:off
    private final static MacroDescription [] MACROS = new MacroDescription [] {
        new MacroDescription ( "Hobbed Bolt", "hobbed_bolt" ),
        new MacroDescription ( "Pocket", "pocket" ),
    };
    // @formatter:on
    
    private MacroDescription selectedMacro;

    @Execute
    public void execute ( IEclipseContext context, MApplication application, Shell shell ) {

        LOG.debug ( "execute:" );

        MacroPickerDialog dialog = new MacroPickerDialog ( shell );
        int open = dialog.open ();
        if ( open == 0 ) { // 1: cancel

            MPart part = partService.createPart ( IConstant.MACRO_PARTDESCRIPTOR_ID );

            MPartStack partStack = (MPartStack) modelService.find ( IConstant.EDITOR_PARTSTACK_ID, application );
            partStack.getChildren ().add ( part );
            // HACK
            application.getContext ().set ( IContextKey.MACRO_TYPE, selectedMacro.contextValue );

            // it instanciates also the part object class
            part.setLabel ( "Macro: " + selectedMacro.listItem );
            partService.showPart ( part, PartState.ACTIVATE );

        }


	}
	
	
	@CanExecute
	public boolean canExecute() {

        // new macros are always possible
		return true;

	}

    private class MacroPickerDialog extends Dialog {
        
        public MacroPickerDialog ( Shell shell ) {

            super ( shell );

        }

        @Override
        protected Control createDialogArea ( Composite parent ) {

            final Composite dialogArea = (Composite) super.createDialogArea ( parent );
            
            List list = new List ( dialogArea, SWT.BORDER );
            list.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );

            for ( MacroDescription macro : MACROS ) {
                list.add ( macro.listItem );
            }

            list.addSelectionListener ( new SelectionAdapter () {

                @Override
                public void widgetSelected ( SelectionEvent evt ) {
                    selectedMacro = MACROS[list.getSelectionIndex ()];
                }

                @Override
                public void widgetDefaultSelected ( SelectionEvent evt ) {
                    selectedMacro = MACROS[list.getSelectionIndex ()];
                    okPressed ();
                }

            } );

            return dialogArea;

        }

    }
		
}