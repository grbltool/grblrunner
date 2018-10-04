package de.jungierek.grblrunner.preference;


import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.GuiFactory;

public class CameraPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( CameraPreferencePage.class );

    private static final String PAGE_NAME = "Camera";
    public static final String ID = IConstant.KEY_BASE + ".camera";

    public CameraPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    private final static int CAMERA_OFFSET_RANGE = 200;

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new ColorFieldEditor ( IPreferenceKey.CAMERA_MIDCROSS_COLOR, "Midcross", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.CAMERA_MIDCROSS_RADIUS, "Midcross Radius", getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.CAMERA_MIDCROSS_OFFSET_X, "Midcross X-Offset", -CAMERA_OFFSET_RANGE, +CAMERA_OFFSET_RANGE, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.CAMERA_MIDCROSS_OFFSET_Y, "Midcross Y-Offset", -CAMERA_OFFSET_RANGE, +CAMERA_OFFSET_RANGE, getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new IntegerFieldEditor ( IPreferenceKey.CAMERA_MILL_OFFSET_X, "Mill X-Offset", -CAMERA_OFFSET_RANGE, +CAMERA_OFFSET_RANGE, getFieldEditorParent () ) );
        addField ( new IntegerFieldEditor ( IPreferenceKey.CAMERA_MILL_OFFSET_Y, "Mill Y-Offset", -CAMERA_OFFSET_RANGE, +CAMERA_OFFSET_RANGE, getFieldEditorParent () ) );

    }

}
