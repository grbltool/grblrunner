package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.GuiFactory;

public class TerminalPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( TerminalPreferencePage.class );

    private static final String PAGE_NAME = "Terminal";
    public static final String ID = IConstant.KEY_BASE + ".terminal";

    public TerminalPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        addField ( new FontFieldEditor ( IPreferenceKey.TERMINAL_FONT_DATA, "Font", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), 3, true );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_TERMINAL_FOREGROUND, "Terminal Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_TERMINAL_BACKGROUND, "Terminal Background", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_ALARM_FOREGROUND, "Alarm Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_ALARM_BACKGROUND, "Alarm Background", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_TIMESTAMP_BACKGROUND, "Timestamp Background", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_SUPPRESSED_LINE_FOREGROUND, "Suppressed Line Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_OK_FOREGROUND, "Ok Message Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_ERROR_FOREGROUND, "Error Message Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_MSG_FOREGROUND, "Feedback Message Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_MSG_BACKGROUND, "Feedback Message Background", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_GRBL_FOREGROUND, "Grbl Message Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_GRBL_BACKGROUND, "Grbl Message Background", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_OK_SUPPRESSED_FOREGROUND, "Suppressed Ok Message Foreground", getFieldEditorParent () ) );
        addField ( new ColorFieldEditor ( IPreferenceKey.COLOR_ERROR_SUPPRESSED_FOREGROUND, "Suppressed Error Message Foreground", getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), 3, true );

    }

}
