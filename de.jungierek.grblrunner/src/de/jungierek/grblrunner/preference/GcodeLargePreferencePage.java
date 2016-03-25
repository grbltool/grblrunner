package de.jungierek.grblrunner.preference;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tools.GuiFactory;

public class GcodeLargePreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeLargePreferencePage.class );

    private static final String PAGE_NAME = "Large Gcode Coordinates";
    public static final String ID = IConstant.KEY_BASE + ".gcodelarge";

    public GcodeLargePreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 3;

        addField ( new FontFieldEditor ( IPreferenceKey.GCODE_LARGE_FONT_DATA, "Font", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), 3, true );

    }

}
