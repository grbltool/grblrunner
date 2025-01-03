package de.jungierek.grblrunner.part;

import java.util.ArrayDeque;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IEvent;
import de.jungierek.grblrunner.part.group.CommandGroup;
import de.jungierek.grblrunner.part.group.GcodeLargeGroup;
import de.jungierek.grblrunner.part.group.ProgressGroup;
import de.jungierek.grblrunner.part.group.StatusLineGroup;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeGrblState;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeService;
import de.jungierek.grblrunner.service.gcode.IGrblRequest;

public class StatusLinePart {

    private static final Logger LOG = LoggerFactory.getLogger ( StatusLinePart.class );

    public static final String [] DIALOG_BUTTON_LABELS_OK_CANCEL = new String [] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL };

    private ArrayDeque<MessageDialog> dialogs = new ArrayDeque<> ( 5 );

    private EGrblState lastState = null;
    private boolean lastSentHalt = false;
    private String lastMessage = null;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    @Inject
    private IGcodeService gcodeService;

    @SuppressWarnings("unused")
    private GcodeLargeGroup gcodeLargeGroup;

    private CommandGroup commandGroup;

    @SuppressWarnings("unused")
    private StatusLineGroup statusLineGroup;

    @SuppressWarnings("unused")
    private ProgressGroup progressGroup;

    @Focus
    public void SetFocusToCommandText () {

        commandGroup.setFocus ();

    }

    @PostConstruct
    public void createGui ( Composite parent, IEclipseContext context ) {

        LOG.debug ( "createGui: parent=" + parent + " layout=" + parent.getLayout () );

        final int cols = 4;
        parent.setLayout ( new GridLayout ( cols, true ) );
        context.set ( IContextKey.PART_COLS, cols );
        context.set ( IContextKey.PART_GROUP_COLS, 1 ); // all groups have a width of 1 column
        context.set ( IContextKey.PART_GROUP_ROWS, 1 );

        // collect groups and hold reference to prevent garbage collection
        gcodeLargeGroup = ContextInjectionFactory.make ( GcodeLargeGroup.class, context );
        commandGroup = ContextInjectionFactory.make ( CommandGroup.class, context );
        statusLineGroup = ContextInjectionFactory.make ( StatusLineGroup.class, context );
        progressGroup = ContextInjectionFactory.make ( ProgressGroup.class, context );

    }

    @Inject
    @Optional
    public void msgErrorNotified ( @UIEventTopic(IEvent.MESSAGE_ERROR) String msg ) {

        LOG.trace ( "msgErrorNotified: msg=" + msg );

        // the default dialog blocks on open and the terminal is completly blocked
        // MessageDialog.openError ( shell, "Error", msg );
        
        // original code from MessageDialog supplemented with non blocking behavior
        // accept the default window icon, ok is the default button
        MessageDialog dialog = new MessageDialog ( shell, "Error", null, msg, MessageDialog.ERROR, new String [] { IDialogConstants.OK_LABEL }, 0 );
        dialog.setBlockOnOpen ( false );
        dialog.open ();

    }

    private class ToolChangeMessageDialog extends MessageDialog {

        public ToolChangeMessageDialog ( String msg ) {

            super ( shell, "Tool Change", null, msg + "\n\nOK will continue this Job, Cancel aborts the Job", MessageDialog.CONFIRM,
                    DIALOG_BUTTON_LABELS_OK_CANCEL, 0 );
            setBlockOnOpen ( false );

        }

        @Override
        protected void buttonPressed ( int buttonId ) {

            LOG.debug ( "buttonPressed: id=" + buttonId );

            if ( buttonId == IDialogConstants.OK_ID ) {
                gcodeService.sendStartCycle ();
            }
            else if ( buttonId == IDialogConstants.CANCEL_ID ) {
                gcodeService.sendReset ();
            }

            super.buttonPressed ( buttonId );

        }

    }

    @Inject
    @Optional
    public void playerLineNotified ( @UIEventTopic(IEvent.PLAYER_LINE) IGcodeLine gcodeLine ) {

        LOG.trace ( "playerLineNotified: gcodeLine=" + gcodeLine );

        if ( gcodeLine == null ) return;

        String line = gcodeLine.getLine ();
        final int pos = line.indexOf ( IConstant.GCODE_MSG_TAG );
        if ( pos > 0 ) {
            lastMessage = line.substring ( pos + IConstant.GCODE_MSG_TAG.length (), line.indexOf ( ")", pos ) );
        }

    }

    @Inject
    @Optional
    public void sentNotified ( @UIEventTopic(IEvent.GRBL_SENT) IGrblRequest request ) {

        LOG.trace ( "sentNotified: request=" + request );

        if ( request == null ) {
            LOG.warn ( "sentNotified: request == null" );
            return;
        }

        lastSentHalt = request.getMessage ().startsWith ( EGcodeMode.PROGRAM_HALT.getCommand () );

    }


    @Inject
    @Optional
    public void stateUpdateNotified ( @UIEventTopic(IEvent.UPDATE_STATE) IGcodeGrblState state ) {

        LOG.trace ( "stateUpdateNotified: state=" + state );

        if ( state == null ) return;

        final EGrblState grblState = state.getGrblState ();

        // if ( grblState == EGrblState.HOLD && lastSentHalt && !grblState.equals ( lastState ) ) {
        if ( grblState == EGrblState.HOLD && lastSentHalt && grblState != lastState ) {
            MessageDialog dialog = dialogs.poll ();
            if ( dialog != null ) {
                dialog.open ();
            }
        }

        lastState = grblState;

    }

    @Inject
    @Optional
    public void msgOnHaltNotified ( @UIEventTopic(IEvent.MESSAGE_ON_HALT) String msg ) {

        LOG.trace ( "msgOnHaltNotified: msg=" + msg );

        if ( lastMessage != null ) msg += "\n\n" + lastMessage;
        dialogs.offer ( new ToolChangeMessageDialog ( msg ) );

        lastMessage = null;

    }

    @Inject
    @Optional
    public void alarmNotified ( @UIEventTopic(IEvent.GRBL_ALARM) String [] line ) {

        LOG.trace ( "alarmNotified: line=" + line [0] );

        dialogs.clear ();

    }

}