package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class SerialPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( SerialPreferencePage.class );

    private static final String PAGE_NAME = "Serial";
    public static final String ID = IConstants.KEY_BASE + ".serial";

    public SerialPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        // addField ( new IntegerFieldEditor ( IPreferenceKey.BAUDRATE, "Baudrate", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_CONNECT, "Connect", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_DISCONNECT, "Disconnect", getFieldEditorParent () ) );

    }

}
