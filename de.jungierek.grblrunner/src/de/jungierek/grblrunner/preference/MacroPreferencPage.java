package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class MacroPreferencPage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroPreferencPage.class );

    private static final String PAGE_NAME = "Macro";
    public static final String ID = IConstant.KEY_BASE + ".macro";

    private int spindleMinRpm;
    private int spindleMaxRpm;
    
    public MacroPreferencPage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

        // TODO update this two fields whenever the min/max prefence change value
        spindleMinRpm = preferenceStore.getInt ( IPreferenceKey.SPINDLE_MIN );
        spindleMaxRpm = preferenceStore.getInt ( IPreferenceKey.SPINDLE_MAX );

    }

    @Override
    protected void createFieldEditors () {

        addField ( new IntegerFieldEditor ( IPreferenceKey.MACRO_SPINDLE_SPEED, "spindle speed (rpm)", spindleMinRpm, spindleMaxRpm, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.MACRO_Z_LIFTUP, "z liftup (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );

    }

}
