package de.jungierek.grblrunner.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
public class Toolbox {

    private static final Logger LOG = LoggerFactory.getLogger ( Toolbox.class );

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    @Inject
    private ECommandService commandService;

    @Inject
    private EHandlerService handlerService;

    @Inject
    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell shell;

    public void gcodeToText ( Text text, IGcodeProgram gcodeProgram ) {
    
        text.setText ( "" );
        StringBuilder sb = new StringBuilder ();
    
        for ( IGcodeLine gcodeLine : gcodeProgram.getAllGcodeLines () ) {
            sb.append ( gcodeLine.getLine () + "\n" );
        }
    
        text.setText ( "" + sb );
    }

    public int parseInteger ( final String s, int defaultValue ) {
        int result = defaultValue;
        try {
            result = Integer.parseInt ( s );
        }
        catch ( NumberFormatException exc ) {}

        return result;
    }

    public int parseIntegerField ( Text field, int defaultValue ) {

        return parseInteger ( field.getText (), defaultValue );

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

    // ---------------------------------------------------------------------------------

    public MCommand findCommand ( String id ) {

        LOG.debug ( "findCommand: id=" + id );

        final List<MCommand> commands = modelService.findElements ( application, id, MCommand.class, null, EModelService.ANYWHERE );
        return commands.size () > 0 ? commands.get ( 0 ) : null;

    }

    public void executeCommand ( String commandId, Map<String, Object> parameter, CommandParameterCallback parameterCallback ) {

        LOG.trace ( "executeCommand:id=" + commandId );

        if ( parameter != null && parameterCallback != null ) {
            parameter.putAll ( parameterCallback.getParameter () );
        }

        executeCommand1 ( commandId, parameter );

    }

    private void executeCommand1 ( final String commandId, Map<String, Object> parameter ) {

        LOG.trace ( "executeCommand: id=" + commandId );

        Command command = commandService.getCommand ( commandId );

        if ( command.isDefined () ) {
            ParameterizedCommand parameterCommand = commandService.createCommand ( commandId, parameter );
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

    @SuppressWarnings("unused")
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

    public CommandExecuteSelectionListener createCommandExecuteSelectionListener ( String commandId, Map<String, Object> parameter, CommandParameterCallback parameterCallback ) {

        return new CommandExecuteSelectionListener ( commandId, parameter, parameterCallback );

    }

    public CommandExecuteSelectionListener createCommandExecuteSelectionListener ( String commandId, CommandParameterCallback parameterCallback ) {

        return new CommandExecuteSelectionListener ( commandId, new HashMap<String, Object> (), parameterCallback );

    }

    public CommandExecuteSelectionListener createCommandExecuteSelectionListener ( String commandId, Map<String, Object> parameter ) {

        return new CommandExecuteSelectionListener ( commandId, parameter );

    }

    public CommandExecuteSelectionListener createCommandExecuteSelectionListener ( String commandId ) {

        return new CommandExecuteSelectionListener ( commandId );

    }

    public void addMenuItemTo ( List<MMenuElement> items, final boolean selectThisItem, final String commandId, final String parameterId, final String value ) {
    
        MCommand command = findCommand ( commandId );
    
        MParameter parameter = MCommandsFactory.INSTANCE.createParameter ();
        parameter.setElementId ( parameterId + value );
        parameter.setName ( parameterId ); // this is the importend "id"
        parameter.setValue ( value );
    
        // MCommandParameter commandParameter = MCommandsFactory.INSTANCE.createCommandParameter ();
        // commandParameter.setElementId ( parameterId );
        // commandParameter.setName ( parameterId );
        // commandParameter.setOptional ( true );
    
        MHandledMenuItem item = MMenuFactory.INSTANCE.createHandledMenuItem ();
        item.setLabel ( value );
        item.setType ( ItemType.RADIO );
        item.setCommand ( command );
        item.getParameters ().add ( parameter );
        if ( selectThisItem ) item.setSelected ( true );
        items.add ( item );
    
    }

    private class CommandExecuteSelectionListener extends SelectionAdapter {

        final String commandId;
        final Map<String, Object> parameter;
        final CommandParameterCallback parameterCallback;

        public CommandExecuteSelectionListener ( String commandId, Map<String, Object> parameter, CommandParameterCallback parameterCallback ) {

            LOG.trace ( "CommandExecuteSelectionListener: id=" + commandId + " parameter=" + parameter + " callback=" + parameterCallback );

            this.commandId = commandId;
            this.parameter = parameter;
            this.parameterCallback = parameterCallback;

        }

        public CommandExecuteSelectionListener ( String commandId, Map<String, Object> parameter ) {

            this ( commandId, parameter, null );

        }

        public CommandExecuteSelectionListener ( String commandId ) {

            this ( commandId, null, null );

        }

        @Override
        public void widgetSelected ( SelectionEvent evt ) {

            LOG.trace ( "widgetSelected: evt=" + evt );

            executeCommand ( commandId, parameter, parameterCallback );

        }

    }

}
