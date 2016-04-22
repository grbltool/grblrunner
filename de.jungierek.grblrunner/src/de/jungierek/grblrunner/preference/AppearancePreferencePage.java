package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.GuiFactory;

public class AppearancePreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( AppearancePreferencePage.class );

    private static final String PAGE_NAME = "Appearance";
    public static final String ID = IConstant.KEY_BASE + ".appearance";

    private int spindleMinRpm;
    private int spindleMaxRpm;

    public AppearancePreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

        // TODO update this two fields whenever the min/max prefence change value
        spindleMinRpm = preferenceStore.getInt ( IPreferenceKey.SPINDLE_MIN );
        spindleMaxRpm = preferenceStore.getInt ( IPreferenceKey.SPINDLE_MAX );

    }
    
    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new DoubleFieldEditor ( IPreferenceKey.FIT_TO_SIZE_MARGIN, "Fit to Size Margin", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new BooleanFieldEditor ( IPreferenceKey.FIT_TO_SIZE_WITH_Z, "Fit to Size with Z", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new IntegerFieldEditor ( IPreferenceKey.SPINDLE_SPEED_ENTRY_1, "spindle speed entry 1", spindleMinRpm, spindleMaxRpm, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.SPINDLE_SPEED_ENTRY_2, "spindle speed entry 2", spindleMinRpm, spindleMaxRpm, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.SPINDLE_SPEED_ENTRY_3, "spindle speed entry 3", spindleMinRpm, spindleMaxRpm, getFieldEditorParent () ) );

    }

}
