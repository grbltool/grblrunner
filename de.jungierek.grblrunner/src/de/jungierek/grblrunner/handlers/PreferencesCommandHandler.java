 
package de.jungierek.grblrunner.handlers;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.preference.ColorsAndFontsPreferencePage;
import de.jungierek.grblrunner.preference.GcodeViewPreferencePage;
import de.jungierek.grblrunner.preference.MillPreferencePage;
import de.jungierek.grblrunner.preference.ProbePreferencePage;
import de.jungierek.grblrunner.preference.ScopedPreferenceStore;
import de.jungierek.grblrunner.preference.SerialPreferencePage;
import de.jungierek.grblrunner.preference.TerminalPreferencePage;

// some idieas from https://github.com/opcoach/e4Preferences/blob/master/com.opcoach.e4.preferences.example/src/com/opcoach/e4/preferences/example/pages/DefaultValuesInitializer.java

public class PreferencesCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger ( PreferencesCommandHandler.class );

	@Execute
    public void execute ( Shell shell ) {

        LOG.debug ( "execute: shell=" + shell );

        PreferenceManager preferenceManager = new PreferenceManager ( '/' );
        
        IPreferenceStore preferenceStore = new ScopedPreferenceStore ( InstanceScope.INSTANCE, IConstants.PREFERENCE_NODE );
        

        preferenceManager.addToRoot ( new PreferenceNode ( MillPreferencePage.ID, new MillPreferencePage ( preferenceStore ) ) );
        PreferenceNode colorsAndFontsNode = new PreferenceNode ( ColorsAndFontsPreferencePage.ID, new ColorsAndFontsPreferencePage ( preferenceStore ) );
        preferenceManager.addToRoot ( colorsAndFontsNode );
        preferenceManager.addTo ( colorsAndFontsNode.getId (), new PreferenceNode ( GcodeViewPreferencePage.ID, new GcodeViewPreferencePage ( preferenceStore ) ) );
        preferenceManager.addTo ( colorsAndFontsNode.getId (), new PreferenceNode ( SerialPreferencePage.ID, new SerialPreferencePage ( preferenceStore ) ) );
        preferenceManager.addTo ( colorsAndFontsNode.getId (), new PreferenceNode ( TerminalPreferencePage.ID, new TerminalPreferencePage ( preferenceStore ) ) );
        preferenceManager.addToRoot ( new PreferenceNode ( ProbePreferencePage.ID, new ProbePreferencePage ( preferenceStore ) ) );

        new PreferenceDialog ( shell, preferenceManager ).open ();

	}

	@CanExecute
	public boolean canExecute() {
		
		return true;
	}

}