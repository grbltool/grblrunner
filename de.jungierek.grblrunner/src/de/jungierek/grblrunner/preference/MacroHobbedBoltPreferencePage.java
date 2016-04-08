package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class MacroHobbedBoltPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroHobbedBoltPreferencePage.class );

    private static final String PAGE_NAME = "Hobbed Bolt";
    public static final String ID = IConstant.KEY_BASE + ".macro.hobbedbolt";

    public MacroHobbedBoltPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_FEEDRATE, "feedrate (mm/min)", 0, 999, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_ANGLE, "angle (°)", 0, 90, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_BOLT_DIAMETER, "bolt diameter (mm)", getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.HOBBED_BOLT_X_CLEARANCE, "x clearance (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_COUNT_RETRACTION, "count retraction", 0, 10, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.HOBBED_BOLT_RETRACTION, "retraction (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_WAIT_AT_TARGET, "wait at target (s)", 0, 10, getFieldEditorParent () ) );

    }

}
