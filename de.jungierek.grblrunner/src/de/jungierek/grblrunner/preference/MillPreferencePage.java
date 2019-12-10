package de.jungierek.grblrunner.preference;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.GuiFactory;

public class MillPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( MillPreferencePage.class );

    private static final String PAGE_NAME = "Mill";
    public static final String ID = IConstant.KEY_BASE + ".mill";

    public MillPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 3;

        addField ( new DirectoryFieldEditor ( IPreferenceKey.GCODE_PATH, "gcode path", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new IntegerFieldEditor ( IPreferenceKey.BAUDRATE, "Baudrate", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new DoubleFieldEditor ( IPreferenceKey.WORK_AREA_MAX_X, "Work Area X (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.WORK_AREA_MAX_Y, "Work Area Y (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );
        addField ( new BooleanFieldEditor ( IPreferenceKey.WORK_AREA_ORGIN_0x0, "Work Area Origin is at (0,0)", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new IntegerFieldEditor ( IPreferenceKey.SPINDLE_MIN, "Spindle min (rpm)", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.SPINDLE_MAX, "Spindle max (rpm)", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new IntegerFieldEditor ( IPreferenceKey.MAX_SEEK_FEEDRATE, "seek feedrate (mm/min)", getFieldEditorParent () ) );
        addField ( new DoubleFieldEditor ( IPreferenceKey.ACCELARATION, "accelaration (mm/s^2)", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new DoubleFieldEditor ( IPreferenceKey.Z_CLEARANCE, "z clearance (mm)", 0.0, IConstant.PREFERENCE_DOUBLE_MAX, getFieldEditorParent () ) );

    }

}
