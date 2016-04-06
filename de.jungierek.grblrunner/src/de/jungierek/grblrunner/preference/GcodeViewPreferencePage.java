package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tools.GuiFactory;

public class GcodeViewPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeViewPreferencePage.class );

    private static final String PAGE_NAME = "Gcode View";
    public static final String ID = IConstant.KEY_BASE + ".gcodeview";

    public GcodeViewPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        addField ( new DoubleFieldEditor ( IPreferenceKey.FIT_TO_SIZE_MARGIN, "Fit to Size Margin", 0.0, 100.0, getFieldEditorParent () ) );
        addField ( new BooleanFieldEditor ( IPreferenceKey.FIT_TO_SIZE_WITH_Z, "Fit to Size with Z", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), 2, true );

        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_AUTOLEVEL_GRID, "Autolevel Grid", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_GANTRY, "Gantry", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_MACHINE_ORIGIN, "Machine Origin", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_WORK_ORIGIN, "Work Origin", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_WORKAREA_BORDER, "Workarea Border", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_WORKAREA_MIDCROSS, "Workarea Midcros", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_SEEK, "Gcode Motion Mode Seek", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_LINEAR, "Gcode Motion Mode Linear", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_ARC, "Gcode Motion Mode Arc", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_OVERLAY_SEEK, "Overlay Gcode Motion Mode Seek", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_OVERLAY_LINEAR, "Overlay Gcode Motion Mode Linear", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_OVERLAY_ARC, "Overlay Gcode Motion Mode Arc", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_PROBE, "Gcode Probe ", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_PROCESSED, "Gcode Processed", getFieldEditorParent () ) );

    }

}
