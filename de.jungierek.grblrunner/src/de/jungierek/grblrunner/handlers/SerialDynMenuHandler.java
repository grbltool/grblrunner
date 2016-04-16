package de.jungierek.grblrunner.handlers;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.ICommandId;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.CommandTools;

public class SerialDynMenuHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger ( SerialDynMenuHandler.class );

    @Inject
    private MApplication application;
    
    @Inject
    private EModelService modelService;

    @Inject
    private ISerialService serial;
    
    @Inject
    private CommandTools commandTool;

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

            String portName = serial.getPortName ();
    
            for ( String port : ports ) {
    
                LOG.debug ( "port=" + port );
    
                MParameter parameter = MCommandsFactory.INSTANCE.createParameter ();
                parameter.setElementId ( ICommandId.SERIAL_SELECT_PORT_PARAMETER + port );
                parameter.setName ( ICommandId.SERIAL_SELECT_PORT_PARAMETER ); // this is the importend "id"
                parameter.setValue ( port );
                LOG.trace ( "parameter=" + parameter );
    
                MCommand command = commandTool.findCommand ( ICommandId.SERIAL_SELECT_PORT );
                LOG.trace ( "command=" + command );
    
                MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
                item.setLabel ( port );
                item.setType ( ItemType.RADIO );
                item.setCommand ( command );
                item.getParameters ().add ( parameter );
                if ( port.equals ( portName ) ) {
                    item.setSelected ( true );
                }
                items.add ( item );
    
            }
    
        }
    
    }

    @AboutToHide
    public void aboutToHide () {

    }

}
