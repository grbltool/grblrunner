package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class MacroPocketPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroPocketPreferencePage.class );

    private static final String PAGE_NAME = "Pocket";
    public static final String ID = IConstants.KEY_BASE + ".macro.pocket";

    public MacroPocketPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new IntegerFieldEditor ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE, "z feedrate (mm/min)", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE, "xy feedrate (mm/min)", getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.POCKET_MILL_DIAMETER, "mill diameter (mm)", 0.0, IConstants.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.POCKET_MILL_DIMENSION, "mill dimension", 0.0, IConstants.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.POCKET_MILL_Z_DEPTH, "z depth (mm)", IConstants.PREFERENCE_DOUBLE_MIN, 0.0, getFieldEditorParent () ) );
        final IntegerFieldEditor overlapEditor = new IntegerFieldEditor ( IPreferenceKey.POCKET_MILL_OVERLAP, "overlap (%)", getFieldEditorParent () );
        overlapEditor.setValidRange ( 0, 99 );
        addField ( overlapEditor );

    }

}
