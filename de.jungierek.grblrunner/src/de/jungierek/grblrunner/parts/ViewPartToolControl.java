package de.jungierek.grblrunner.parts;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class ViewPartToolControl {

    private static final Logger LOG = LoggerFactory.getLogger ( ViewPartToolControl.class );

    @Inject
    private EPartService partService;

    private CCombo cCombo;
	
	@PostConstruct
    public void createGui ( Composite parent, MPart part ) {
	    
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
                ((ViewPart) part.getObject ()).getGcodeViewGroup ().setOverlayGcodeProgram ( getSelectedProgram ( cCombo.getText () ) );
            }

        } );

	}

    private IGcodeProgram getSelectedProgram ( String text ) {

        LOG.debug ( "getSelectedProgram: text=" + text );

        Collection<MPart> parts = partService.getParts ();
        for ( MPart part : parts ) {
            if ( IConstants.EDITOR_PARTDESCRIPTOR_ID.equals ( part.getElementId () ) ) {
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

        ArrayList<String> itemList = new ArrayList<String> ( 10 );
        itemList.add ( IConstants.NO_OVERLAY );

        Collection<MPart> parts = partService.getParts ();
        for ( MPart part : parts ) {
            if ( IConstants.EDITOR_PARTDESCRIPTOR_ID.equals ( part.getElementId () ) ) {
                if ( part.getContext () != null ) { // part is instantiated
                    itemList.add ( part.getLabel () );
                }
            }
        }

        String [] items = itemList.toArray ( new String [0] );
        cCombo.setItems ( items );
        if ( index > 0 ) {
            for ( int i = 1; i < items.length; i++ ) {
                if ( text.equals ( items[i] ) ) {
                    index = i;
                }

            }
        }
        cCombo.select ( index );

    }

    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.PLAYER_LOADED) String fileName ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        setComboList ();

    }

}