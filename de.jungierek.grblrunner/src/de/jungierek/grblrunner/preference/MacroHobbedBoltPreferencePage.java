package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class MacroHobbedBoltPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroHobbedBoltPreferencePage.class );

    private static final String PAGE_NAME = "Hobbed Bolt";
    public static final String ID = IConstants.KEY_BASE + ".macro.hobbedbolt";

    public MacroHobbedBoltPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_FEEDRATE, "feedrate (mm/min)", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_ANGLE, "abgle (°)", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_BOLT_DIAMETER, "bolt diameter (mm)", getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.HOBBED_BOLT_X_CLEARANCE, "x clearance (mm)", 0.0, IConstants.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_COUNT_RETRACTION, "count retraction", getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.HOBBED_BOLT_RETRACTION, "retraction (mm)", 0.0, IConstants.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.HOBBED_BOLT_WAIT_AT_TARGET, "wait at target (s)", getFieldEditorParent () ) );

    }

}
