package de.jungierek.grblrunner.part.group;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.kabeja.DraftDocument;
import org.kabeja.common.Layer;
import org.kabeja.common.Type;
import org.kabeja.dxf.parser.DXFParserBuilder;
import org.kabeja.entities.Line;
import org.kabeja.math.Point3D;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IContextKey;
import de.jungierek.grblrunner.constants.IPreferenceKey;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tool.GuiFactory;

public class MacroDxfGroup extends MacroGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroDxfGroup.class );

    private static final String GROUP_NAME = "Dxf";
    private static final int PART_COLS = 9;

    private Text xyFeedrateText;
    private Text spindleSpeedText;
    private Text millDiameterText;
    private Text zClearanceText;
    private Text zLiftupText;
    private Text zDepthText;
    private Text zFeedrateText;
    private Button drillCompensationCheckButton;

    @Override
    protected int getGridLayoutColumns () {
        return PART_COLS;
    }

    @Override
    protected String getGroupName () {
        return GROUP_NAME;
    }

    @Override
    public void createGroupControls ( Group group, int partCols, int groupCols ) {

        LOG.debug ( "createGroupControls:" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z liftup", 1 );
        zLiftupText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate xy", 1 );
        xyFeedrateText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ), 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z clearance", 1 );
        zClearanceText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "spindle speed", 1 );
        spindleSpeedText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ), 1, true,
                getIntPreference ( IPreferenceKey.SPINDLE_MIN ), getIntPreference ( IPreferenceKey.SPINDLE_MAX ) );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "rpm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mill diameter", 1 );
        millDiameterText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_DIAMETER ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z depth", 1 );
        zDepthText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) ), 1, true, -5.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate z", 1 );
        zFeedrateText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ), 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "cutter comp.", 1 );
        drillCompensationCheckButton = GuiFactory.createButton ( group, SWT.CHECK, null, SWT.LEFT, SWT.CENTER, true, false );
        drillCompensationCheckButton.setSelection ( true );
        GuiFactory.createHiddenLabel ( group );

        xyFeedrateText.addModifyListener ( textFieldModifyListener );
        zFeedrateText.addModifyListener ( textFieldModifyListener );
        spindleSpeedText.addModifyListener ( textFieldModifyListener );
        millDiameterText.addModifyListener ( textFieldModifyListener );
        zClearanceText.addModifyListener ( textFieldModifyListener );
        zLiftupText.addModifyListener ( textFieldModifyListener );
        zDepthText.addModifyListener ( textFieldModifyListener );
        drillCompensationCheckButton.addSelectionListener ( buttonSelectionListener );

    }

    @Override
    public void restorePreferenceData () {
        
        // xDimensionText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) ) );
        zLiftupText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) ) );
        xyFeedrateText.setText ( "" + getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ) );
        // yDimensionText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) ) );
        zClearanceText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) ) );
        spindleSpeedText.setText ( "" + getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ) );
        millDiameterText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_DIAMETER ) ) );
        zDepthText.setText ( formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) ) );
        zFeedrateText.setText ( "" + getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ) );
        // drillCompensationCheckButton.setSelection ( true );

    }

    @Override
    protected String getTitle () {

        return "gcode generation from dxf";

    }

    // http://www.programcreek.com/java-api-examples/index.php?api=org.kabeja.parser.Parser
    @Override
    public void generateGcodeCore ( IGcodeProgram gcodeProgram ) {

        LOG.debug ( "generateGcodeCore: start" );
        
        int xyFeedrate = toolbox.parseIntegerField ( xyFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ) );
        int spindleSpeed = toolbox.parseIntegerField ( spindleSpeedText, getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ) );
        double millDiameter = toolbox.parseDoubleField ( millDiameterText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIAMETER ) );
        double zClearance = toolbox.parseDoubleField ( zClearanceText, getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) );
        double zLiftup = toolbox.parseDoubleField ( zLiftupText, getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) );
        double zDepth = toolbox.parseDoubleField ( zDepthText, getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) );
        boolean isDrillCompensation = drillCompensationCheckButton.getSelection ();
        int zFeedrate = toolbox.parseIntegerField ( zFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ) );

        double millRadius = millDiameter / 2;

        motionSeekZ ( zClearance );
        motionSeekXY ( 0.0, 0.0 );
        spindleOn ( spindleSpeed );

        final String dxfFileName = (String) context.get ( IContextKey.MACRO_DXF_FILE_NAME );
        LOG.debug ( "generateGcodeCore: dxfFile=" + dxfFileName );
        File file = new File ( dxfFileName );
        Parser parser = DXFParserBuilder.createDefaultParser ();

        try {

            LOG.debug ( "generateGcodeCore: ----------------------------------------" );

            DraftDocument document = parser.parse ( new FileInputStream ( file ), new HashMap<String, Object> () );

            Point3D lastP2 = null;

            // Collection<Layer> layers = document.getLayers ();
            // for ( Layer layer : layers ) {
            // LOG.debug ( "generateGcodeCore: layer=" + layer.getName () );
            // }

            Layer layer0 = document.getLayer ( "0" );
            List<Line> lines = layer0.getEntitiesByType ( Type.TYPE_LINE );
            for ( Line line : lines ) {
                final Point3D p1 = line.getStartPoint ();
                final Point3D p2 = line.getEndPoint ();
                LOG.debug ( "generateGcodeCore: p1=" + point ( p1 ) + " p2=" + point ( p2 ) + " lastP2=" + point ( lastP2 ) );
                if ( !p1.equals ( lastP2 ) ) {
                    // this is valid for the first point with lastP2 == null and also for every start of a new contour
                    motionSeekZ ( zLiftup );
                    motionSeekXY ( p1.getX (), p1.getY () );
                    motionLinearZ ( zDepth, zFeedrate );
                }
                motionLinearXY ( p2.getX (), p2.getY (), xyFeedrate );
                lastP2 = p2;
            }
            motionSeekZ ( zLiftup );

        }
        catch ( FileNotFoundException | ParseException exc ) {

            LOG.error ( "generateGcodeCore:" + exc );

            gcodeGenerationErrorOmiited ();

            StringBuilder sb = new StringBuilder ( "File not founf " + file.getName () + "!\n\n" );
            sb.append ( "Cause:\n" );
            sb.append ( exc + "\n\n" );
            MessageDialog.openError ( shell, "Internal Error", "" + sb );

        }

        LOG.debug ( "generateGcodeCore: end" );

    }

    private String point ( Point3D p ) {

        if ( p == null ) return "null";

        return "[" + p.getX () + "," + p.getY () + "]";

    }

    @Override
    public void setControlsEnabled ( boolean enabled ) {

        millDiameterText.setEnabled ( enabled );
        spindleSpeedText.setEnabled ( enabled );
        zClearanceText.setEnabled ( enabled );
        xyFeedrateText.setEnabled ( enabled );
        zLiftupText.setEnabled ( enabled );
        zDepthText.setEnabled ( enabled );
        zFeedrateText.setEnabled ( enabled );
        drillCompensationCheckButton.setEnabled ( enabled );

    }

}
