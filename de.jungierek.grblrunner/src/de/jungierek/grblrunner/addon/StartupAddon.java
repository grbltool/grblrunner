package de.jungierek.grblrunner.addon;

import java.io.File;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.part.GcodeEditorPart;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.serial.ISerialService;

@SuppressWarnings("restriction")
public class StartupAddon {

    private static final Logger LOG = LoggerFactory.getLogger ( StartupAddon.class );
    
    @Inject
    private IEventBroker eventBroker;
    
    @Inject
    private ISerialService serial;

    private MWindow window;

    private IGcodeProgram gcodeProgram;

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

    private String grblVersion;

    @Inject
    public StartupAddon () {

        LOG.debug ( "StartupAddon: Constructor called" );

        // HACK set US loacale for number conversion
        Locale.setDefault ( Locale.US );

    }

    @PostConstruct
    public void processAddon ( ISerialService serial, EModelService modelService, MApplication application ) {

        LOG.debug ( "processAddon: detect serial ports" );

        // eventBroker.subscribe ( UIEvents.TrimmedWindow.TOPIC_ALL, addHandler );

        serial.detectSerialPortsAsync ();
        
        window = (MWindow) modelService.find ( IConstant.MAIN_WINDOW_ID, application );
        LOG.debug ( "processAddon: window=" + window );

        setWindowLabel ();

    }


    @Inject
    public void setGcodeProgram ( @Optional @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.debug ( "setGcodeProgram: program=" + program );

        gcodeProgram = program;
        setWindowLabel ();

    }

    private void setWindowLabel () {
    
        if ( window == null ) return;

        String msg = IConstant.APPLICATION_TITILE;
    
        if ( serial.isOpen () ) {
            msg += " on " + serial.getPortName ();
            msg += "@" + serial.getBaudrate () + " Baud";
            if ( grblVersion != null ) msg += " - " + grblVersion;
        }
    
        if ( gcodeProgram != null ) {
            File gcodeProgramFile = gcodeProgram.getGcodeProgramFile ();
            if ( gcodeProgramFile != null ) msg += " - " + gcodeProgramFile.getPath ();
        }
    
        window.setLabel ( msg );
    
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

        setWindowLabel ();

    }

    // @Inject
    public void partSwitched ( @Named(IServiceConstants.ACTIVE_PART) MPart part ) {

        LOG.info ( "partSwitched: part=" + part );
        if ( part != null ) {
            Object object = part.getObject ();
            if ( object != null ) {
                LOG.info ( "partSwitched: obj=" + object );
                if ( object instanceof GcodeEditorPart ) {
                    LOG.info ( "partSwitched: prog=" + part.getContext ().get ( IGcodeProgram.class ) );
                }
            }
        }

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvent.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        grblVersion = line.substring ( 0, line.indexOf ( '[' ) - 1 );
        setWindowLabel ();

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        setWindowLabel ();

    }

    @Inject
    @Optional
    public void serialDisconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String port ) {

        LOG.trace ( "serialEventNotified:" );

        setWindowLabel ();

    }

    private void updateToolbarState () {

        eventBroker.send ( UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvent.GRBL_RESTARTED) Object data ) {

        LOG.trace ( "grblRestartedNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void gcodeEventNotified ( @UIEventTopic(IEvent.GCODE_ALL) Object data ) {

        LOG.trace ( "gcodeEventNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void playerEventNotified ( @UIEventTopic(IEvent.PLAYER_ALL) Object data ) {

        LOG.trace ( "playerEventNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void autolevelEventNotified ( @UIEventTopic(IEvent.AUTOLEVEL_ALL) Object data ) {

        LOG.trace ( "autolevelEventNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void serialEventNotified ( @UIEventTopic(IEvent.SERIAL_ALL) Object data ) {

        LOG.trace ( "serialEventNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void updateEventNotified ( @UIEventTopic(IEvent.UPDATE_ALL) Object data ) {

        LOG.trace ( "updateEventNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void cameraEventNotified ( @UIEventTopic(IEvent.CAMERA_ALL) Object data ) {

        LOG.trace ( "cameraEventNotified: data=" + data );
        updateToolbarState ();

    }

    @Inject
    @Optional
    public void serialPortsDetectedNotified ( @UIEventTopic(IEvent.SERIAL_PORTS_DETECTED) String [] portNames ) {

        LOG.trace ( "serialPortsDetectedNotified:" );

        String ports = null;
        for ( String port : portNames ) {
            if ( ports == null ) ports = port;
            else ports += "," + port;
        }

        // the only one info about ports
        LOG.info ( "serialPortsDetectedNotified: " + (ports == null ? "no ports detected" : "ports=" + ports) );

    }

}
