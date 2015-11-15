package de.jungierek.grblrunner.addons;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModel;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodeResponse;
import de.jungierek.grblrunner.service.serial.ISerialService;
import de.jungierek.grblrunner.tools.IConstants;
import de.jungierek.grblrunner.tools.IEvents;
import de.jungierek.grblrunner.tools.IPreferences;

@SuppressWarnings("restriction")
public class StartupAddon {

    private static final Logger LOG = LoggerFactory.getLogger ( StartupAddon.class );
    
    @Inject
    private IEventBroker eventBroker;
    
    @Inject
    private ISerialService serial;

    private MWindow window;

    /*
     * Notizen: IApplicationContext enthält startup Parameter via getArguments()
     */
    
    private EventHandler addHandler = new EventHandler () {

        @Override
        public void handleEvent ( Event event ) {

            if ( !(event.getProperty ( UIEvents.EventTags.ELEMENT ) instanceof MWindow) ) return;

            if ( !UIEvents.isCREATE ( event ) ) return;

            LOG.debug ( "handleEvent: event=" + event );

        }
    };

    @Inject
    public StartupAddon () {

        LOG.debug ( "StartupAddon: Constructor called" );

    }

    @PostConstruct
    public void processAddon ( ISerialService serial, EModelService modelService, MApplication application ) {

        LOG.debug ( "processAddon: detect serial ports" );

        // eventBroker.subscribe ( UIEvents.Window.TOPIC_ALL, addHandler );
        eventBroker.subscribe ( UIEvents.TrimmedWindow.TOPIC_ALL, addHandler );

        serial.detectSerialPortsAsync ();
        
        window = (MWindow) modelService.find ( IConstants.MAIN_WINDOW_ID, application );
        LOG.debug ( "processAddon: window=" + window );

    }


    @Inject
    @Optional
    public void applicationStopping ( @UIEventTopic(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED) Object event ) {

        LOG.debug ( "applicationStopping: event=" + event );

        // serial.close ();

    }

    @Inject
    @Optional
    public void applicationStarted ( @UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Object event ) {

        LOG.debug ( "applicationStarted: event=" + event );

        if ( window != null ) window.setLabel ( IPreferences.APPLICATION_TITILE );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvents.EVENT_GCODE_GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        String grblVersion = line.substring ( 0, line.indexOf ( '[' ) - 1 );
        if ( window != null ) window.setLabel ( IPreferences.APPLICATION_TITILE + " " + serial.getPortName () + " " + grblVersion );

    }

    @Inject
    @Optional
    public void serialDisconnectNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_DISCONNECTED) String port ) {

        LOG.trace ( "serialEventNotified:" );

        if ( window != null ) window.setLabel ( IPreferences.APPLICATION_TITILE );

    }

    @Inject
    @Optional
    public void gcodeEventNotified ( @UIEventTopic("TOPIC_GCODE/*") Object data ) {

        if ( data != null && data instanceof IGcodeResponse ) {} // noop
        else if ( data != null && data instanceof EGrblState ) {} // noop
        else {
            LOG.trace ( "gcodeEventNotified: data=" + data );
            eventBroker.send ( UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID );
        }

    }

    @Inject
    @Optional
    public void serialEventNotified ( @UIEventTopic("TOPIC_SERIAL/*") Object data ) {

        LOG.trace ( "serialEventNotified: data=" + data );
        eventBroker.send ( UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID );

    }

    @Inject
    @Optional
    public void serialPortsDetectedNotified ( @UIEventTopic(IEvents.EVENT_SERIAL_PORTS_DETECTED) String [] portNames ) {

        LOG.trace ( "serialPortsDetectedNotified:" );

        String ports = null;
        for ( String port : portNames ) {
            if ( ports == null ) ports = port;
            else ports += "," + port;
        }
        // the only one info about ports
        LOG.info ( "serialPortsDetectedNotified: ports=" + ports );

    }

    @SuppressWarnings("unused")
    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.EVENT_GCODE_PLAYER_LOADED) String fileName, IGcodeModel gcodeModel ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        if ( IPreferences.DUMP_PARSED_GCODE_LINE && gcodeModel != null ) {

            gcodeModel.visit ( new IGcodeModelVisitor () {
                @Override
                public void visit ( IGcodeLine gcodeLine ) {
                    LOG.info ( "playerLoadedNotified: line=" + gcodeLine.getLine () );
                    LOG.info ( "playerLoadedNotified:          gcodeLine=" + gcodeLine );
                }
            } );

        }

    }

}
