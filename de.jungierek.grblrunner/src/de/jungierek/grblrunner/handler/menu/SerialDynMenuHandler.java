package de.jungierek.grblrunner.handler.menu;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tool.Toolbox;

public class SerialDynMenuHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger ( SerialDynMenuHandler.class );

    @Inject
    private MApplication application;
    
    @Inject
    private EModelService modelService;

    @Inject
    private ISerialService serial;
    
    @Inject
    private Toolbox toolbox;

    @AboutToShow
    public void aboutToShow ( List<MMenuElement> items ) {
    
        LOG.debug ( "aboutToShow:" );

        String [] ports = serial.getCachedSerialPorts ();
    
        if ( ports == null ) {

            MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
            item.setLabel ( "will be detected ..." );
            items.add ( item );

        }
        else {

            String selectedPortName = serial.getPortName ();
            for ( String port : ports ) {
                LOG.debug ( "port=" + port );
                toolbox.addMenuItemTo ( items, port.equals ( selectedPortName ), ICommandId.SERIAL_SELECT_PORT, ICommandId.SERIAL_SELECT_PORT_PARAMETER, port );
            }
    
        }
    
    }

    @AboutToHide
    public void aboutToHide () {

    }

}
