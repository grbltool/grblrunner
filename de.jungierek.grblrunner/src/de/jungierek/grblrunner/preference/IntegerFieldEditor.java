package de.jungierek.grblrunner.preference;

import org.eclipse.swt.widgets.Composite;

public class IntegerFieldEditor extends org.eclipse.jface.preference.IntegerFieldEditor {

    public IntegerFieldEditor () {}

    public IntegerFieldEditor ( String name, String labelText, Composite parent ) {

        super ( name, labelText, parent );

    }

    public IntegerFieldEditor ( String name, String labelText, int min, int max, Composite parent ) {

        super ( name, labelText, parent );
        setValidRange ( min, max );

    }

}
