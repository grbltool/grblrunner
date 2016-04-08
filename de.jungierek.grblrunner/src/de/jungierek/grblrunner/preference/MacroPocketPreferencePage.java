package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class MacroPocketPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroPocketPreferencePage.class );

    private static final String PAGE_NAME = "Pocket";
    public static final String ID = IConstant.KEY_BASE + ".macro.pocket";

    public MacroPocketPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new IntegerFieldEditor ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE, "z feedrate (mm/min)", 0, 999, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE, "xy feedrate (mm/min)", 0, 999, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.POCKET_MILL_DIAMETER, "mill diameter (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.POCKET_MILL_DIMENSION, "mill dimension", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.POCKET_MILL_Z_DEPTH, "z depth (mm)", -5.0, 0.0, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.POCKET_MILL_OVERLAP, "overlap (%)", 0, 99, getFieldEditorParent () ) );

    }

}
