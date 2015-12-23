package de.jungierek.grblrunner.tools;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

@SuppressWarnings("restriction")
@Singleton
@Creatable
public class PartTools {

    private static final Logger LOG = LoggerFactory.getLogger ( PartTools.class );

    @Inject
    private ECommandService commandService;

    @Inject
    private EHandlerService handlerService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    public void executeCommand ( String commandId ) {

        LOG.trace ( "executeCommand:id=" + commandId );
    
        executeCommand1 ( commandId );
    
    }

    private void executeCommand1 ( final String commandId ) {

        LOG.trace ( "executeCommand: id=" + commandId );

        Command command = commandService.getCommand ( commandId );

        if ( command.isDefined () ) {
            ParameterizedCommand parameterCommand = commandService.createCommand ( commandId, null );
            if ( handlerService.canExecute ( parameterCommand ) ) {
                handlerService.executeHandler ( parameterCommand );
            }
            else {
                MessageDialog.openError ( shell, "Internal Error", "Command can not be executed!\n" + commandId );
            }
        }
        else {
            MessageDialog.openError ( shell, "Internal Error", "Command not found!\n" + commandId );
        }

    }
    
    private void executeCommand2 ( final String commandId ) {

        LOG.debug ( "executeCommand:id=" + commandId );

        try {
            Command command = commandService.getCommand ( commandId );
            command.executeWithChecks ( new ExecutionEvent () );
        }
        catch ( ExecutionException | NotDefinedException | NotEnabledException | NotHandledException exc ) {

            LOG.error ( "execute:", exc );

            StringBuilder sb = new StringBuilder ( "Command " + commandId + " not executed!\n\n" );

            sb.append ( "Cause:\n" );
            sb.append ( exc + "\n\n" );
            // for ( StackTraceElement elem : exc.getStackTrace () ) {
            // sb.append ( "\n\tat " );
            // sb.append ( elem );
            // }

            MessageDialog.openError ( shell, "Internal Error", "" + sb );

        }

    }

    public void gcodeToText ( Text text, IGcodeProgram gcodeProgram ) {

        text.setText ( "" );
        StringBuilder sb = new StringBuilder ();

        for ( IGcodeLine gcodeLine : gcodeProgram.getAllGcodeLines () ) {
            sb.append ( gcodeLine.getLine () + "\n" );
        }

        text.setText ( "" + sb );
    }

    public CommandExecuteSelectionListener createCommandExecuteSelectionListener ( String commandId ) {
        
        return new CommandExecuteSelectionListener ( commandId );
        
    }

    private class CommandExecuteSelectionListener extends SelectionAdapter {

        final String commandId;

        public CommandExecuteSelectionListener ( String commandId ) {
            LOG.trace ( "CommandExecuteSelectionListener: id=" + commandId );
            this.commandId = commandId;
        }

        @Override
        public void widgetSelected ( SelectionEvent evt ) {
            LOG.trace ( "widgetSelected: evt=" + evt );
            executeCommand ( commandId );
        }

    }

    public int parseIntegerField ( Text field, int defaultValue ) {

        int result = defaultValue;
        try {
            result = Integer.parseInt ( field.getText () );
        }
        catch ( NumberFormatException exc ) {}

        return result;

    }

    public double parseDouble ( String s, double defaultValue ) {

        double result = defaultValue;

        if ( s != null ) {
            try {
                result = Double.parseDouble ( s );
            }
            catch ( NumberFormatException exc ) {}
        }

        return result;

    }

    public double parseDoubleField ( Text field, double defaultValue ) {

        return parseDouble ( field.getText (), defaultValue );

    }

    public boolean parseBoolean ( String value ) {

        return Boolean.parseBoolean ( value );

    }

}
