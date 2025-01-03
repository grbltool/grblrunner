package de.jungierek.grblrunner.part;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

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

import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tool.GuiFactory.IntegerVerifyer;
import de.jungierek.grblrunner.tool.Toolbox;

public class GcodeEditorToolControl {
    
    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditorToolControl.class );

    @Inject
    private IGcodeProgram gcodeProgram;
    
    @Inject
    private Toolbox toolbox;
    
    @Inject
    private IEventBroker eventBroker; 

    private Text gcodeRotationText;

    private boolean ignoreRotationTextModifyListener = false;

    @PostConstruct
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
                gcodeProgram.rotate ( toolbox.parseIntegerField ( gcodeRotationText, 0 ) );
                gcodeProgram.prepareAutolevelScan ();

                eventBroker.send ( IEvent.REDRAW, null );

            }

        } );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void programLoadedNotified ( @UIEventTopic(IEvent.GCODE_PROGRAM_LOADED) String fileName ) {

        LOG.debug ( "programLoadedNotified: fileName=" + fileName );

        ignoreRotationTextModifyListener = true;
        gcodeRotationText.setText ( String.format ( "%.0f", gcodeProgram.getRotationAngle () ) );
        ignoreRotationTextModifyListener = false;

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvent.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) Object dummy ) {

        LOG.debug ( "scanStopNotified:" );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvent.AUTOLEVEL_STOP) Object dummy ) {

        LOG.debug ( "scanStopNotified:" );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        gcodeRotationText.setEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        gcodeRotationText.setEnabled ( true && gcodeProgram.isLoaded () );

    }

}
