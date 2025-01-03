package de.jungierek.grblrunner.part.group;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.tool.GuiFactory;

public class ProgressGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( ProgressGroup.class );

    private static final String GROUP_NAME = "Progress";

    @Inject
    private IGcodeService gcodeService;

    private ProgressBar progressBar;

    @PostConstruct
    public void createGui ( Composite parent, @Named(IContextKey.PART_GROUP_COLS) int groupCols ) {

        LOG.debug ( "createGui: parent=" + parent );

        Group group = GuiFactory.createGroup ( parent, GROUP_NAME, groupCols, 1, true );

        final int cols = 1;
        group.setLayout ( new GridLayout ( cols, true ) );

        progressBar = new ProgressBar ( group, SWT.SMOOTH );
        progressBar.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, cols, 1 ) );

        progressBar.setMinimum ( 0 );
        progressBar.setMaximum ( 1 );
        progressBar.setState ( SWT.NORMAL );

    }

    @Inject
    public void setGcodeProgram ( @Optional @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.debug ( "setGcodeProgram: program=" + program );

        // reset progress bar
        if ( program == null && progressBar != null && !progressBar.isDisposed () ) {
            progressBar.setSelection ( 0 );
            progressBar.setState ( SWT.NORMAL );
        }

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String line ) {

        LOG.debug ( "alarmNotified: line=" + line );

        progressBar.setState ( SWT.ERROR );

    }

    @Inject
    @Optional
    public void updateStateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState grblState ) {

        LOG.trace ( "updateStateNotified: grblState=" + grblState );

        switch ( grblState.getGrblState () ) {
            case IDLE:
            case RUN:
            case HOME:
                progressBar.setState ( SWT.NORMAL );
                break;

            case HOLD:
            case CHECK:
                progressBar.setState ( SWT.PAUSED );
                break;

            case ALARM:
                progressBar.setState ( SWT.ERROR );
                break;

            default:
                break;
        }

    }

    @Inject
    @Optional
    public void disconnectedNotified ( @UIEventTopic(IEvent.SERIAL_DISCONNECTED) String port ) {

        LOG.trace ( "connectedNotified: port=" + port );

        progressBar.setSelection ( 0 );

    }

    @Inject
    @Optional
    public void playerStartNotified ( @UIEventTopic(IEvent.PLAYER_START) String timestamp, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.trace ( "playerStartNotified: timestamp=" + timestamp );

        progressBar.setMinimum ( 0 );
        progressBar.setMaximum ( program.getLineCount () );
        progressBar.setSelection ( 0 );

    }

    @Inject
    @Optional
    public void playerLineNotified ( @UIEventTopic(IEvent.PLAYER_LINE) IGcodeLine line ) {

        LOG.trace ( "gcodePlayerLineNotified: line=" + line );

        progressBar.setSelection ( progressBar.getSelection () + 1 );

    }

    @Inject
    @Optional
    public void scanStartNotified ( @UIEventTopic(IEvent.AUTOLEVEL_START) String timestamp, @Named(IServiceConstants.ACTIVE_SELECTION) IGcodeProgram program ) {

        LOG.trace ( "scanStartNotified:" );

        progressBar.setMinimum ( 0 );
        progressBar.setMaximum ( program.getNumProbePoints () );
        progressBar.setSelection ( 0 );

    }

    @Inject
    @Optional
    public void updateProbeNotified ( @UIEventTopic(IEvent.AUTOLEVEL_UPDATE) IGcodePoint probe ) {

        LOG.trace ( "updateProbeNotified: probe=" + probe );

        if ( gcodeService.isAutolevelScan () ) {

            progressBar.setSelection ( progressBar.getSelection () + 1 );

        }

    }

}
