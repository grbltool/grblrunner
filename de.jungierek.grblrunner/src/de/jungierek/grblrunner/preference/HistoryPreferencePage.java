package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.tool.GuiFactory;

public class HistoryPreferencePage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( HistoryPreferencePage.class );

    private static final String PAGE_NAME = "History";
    public static final String ID = IConstant.KEY_BASE + ".history";

    public HistoryPreferencePage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }
    
    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new IntegerFieldEditor ( IPreferenceKey.STATUS_HISTORY_DEPTH, "status depth", 1, IConstant.MAX_HISTORY_DEPTH, getFieldEditorParent () ) );
        GuiFactory.createHiddenLabel ( getFieldEditorParent (), cols, true );

        addField ( new IntegerFieldEditor ( IPreferenceKey.COMMAND_HISTORY_DEPTH, "command depth", 1, IConstant.MAX_HISTORY_DEPTH, getFieldEditorParent () ) );
        addField ( new BooleanFieldEditor ( IPreferenceKey.COMMAND_HISTORY_WITHNOWERROR, "report only commands with no error", getFieldEditorParent () ) );

    }

}
