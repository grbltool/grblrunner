package de.jungierek.grblrunner.part;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeViewToolControl {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewToolControl.class );

    @Inject
    private EPartService partService;

    @Inject
    private MPart part;

    private CCombo cCombo;
	
	@PostConstruct
    public void createGui ( Composite parent ) {
	    
        LOG.debug ( "createGui:" );

        Composite composite = new Composite ( parent, SWT.NONE );
        composite.setLayout ( new GridLayout ( 1, true ) );

        // cCombo = new CCombo ( composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER | SWT.CENTER );
        cCombo = new CCombo ( composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.CENTER );
        cCombo.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        setComboList ();

        cCombo.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent evt ) {
                ((GcodeViewPart) part.getObject ()).getGcodeViewGroup ().setOverlayGcodeProgram ( getSelectedProgram ( cCombo.getText () ) );
            }

        } );

	}

    private IGcodeProgram getSelectedProgram ( String text ) {

        LOG.debug ( "getSelectedProgram: text=" + text );

        Collection<MPart> parts = partService.getParts ();
        for ( MPart part : parts ) {
            if ( isOverlayPart ( part ) ) {
                if ( text.equals ( part.getLabel () ) ) {
                    final IEclipseContext context = part.getContext ();
                    if ( context != null ) return context.get ( IGcodeProgram.class );
                }
            }
        }

        // if part is not instantiated and therefore gcode program is not loaded, then we are here
        return null;

    }

    private void setComboList () {

        int index = cCombo.getSelectionIndex ();
        if ( index < 0 ) index = 0; // default selection is "NO OVERLAY"
        String text = cCombo.getText ();

        ArrayList<String> itemList = new ArrayList<> ( 10 );
        itemList.add ( IConstant.NO_OVERLAY );

        Collection<MPart> parts = partService.getParts ();
        for ( MPart part : parts ) {
            if ( isOverlayPart ( part ) ) {
                if ( part.getContext () != null ) { // part is instantiated
                    itemList.add ( part.getLabel () );
                }
            }
        }

        String [] items = itemList.toArray ( new String [0] );
        cCombo.setItems ( items );
        if ( index > 0 ) {
            int newIndex = 0;
            for ( int i = 1; i < items.length; i++ ) {
                if ( text.equals ( items[i] ) ) {
                    newIndex = i;
                    break;
                }
            }
            index = newIndex;
        }
        cCombo.select ( index );
        ((GcodeViewPart) part.getObject ()).getGcodeViewGroup ().setOverlayGcodeProgram ( getSelectedProgram ( cCombo.getText () ) );

    }

    private boolean isOverlayPart ( MPart part ) {

        final String id = part.getElementId ();
        return IConstant.EDITOR_PARTDESCRIPTOR_ID.equals ( id ) || IConstant.MACRO_PARTDESCRIPTOR_ID.equals ( id );

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        setComboList ();

    }

    @Inject
    @Optional
    public void macroGeneratedNotified ( @UIEventTopic(IEvent.GCODE_MACRO_GENERATED) Object dummy ) {

        LOG.debug ( "macroGneratedNotified:" );

        setComboList ();

    }

    @Inject
    @Optional
    public void gcodeClosedNotified ( @UIEventTopic(IEvent.GCODE_CLOSED) Object dummy ) {

        LOG.debug ( "gcodeClosedNotified:" );

        setComboList ();

    }

}