package de.jungierek.grblrunner.part.group;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Scale;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tool.GuiFactory;

public class ControlOverrideGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( ControlOverrideGroup.class );

    private static final String GROUP_NAME = "Override";

    @Inject
    private IGcodeService gcodeService;

    private Button feedOverrideIncrease1PercentButton;
    private Button feedOverrideIncrease10PercentButton;
    private Button feedOverride100PercentButton;
    private Button feedOverrideDecrease10PercentButton;
    private Button feedOverrideDecrease1PercentButton;

    private Button spindleOverrideIncrease1PercentButton;
    private Button spindleOverrideIncrease10PercentButton;
    private Button spindleOverride100PercentButton;
    private Button spindleOverrideDecrease10PercentButton;
    private Button spindleOverrideDecrease1PercentButton;
    private Button toggleSpindleStopButton;

    private Scale rapidOverrideScale;

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent );

        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );

        group.setLayout ( new GridLayout ( 9, true ) );

        // line 1 Labels
        GuiFactory.createHiddenLabel ( group, 1 );
        GuiFactory.createHeadingLabel ( group, "-1%", 1, false );
        GuiFactory.createHeadingLabel ( group, "-10%", 1, false );
        GuiFactory.createHeadingLabel ( group, "100%", 1, false );
        GuiFactory.createHeadingLabel ( group, "+10%", 1, false );
        GuiFactory.createHeadingLabel ( group, "+1%", 1, false );
        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "Rapid", 1 );
        rapidOverrideScale = createScale ( group, 3, 3 );
        GuiFactory.createHeadingLabel ( group, "100%", 1, false );
        // GuiFactory.createHiddenLabel ( group, 1 );

        // line 2 feed
        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "Feed", 1 );
        feedOverrideDecrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.LEFT );
        feedOverrideDecrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.LEFT );
        feedOverride100PercentButton = GuiFactory.createPushButton ( group, " | " );
        feedOverrideIncrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.RIGHT );
        feedOverrideIncrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.RIGHT );
        GuiFactory.createHiddenLabel ( group, 1 );
        GuiFactory.createHeadingLabel ( group, "50%", 1, false );
        // GuiFactory.createHiddenLabel ( group, 1 );

        // line 3 spindle
        GuiFactory.createHeadingLabel ( group, SWT.RIGHT, "Spindle", 1 );
        spindleOverrideDecrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.LEFT );
        spindleOverrideDecrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.LEFT );
        spindleOverride100PercentButton = GuiFactory.createPushButton ( group, " | " );
        spindleOverrideIncrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.RIGHT );
        spindleOverrideIncrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.RIGHT );
        toggleSpindleStopButton = GuiFactory.createPushButton ( group, "" );
        toggleSpindleStopButton.setImage ( loadImage ( "/icons/suspend.gif" ) );
        // GuiFactory.createHiddenLabel ( group, 1 );
        GuiFactory.createHeadingLabel ( group, "25%", 1, false );
        // GuiFactory.createHiddenLabel ( group, 1 );

        feedOverrideIncrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideIncrease1Percent ();
                super.widgetSelected ( event );
            }
        } );

        feedOverrideIncrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideIncrease10Percent ();
                super.widgetSelected ( event );
            }
        } );

        feedOverride100PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverride100Percent ();
                super.widgetSelected ( event );
            }
        } );

        feedOverrideDecrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideDecrease10Percent ();
                super.widgetSelected ( event );
            }
        } );

        feedOverrideDecrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideDecrease1Percent ();
                super.widgetSelected ( event );
            }
        } );

        spindleOverrideIncrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideIncrease1Percent ();
                super.widgetSelected ( event );
            }
        } );

        spindleOverrideIncrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideIncrease10Percent ();
                super.widgetSelected ( event );
            }
        } );

        spindleOverride100PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverride100Percent ();
                super.widgetSelected ( event );
            }
        } );

        spindleOverrideDecrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideDecrease10Percent ();
                super.widgetSelected ( event );
            }
        } );

        spindleOverrideDecrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideDecrease1Percent ();
                super.widgetSelected ( event );
            }
        } );

        toggleSpindleStopButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendToggleSpindleStop ();
                super.widgetSelected ( event );
            }
        } );

        rapidOverrideScale.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                int selection = rapidOverrideScale.getSelection ();
                switch ( selection ) {
                    case 1:
                        gcodeService.sendRapidOverride100Percent ();
                        break;
                    case 2:
                        gcodeService.sendRapidOverride50Percent ();
                        break;
                    case 3:
                        gcodeService.sendRapidOverride25Percent ();
                        break;
                    default:
                        break;
                }
                // super.widgetSelected ( event );
            }
        } );

    }

    private Scale createScale ( Group group, final int steps, final int verticalSpan ) {

        Scale scale = new Scale ( group, SWT.VERTICAL );
        scale.setMinimum ( 1 );
        scale.setMaximum ( steps );
        scale.setIncrement ( 1 );
        scale.setPageIncrement ( 1 );
        GridData gd = new GridData ( SWT.CENTER, SWT.FILL, false, true );
        gd.verticalSpan = verticalSpan;
        gd.heightHint = 30;
        scale.setLayoutData ( gd );

        scale.setEnabled ( false );

        return scale;

    }

    private Image loadImage ( final String path ) {
    
        Bundle bundle = FrameworkUtil.getBundle ( getClass () );
        URL url = FileLocator.find ( bundle, new Path ( path ), null );
        ImageDescriptor imageDesc = ImageDescriptor.createFromURL ( url );
        Image image = imageDesc.createImage ();
    
        return image;
    
    }

    private void setControlsEnabled ( boolean enabled ) {

        feedOverrideIncrease1PercentButton.setEnabled ( enabled );
        feedOverrideIncrease10PercentButton.setEnabled ( enabled );
        feedOverride100PercentButton.setEnabled ( enabled );
        feedOverrideDecrease10PercentButton.setEnabled ( enabled );
        feedOverrideDecrease1PercentButton.setEnabled ( enabled );
        spindleOverrideIncrease1PercentButton.setEnabled ( enabled );
        spindleOverrideIncrease10PercentButton.setEnabled ( enabled );
        spindleOverride100PercentButton.setEnabled ( enabled );
        spindleOverrideDecrease10PercentButton.setEnabled ( enabled );
        spindleOverrideDecrease1PercentButton.setEnabled ( enabled );
        toggleSpindleStopButton.setEnabled ( enabled );
        rapidOverrideScale.setEnabled ( enabled );

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.trace ( "alarmNotified: line=" + line );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void grblRestartedNotified ( @UIEventTopic(IEvent.GRBL_RESTARTED) String line ) {

        LOG.trace ( "grblRestartedNotified: line=" + line );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void connectedNotified ( @UIEventTopic(IEvent.SERIAL_CONNECTED) String portName ) {

        LOG.trace ( "connectedNotified: portName=" + portName );

        setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String param ) {

        LOG.trace ( "connectedNotified: param=" + param );

        setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String fileName ) {

        LOG.trace ( "playerStartNotified: fileName=" + fileName );

        // setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void playerStopNotified ( @UIEventTopic(IEvent.PLAYER_STOP) String fileName ) {

        LOG.trace ( "playerStopNotified: fileName=" + fileName );

        // setControlsEnabled ( true );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) Object dummy ) {

        LOG.trace ( "scanStartNotified:" );

        // setControlsEnabled ( false );

    }

    @Inject
    @Optional
    public void scanStopNotified ( @UIEventTopic(IEvent.AUTOLEVEL_STOP) Object dummy ) {

        LOG.trace ( "scanStopNotified:" );

        // setControlsEnabled ( true );

    }

    // @PostConstruct
    public void createGui2 ( Composite parent, IEclipseContext context ) {
    
        LOG.debug ( "createGui: parent=" + parent );
    
        int groupCols = ((Integer) context.get ( IContextKey.PART_GROUP_COLS )).intValue ();
        int groupRows = ((Integer) context.get ( IContextKey.PART_GROUP_ROWS )).intValue ();
        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, groupRows, true );
    
        group.setLayout ( new GridLayout ( 10, true ) );
    
        // line 1
        GuiFactory.createHiddenLabel ( group, 1, false );
        GuiFactory.createHeadingLabel ( group, "Feed", 1, false );
        GuiFactory.createHiddenLabel ( group, 2, false );
        GuiFactory.createHeadingLabel ( group, "Spindle", 1, false );
        GuiFactory.createHiddenLabel ( group, 2, false );
        GuiFactory.createHeadingLabel ( group, "Rapid", 1, false );
        GuiFactory.createHiddenLabel ( group, 2, false );
    
        // LOG.info ( "createGui: min=" + scale.getMinimum () + " max=" + scale.getMaximum () + " inc=" + scale.getIncrement () + " pginc=" + scale.getPageIncrement () );
    
        // line 2
        GuiFactory.createHiddenLabel ( group, 1, false );
        feedOverrideIncrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.UP );
        GuiFactory.createHeadingLabel ( group, "+1%", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, false );
        spindleOverrideIncrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.UP );
        GuiFactory.createHeadingLabel ( group, "+1%", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, false );
        rapidOverrideScale = createScale ( group, 3, 3 );
        GuiFactory.createHeadingLabel ( group, "100%", 1, false );
        // rapidOverride100PercentButton = GuiFactory.createPushButton ( group, "100%", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( group, 1, false );
    
        // line 3
        GuiFactory.createHiddenLabel ( group, 1, false );
        feedOverrideIncrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.UP );
        GuiFactory.createHeadingLabel ( group, "+10%", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, false );
        spindleOverrideIncrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.UP );
        GuiFactory.createHeadingLabel ( group, "+10%", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, false );
        GuiFactory.createHeadingLabel ( group, "50%", 1, false );
        // rapidOverride50PercentButton = GuiFactory.createPushButton ( group, "50%", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( group, 1, false );
    
        // line 4
        GuiFactory.createHiddenLabel ( group, 1, false );
        feedOverride100PercentButton = GuiFactory.createPushButton ( group, "100%" );
        GuiFactory.createHiddenLabel ( group, 2, false );
        spindleOverride100PercentButton = GuiFactory.createPushButton ( group, "100%" );
        toggleSpindleStopButton = GuiFactory.createPushButton ( group, "" );
        toggleSpindleStopButton.setImage ( loadImage ( "/icons/suspend.gif" ) );
        GuiFactory.createHiddenLabel ( group, 1, false );
        GuiFactory.createHeadingLabel ( group, "25%", 1, false );
        // rapidOverride25PercentButton = GuiFactory.createPushButton ( group, "25%", SWT.FILL, true );
        GuiFactory.createHiddenLabel ( group, 1, false );
    
        // line 5
        GuiFactory.createHiddenLabel ( group, 1, false );
        feedOverrideDecrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.DOWN );
        GuiFactory.createHeadingLabel ( group, "-10%", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, false );
        spindleOverrideDecrease10PercentButton = GuiFactory.createArrowButton ( group, SWT.DOWN );
        GuiFactory.createHeadingLabel ( group, "-10%", 1, false );
        GuiFactory.createHiddenLabel ( group, 4, false );
    
        // line 6
        GuiFactory.createHiddenLabel ( group, 1, false );
        feedOverrideDecrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.DOWN );
        GuiFactory.createHeadingLabel ( group, "-1%", 1, false );
        GuiFactory.createHiddenLabel ( group, 1, false );
        spindleOverrideDecrease1PercentButton = GuiFactory.createArrowButton ( group, SWT.DOWN );
        GuiFactory.createHeadingLabel ( group, "-1%", 1, false );
        GuiFactory.createHiddenLabel ( group, 4, false );
    
        feedOverrideIncrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideIncrease1Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        feedOverrideIncrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideIncrease10Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        feedOverride100PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverride100Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        feedOverrideDecrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideDecrease10Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        feedOverrideDecrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendFeedOverrideDecrease1Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        spindleOverrideIncrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideIncrease1Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        spindleOverrideIncrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideIncrease10Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        spindleOverride100PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverride100Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        spindleOverrideDecrease10PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideDecrease10Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        spindleOverrideDecrease1PercentButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendSpindleOverrideDecrease1Percent ();
                super.widgetSelected ( event );
            }
        } );
    
        toggleSpindleStopButton.addSelectionListener ( new SelectionAdapter () {
            @Override
            public void widgetSelected ( SelectionEvent event ) {
                gcodeService.sendToggleSpindleStop ();
                super.widgetSelected ( event );
            }
        } );
    
    }

}
