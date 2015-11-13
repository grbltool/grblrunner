package de.jungierek.grblrunner.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GuiFactory {
    
    private GuiFactory () {}
    
    public static Group createGroup ( Composite parent, String groupHeading, int horizontalSpan, int verticalSpan, boolean grabExcessHorizontalSpace ) {
        
        return createGroup ( parent, groupHeading, horizontalSpan, verticalSpan, grabExcessHorizontalSpace, false );
        
    }
    
    public static Group createGroup ( Composite parent, String groupHeading, int horizontalSpan, int verticalSpan, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace ) {
        
        Group group = new Group ( parent, SWT.SHADOW_ETCHED_IN );
        group.setText ( groupHeading );
        group.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, grabExcessHorizontalSpace, grabExcessVerticalSpace, horizontalSpan, verticalSpan ) );
        
        return group;
        
    }
    
    public static Label createHiddenLabel ( Composite parent ) {

        return createLabel ( parent, SWT.None, null, 1, false );

    }

    public static Label createHiddenLabel ( Composite parent, int span ) {

        return createLabel ( parent, SWT.None, null, span, false );

    }

    public static Label createHiddenLabel ( Composite parent, int span, boolean grabExcessHorizontalSpace ) {

        return createLabel ( parent, SWT.None, null, span, grabExcessHorizontalSpace );

    }

    public static Label createHeadingLabel ( Composite parent, String heading ) {

        return createHeadingLabel ( parent, SWT.CENTER, heading, 1 );

    }

    public static Label createHeadingLabel ( Composite parent, int style, String heading ) {

        return createHeadingLabel ( parent, style, heading, 1 );

    }

    public static Label createHeadingLabel ( Composite parent, String heading, int span ) {

        return createHeadingLabel ( parent, SWT.CENTER, heading, span );

    }

    public static Label createHeadingLabel ( Composite parent, String heading, int span, boolean grabExcessHorizontalSpace ) {

        return createLabel ( parent, SWT.CENTER, heading, span, grabExcessHorizontalSpace );

    }

    public static Label createHeadingLabel ( Composite parent, int style, String heading, int span ) {

        return createLabel ( parent, style, heading, span, true );

    }

    public static Label createCoordinateLabel ( Composite parent ) {

        return createLabel ( parent, SWT.BORDER | SWT.RIGHT, "0.000", 1, true );

    }

    public static Label createLabel ( Composite parent, int style, String heading, int span, boolean grabExcessHorizontalSpace ) {

        Label result = new Label ( parent, style );
        if ( heading != null ) result.setText ( heading );
        GridData gd = new GridData ();
        gd.horizontalSpan = span;
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        result.setLayoutData ( gd );

        return result;

    }
    
    public static Text createIntegerText ( Composite parent, String preset, int span, boolean grabExcessHorizontalSpace, int min ) {
        
        Text text = createText ( parent, SWT.SINGLE | SWT.RIGHT | SWT.BORDER, preset, span, grabExcessHorizontalSpace );
        text.addVerifyListener ( new IntegerVerifyer ( min ) );
        return text;
        
    }
    
    public static Text createIntegerText ( Composite parent, String preset, int span, boolean grabExcessHorizontalSpace, int min, int max ) {

        Text text = createText ( parent, SWT.SINGLE | SWT.RIGHT | SWT.BORDER, preset, span, grabExcessHorizontalSpace );
        text.addVerifyListener ( new IntegerVerifyer ( min, max ) );
        return text;

    }

    public static Text createDoubleText ( Composite parent, String preset, int span, boolean grabExcessHorizontalSpace, double min ) {

        Text text = createText ( parent, SWT.SINGLE | SWT.RIGHT | SWT.BORDER, preset, span, grabExcessHorizontalSpace );
        text.addVerifyListener ( new DoubleVerifyer ( min ) );
        return text;

    }

    public static Text createDoubleText ( Composite parent, String preset, int span, boolean grabExcessHorizontalSpace ) {

        Text text = createText ( parent, SWT.SINGLE | SWT.RIGHT | SWT.BORDER, preset, span, grabExcessHorizontalSpace );
        text.addVerifyListener ( new DoubleVerifyer () );
        return text;

    }

    public static Text createText ( Composite parent, String preset, int span, boolean grabExcessHorizontalSpace ) {
        
        return createText ( parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER, preset, span, grabExcessHorizontalSpace );
        
    }
    
    public static Text createText ( Composite parent, int style, String preset, int span, boolean grabExcessHorizontalSpace ) {

        Text result = new Text ( parent, style );
        result.setText ( preset );
        result.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, span, 1 ) );
        result.setEnabled ( false );
        
        return result;
    
    }
    
    
    public static Button createArrowButton ( Composite parent, int style ) {
        
        return createButton ( parent, SWT.ARROW | style, null, SWT.FILL, SWT.FILL, false, false );

    }
    
    public static Button createPushButton ( Composite parent, String heading ) {

        return createButton ( parent, SWT.PUSH, heading, SWT.CENTER, SWT.CENTER, true, false );

    }
    
    public static Button createPushButton ( Composite parent, String heading, int horizontalAlignment, boolean grabExcessHorizontalSpace  ) {

        return createButton ( parent, SWT.PUSH, heading, horizontalAlignment, SWT.CENTER, grabExcessHorizontalSpace, false );

    }

    public static Button createButton ( Composite parent, int style, String heading, int horizontalAlignment, int verticalAlignment, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace  ) {

        Button result = new Button ( parent, style );
        result.setEnabled ( false );
        if ( heading != null ) result.setText ( heading );
        result.setLayoutData ( new GridData ( horizontalAlignment, SWT.CENTER, grabExcessHorizontalSpace, false, 1, 1 ) );

        return result;

    }
    
    public static class IntegerVerifyer implements VerifyListener {
        
        private int min, max;

        public IntegerVerifyer () {

            this ( -999, +999 );

        }

        public IntegerVerifyer ( int min ) {

            this ( min, +999 );

        }

        public IntegerVerifyer ( int min, int max ) {

            this.min = min;
            this.max = max;

        }

        @Override
        public void verifyText ( VerifyEvent evt ) {

            String oldText = ((Text) evt.widget).getText (); // bisheriger Text
            String newText = oldText.substring ( 0, evt.start );
            newText += evt.text;
            newText += oldText.substring ( evt.end );

             try {
                // wenn nicht leer und nicht '-'
                if ( !newText.equals ( "" ) && !newText.equals ( "-" ) ) {
                    int i = Integer.parseInt ( newText ); // check for exception
                    if ( i < min || i > max ) {
                        evt.doit = false;
                        return;
                    }
                }
            }
            catch ( NumberFormatException exc ) {
                // Eingabe ungültig, nicht akzeptieren, keine Fehlermeldung
                evt.doit = false;
                return;
            }
             
        }
        
    }

    public static class DoubleVerifyer implements VerifyListener {

        private double min, max;

        public DoubleVerifyer () {

            this ( -999.9, +999.9 );

        }

        public DoubleVerifyer ( double min ) {

            this ( min, +999.9 );

        }

        public DoubleVerifyer ( double min, double max ) {

            this.min = min;
            this.max = max;

        }

        @Override
        public void verifyText ( VerifyEvent evt ) {

            String oldText = "";

            if ( evt.widget instanceof Text ) {
                oldText = ((Text) evt.widget).getText ();
            }
            else if ( evt.widget instanceof CCombo ) {
                oldText = ((CCombo) evt.widget).getText ();
            }
            String newText = oldText.substring ( 0, evt.start );
            newText += evt.text;
            newText += oldText.substring ( evt.end );

            try {
                // wenn nicht leer und nicht '-' und nicht '.'
                if ( !newText.equals ( "" ) && !newText.equals ( "-" ) ) {
                    double d = Double.parseDouble ( newText ); // check for exception
                    if ( d < min || d > max ) {
                        evt.doit = false;
                        return;
                    }
                }
            }
            catch ( NumberFormatException exc ) {
                // Eingabe ungültig, nicht akzeptieren, keine Fehlermeldung
                evt.doit = false;
                return;
            }

        }

    }

}
