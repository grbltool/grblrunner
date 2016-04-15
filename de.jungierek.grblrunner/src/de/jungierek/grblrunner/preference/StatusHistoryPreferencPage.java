package de.jungierek.grblrunner.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.constants.IPreferenceKey;

public class StatusHistoryPreferencPage extends FieldEditorPreferencePage {

    private static final Logger LOG = LoggerFactory.getLogger ( StatusHistoryPreferencPage.class );

    private static final String PAGE_NAME = "Status History";
    public static final String ID = IConstant.KEY_BASE + ".status.history";

    public StatusHistoryPreferencPage ( IPreferenceStore preferenceStore ) {

        super ( PAGE_NAME, GRID );
        setPreferenceStore ( preferenceStore );

    }

    @Override
    protected void createFieldEditors () {

        final int cols = 2;

        addField ( new de.jungierek.grblrunner.preference.IntegerFieldEditor ( IPreferenceKey.STATUS_HISTORY_DEPTH, "depth", 1, 100,
                getFieldEditorParent () ) );

    }

}
