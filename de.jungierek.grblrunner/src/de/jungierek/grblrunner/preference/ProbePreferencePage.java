package de.jungierek.grblrunner.preference;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class ProbePreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( ProbePreferencePage.class );

    private static final String PAGE_NAME = "Probe";
    public static final String ID = IConstants.KEY_BASE + ".probe";

    public ProbePreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 3;

        addField ( new BooleanFieldEditor ( IPreferenceKey.PROBE_WITH_ERROR, "use gcode command with error indication", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.PROBE_FEEDRATE, "feedrate", getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.PROBE_Z_MAX, "z max", IConstants.PREFERENCE_DOUBLE_MIN, IConstants.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.PROBE_DEPTH, "depth", IConstants.PREFERENCE_DOUBLE_MIN, IConstants.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );

    }

}
