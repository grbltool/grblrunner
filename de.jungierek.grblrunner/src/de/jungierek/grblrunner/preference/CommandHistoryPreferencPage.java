package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class CommandHistoryPreferencPage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( CommandHistoryPreferencPage.class );

    private static final String PAGE_NAME = "Command History";
    public static final String ID = IConstant.KEY_BASE + ".command.history";

    public CommandHistoryPreferencPage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        final IntegerFieldEditor commandHistoryDepthEditor = new IntegerFieldEditor ( IPreferenceKey.COMMAND_HISTORY_DEPTH, "depth", getFieldEditorParent () );
        commandHistoryDepthEditor.setValidRange ( 1, 100 );
        addField ( commandHistoryDepthEditor );
        addField ( new BooleanFieldEditor ( IPreferenceKey.COMMAND_HISTORY_WITHNOWERROR, "with no error", getFieldEditorParent () ) );

    }

}
