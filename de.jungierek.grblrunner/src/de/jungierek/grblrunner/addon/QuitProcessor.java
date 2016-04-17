package de.jungierek.grblrunner.addon;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.serial.ISerialService;

public class QuitProcessor {

    private static final Logger LOG = LoggerFactory.getLogger ( QuitProcessor.class );

    private final MWindow window;
    private final IEventBroker eventBroker;

    @Inject
    public QuitProcessor ( @Named(IConstant.MAIN_WINDOW_ID) MWindow window, IEventBroker eventBroker ) {

        LOG.debug ( "QuitProcessor: window=" + window );

        this.window = window;
        this.eventBroker = eventBroker;

    }

    // Quelle: https://www.eclipse.org/forums/index.php/t/369989/

    @Execute
    void installIntoContext () {

        LOG.debug ( "installIntoContext:" );

        eventBroker.subscribe ( UIEvents.Context.TOPIC_CONTEXT, new EventHandler () {

            @Override
            public void handleEvent ( Event event ) {

                LOG.trace ( "installIntoContext: event=" + event );

                if ( UIEvents.isSET ( event ) ) {

                    if ( window.equals ( event.getProperty ( "ChangedElement" ) ) && window.getContext () != null ) {

                        // use RunAndTrack to get notified after the IWindowCloseHanlder was changed in the IEclipseContext
                        window.getContext ().runAndTrack ( new RunAndTrack () {

                            private final IWindowCloseHandler quitHandler = new QuitHandler ();

                            @Override
                            public boolean changed ( IEclipseContext context ) {
                                Object value = context.get ( IWindowCloseHandler.class ); // access the context value to be reevaluated on every future change of the value

                                if ( !quitHandler.equals ( value ) ) { // prevents endless loop
                                    LOG.debug ( "installIntoContext: set quit handler event=" + event );
                                    context.set ( IWindowCloseHandler.class, quitHandler );
                                }

                                return true; // true keeps tracking and the quitHandler as the only opportunity
                            }

                        } );
                    }
                }
            }

        } );
    }

    private class QuitHandler implements IWindowCloseHandler {

        @Override
        public boolean close ( MWindow window ) {

            LOG.debug ( "close:" );

            IGcodeService gcodeService = window.getContext ().get ( IGcodeService.class );
            if ( gcodeService.isPlaying () || gcodeService.isAutolevelScan () || !gcodeService.isGrblIdle () ) {
                LOG.warn ( "close: job is runnung" );
                eventBroker.post ( IEvent.MESSAGE_ERROR, "Closing application is not possible! Job is running!" );
                return false;
            }

            ISerialService serial = window.getContext ().get ( ISerialService.class );
            serial.close ();

            return true; // false -> dont close window

        }

    }
}