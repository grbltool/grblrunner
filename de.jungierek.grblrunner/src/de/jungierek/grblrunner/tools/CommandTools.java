package de.jungierek.grblrunner.tools;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Creatable
public class CommandTools {

    private static final Logger LOG = LoggerFactory.getLogger ( CommandTools.class );

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    public MCommand findCommand ( String id ) {

        LOG.debug ( "findCommand: id=" + id );

        final List<MCommand> commands = modelService.findElements ( application, id, MCommand.class, null, EModelService.ANYWHERE );
        return commands.size () > 0 ? commands.get ( 0 ) : null;

    }

}
