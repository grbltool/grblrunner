package de.jungierek.grblrunner.part;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;

public class GcodeEditorDirectToolItem {

    private static final Logger LOG = LoggerFactory.getLogger ( GcodeEditorDirectToolItem.class );

    @Inject
    private IGcodeProgram gcodeProgram;

    @Execute
    public void execute ( Shell shell ) {

        LOG.debug ( "execute:" );

        new GcodeDialog ( shell ).open ();

    }

    private class GcodeDialog extends Dialog {

        private static final String JUSTIFY_PLACE = "                                                                                "; // 80x space

        public GcodeDialog ( Shell shell ) {

            super ( shell );

        }

        @Override
        protected Control createDialogArea ( Composite parent ) {

            LOG.debug ( "createDialogArea: parent=" + parent );

            Font terminalFont = JFaceResources.getFont ( JFaceResources.TEXT_FONT );

            Composite container = (Composite) super.createDialogArea ( parent );
            GridLayout layout = new GridLayout ( 1, true );
            layout.marginRight = 5;
            layout.marginLeft = 10;
            container.setLayout ( layout );

            Text textGcodeLine = new Text ( parent, SWT.MULTI | SWT.V_SCROLL );
            textGcodeLine.setFont ( terminalFont );
            textGcodeLine.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true ) );
            textGcodeLine.setEditable ( false );

            for ( IGcodeLine gcodeLine : gcodeProgram.getAllGcodeLines () ) {

                StringBuilder sb = new StringBuilder ( 200 );
                sb.append ( "  " );
                sb.append ( gcodeLine.getLine () );

                if ( gcodeLine.isMotionMode () ) {

                    IGcodePoint start = gcodeLine.getStart ();
                    IGcodePoint end = gcodeLine.getEnd ();

                    int col = 40;
                    sb.append ( JUSTIFY_PLACE.substring ( 0, Math.max ( col - sb.length (), 0 ) ) );
                    sb.append ( String.format ( "%5s:  ", gcodeLine.getLineNo () ) );
                    sb.append ( String.format ( "%3s   ", gcodeLine.getGcodeMode ().getCommand () ) );

                    sb.append ( String.format ( "X%+08.3f ", start.getX () ) );
                    sb.append ( String.format ( "Y%+08.3f ", start.getY () ) );
                    sb.append ( String.format ( "Z%+08.3f   ", start.getZ () ) );

                    sb.append ( String.format ( "X%+08.3f ", end.getX () ) );
                    sb.append ( String.format ( "Y%+08.3f ", end.getY () ) );
                    sb.append ( String.format ( "Z%+08.3f   ", end.getZ () ) );

                    if ( gcodeLine.isMotionModeArc () ) sb.append ( String.format ( "R%+08.3f   ", gcodeLine.getRadius () ) );

                    sb.append ( String.format ( "F%s   ", gcodeLine.getFeedrate () ) );

                    // sb.append ( gcodeLine );
                }

                sb.append ( "\n" );

                textGcodeLine.append ( "" + sb );

            }

            return super.createDialogArea ( parent );

        }

    }

}
