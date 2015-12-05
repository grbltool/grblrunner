package de.jungierek.grblrunner.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IEvents;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.GuiFactory.IntegerVerifyer;
import de.jungierek.grblrunner.tools.PartTools;

public class GcodeEditorToolControl {
    
    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditorToolControl.class );

    @Inject
    private IGcodeProgram gcodeProgram;
    
    @Inject
    private PartTools partTools;
    
    @Inject
    private IEventBroker eventBroker; 

    private Text gcodeRotationText;

    private boolean ignoreRotationTextModifyListener = false;

    @PostConstruct
    public void createGuiEmpty ( Composite parent ) {

        createGui ( parent );

    }

    public void createGui ( Composite parent ) {

        Composite composite = new Composite ( parent, SWT.NONE );
        composite.setLayout ( new GridLayout ( 1, true ) );

        gcodeRotationText = new Text ( composite, SWT.SINGLE | SWT.RIGHT );
        gcodeRotationText.setText ( "   0" );
        gcodeRotationText.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        gcodeRotationText.setEnabled ( false );
        gcodeRotationText.addVerifyListener ( new IntegerVerifyer ( -90, +90 ) );

        gcodeRotationText.addModifyListener ( new ModifyListener () {

            @Override
            public void modifyText ( ModifyEvent evt ) {

                if ( ignoreRotationTextModifyListener ) return;

                LOG.debug ( "modifyText: gcodeRotationText" );
                gcodeProgram.rotate ( partTools.parseIntegerField ( gcodeRotationText, 0 ) );
                gcodeProgram.prepareAutolevelScan ();

                eventBroker.send ( IEvents.REDRAW, null );

            }

        } );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvents.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvents.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void playerLoadedNotified ( @UIEventTopic(IEvents.PLAYER_LOADED) String fileName ) {

        LOG.debug ( "playerLoadedNotified: fileName=" + fileName );

        ignoreRotationTextModifyListener = true;
        gcodeRotationText.setText ( String.format ( "%.0f", gcodeProgram.getRotationAngle () ) );
        ignoreRotationTextModifyListener = false;

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvents.GRBL_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvents.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvents.AUTOLEVEL_START) Object dummy ) {

        LOG.debug ( "scanStopNotified:" );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvents.AUTOLEVEL_STOP) Object dummy ) {

        LOG.debug ( "scanStopNotified:" );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvents.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvents.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

}
