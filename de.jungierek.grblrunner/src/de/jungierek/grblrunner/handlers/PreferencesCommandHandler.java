 
package de.jungierek.grblrunner.handlers;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.preference.GcodeViewPreferencePage;
import de.jungierek.grblrunner.preference.PreferenceGui;
import de.jungierek.grblrunner.preference.ScopedPreferenceStore;
import de.jungierek.grblrunner.preference.SerialPreferencePage;
import de.jungierek.grblrunner.preference.TerminalPreferencePage;

// some idieas from https://github.com/opcoach/e4Preferences/blob/master/com.opcoach.e4.preferences.example/src/com/opcoach/e4/preferences/example/pages/DefaultValuesInitializer.java

public class PreferencesCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( PreferencesCommandHandler.class );

    @Inject
    private EPartService partService;
    
    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    @Inject
    @Preference(nodePath = IConstants.PREFERENCE_NODE)
    private IEclipsePreferences preferences;

	@Execute
    public void execute ( Shell shell ) {

        LOG.debug ( "execute: shell=" + shell );

        PreferenceManager preferenceManager = new PreferenceManager ();
        
        IPreferenceStore preferenceStore = new ScopedPreferenceStore ( InstanceScope.INSTANCE, IConstants.PREFERENCE_NODE );

        // collectPreferencePages ( preferenceManager, preferenceStore );

        preferenceManager.addToRoot ( new PreferenceNode ( GcodeViewPreferencePage.ID, new GcodeViewPreferencePage ( preferenceStore ) ) );
        preferenceManager.addToRoot ( new PreferenceNode ( SerialPreferencePage.ID, new SerialPreferencePage ( preferenceStore ) ) );
        preferenceManager.addToRoot ( new PreferenceNode ( TerminalPreferencePage.ID, new TerminalPreferencePage ( preferenceStore ) ) );

        new PreferenceDialog ( shell, preferenceManager ).open ();

	}

    private void collectPreferencePages ( PreferenceManager preferenceManager, IPreferenceStore preferenceStore ) {

        List<MPart> elements = modelService.findElements ( application, null, MPart.class, null );
        for ( MPart part : elements ) {
            Object partObject = part.getObject ();
            try {
                IEclipseContext context = EclipseContextFactory.create ();
                context.set ( IEclipseContext.class, context );
                Object invoke = ContextInjectionFactory.invoke ( partObject, PreferenceGui.class, context );
                // LOG.info ( "execute: invoke=" + invoke );
                if ( invoke instanceof PreferencePage ) {
                    final PreferencePage preferencePage = (PreferencePage) invoke;
                    preferencePage.setPreferenceStore ( preferenceStore );
                    PreferenceNode node = new PreferenceNode ( part.getElementId (), preferencePage );
                    preferenceManager.addToRoot ( node );
                }
            }
            catch ( InjectionException exc ) {
                // LOG.error ( "execute:", exc );
            }
        }

    }
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}

}