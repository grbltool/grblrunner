package de.jungierek.grblrunner.parts.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.GuiFactory;

public class MacroPocketGroup extends MacroGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroPocketGroup.class );

    private static final String GROUP_NAME = "Pocket";
    private static final int PART_COLS = 9;

    private Text xyFeedrateText;
    private Text spindleSpeedText;
    private Text xDimensionText;
    private Text yDimensionText;
    private Text millDiameterText;
    private Text zClearanceText;
    private Text zLiftupText;
    private Text zDepthText;
    private Text overlapText;
    private Text zFeedrateText;
    private Button cornerCompensationCheckButton;

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

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "dimension x", 1 );
        xDimensionText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.POCKET_MILL_DIMENSION ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z liftup", 1 );
        zLiftupText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.MACRO_Z_LIFTUP ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate xy", 1 );
        xyFeedrateText = GuiFactory.createIntegerText ( group, "" + IPreferences.POCKET_MILL_XY_FEEDRATE, 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "dimension y", 1 );
        yDimensionText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.POCKET_MILL_DIMENSION ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z clearance", 1 );
        zClearanceText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.Z_CLEARANCE ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "spindle speed", 1 );
        spindleSpeedText = GuiFactory.createIntegerText ( group, "" + IPreferences.MACRO_SPINDLE_SPEED, 1, true, IPreferences.SPINDLE_MIN_RPM, IPreferences.SPINDLE_MAX_RPM );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "rpm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mill diameter", 1 );
        millDiameterText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.POCKET_MILL_DIAMETER ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z depth", 1 );
        zDepthText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.POCKET_MILL_Z_DEPTH ), 1, true, -5.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate z", 1 );
        zFeedrateText = GuiFactory.createIntegerText ( group, "" + IPreferences.POCKET_MILL_Z_FEEDRATE, 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "overlap", 1 );
        overlapText = GuiFactory.createIntegerText ( group, "" + IPreferences.POCKET_MILL_OVERLAP, 1, true, 0, 49 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "%" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "corner comp.", 1 );
        cornerCompensationCheckButton = GuiFactory.createButton ( group, SWT.CHECK, null, SWT.LEFT, SWT.CENTER, true, false );
        GuiFactory.createHiddenLabel ( group );

        xyFeedrateText.addModifyListener ( textFieldModifyListener );
        zFeedrateText.addModifyListener ( textFieldModifyListener );
        spindleSpeedText.addModifyListener ( textFieldModifyListener );
        xDimensionText.addModifyListener ( textFieldModifyListener );
        yDimensionText.addModifyListener ( textFieldModifyListener );
        millDiameterText.addModifyListener ( textFieldModifyListener );
        zClearanceText.addModifyListener ( textFieldModifyListener );
        zLiftupText.addModifyListener ( textFieldModifyListener );
        zDepthText.addModifyListener ( textFieldModifyListener );
        overlapText.addModifyListener ( textFieldModifyListener );
        cornerCompensationCheckButton.addSelectionListener ( buttonSelectionListener );

    }

    @Override
    protected String getTitle () {

        return "milling pockets";

    }

    @Override
    public void generateGcodeCore ( IGcodeProgram gcodeProgram ) {
        
        LOG.debug ( "generateGcodeCore: start" );
        
        int xyFeedrate = partTools.parseIntegerField ( xyFeedrateText, IPreferences.POCKET_MILL_XY_FEEDRATE );
        int spindleSpeed = partTools.parseIntegerField ( spindleSpeedText, IPreferences.MACRO_SPINDLE_SPEED );
        double xDimension = partTools.parseDoubleField ( xDimensionText, IPreferences.POCKET_MILL_DIMENSION );
        double yDimension = partTools.parseDoubleField ( yDimensionText, IPreferences.POCKET_MILL_DIMENSION );
        double millDiameter = partTools.parseDoubleField ( millDiameterText, IPreferences.POCKET_MILL_DIAMETER );
        double zClearance = partTools.parseDoubleField ( zClearanceText, IPreferences.Z_CLEARANCE );
        double zLiftup = partTools.parseDoubleField ( zLiftupText, IPreferences.MACRO_Z_LIFTUP );
        double zDepth = partTools.parseDoubleField ( zDepthText, IPreferences.POCKET_MILL_Z_DEPTH );
        double overlap = partTools.parseIntegerField ( overlapText, IPreferences.POCKET_MILL_OVERLAP ) / 100.0;
        boolean isCornerCompensation = cornerCompensationCheckButton.getSelection ();
        int zFeedrate = partTools.parseIntegerField ( zFeedrateText, IPreferences.POCKET_MILL_Z_FEEDRATE );

        double millRadius = millDiameter / 2;
        double cornerCompensation = (1 - 1 / Math.sqrt ( 2 )) * millRadius;

        motionSeekZ ( zClearance );
        spindleOn ( spindleSpeed );

        // Contour
        // front left
        double x = millRadius;
        double y = millRadius;
        motionSeekXY ( x, y );
        motionSeekZ ( zLiftup );
        motionLinearZ ( zDepth, zFeedrate );

        // back left
        y = yDimension - millRadius;
        motionLinearXY ( x, y, xyFeedrate );
        if ( isCornerCompensation ) {
            motionLinearXY ( x - cornerCompensation, y + cornerCompensation, xyFeedrate );
            motionLinearXY ( x, y, xyFeedrate );
        }

        // back right
        x = xDimension - millRadius;
        motionLinearXY ( x, y, xyFeedrate );
        if ( isCornerCompensation ) {
            motionLinearXY ( x + cornerCompensation, y + cornerCompensation, xyFeedrate );
            motionLinearXY ( x, y, xyFeedrate );
        }

        // front right
        y = millRadius;
        motionLinearXY ( x, y, xyFeedrate );
        if ( isCornerCompensation ) {
            motionLinearXY ( x + cornerCompensation, y - cornerCompensation, xyFeedrate );
            motionLinearXY ( x, y, xyFeedrate );
        }

        // front left
        x = millRadius;
        motionLinearXY ( x, y, xyFeedrate );
        if ( isCornerCompensation ) {
            motionLinearXY ( x - cornerCompensation, y - cornerCompensation, xyFeedrate );
            motionLinearXY ( x, y, xyFeedrate );
        }
        motionSeekZ ( zLiftup );

        // inner area
        // one intervall is computed by millDiameter less 2 times the overlap
        final double distX = xDimension - 2 * millDiameter;
        int n = (int) (distX / ((1 - 2 * overlap) * millDiameter)) + 1;
        double dx = distX / n;

        final double distFromContour = millDiameter + dx / 2;
        // x = distFromContour;
        // y = distFromContour;
        x = y = distFromContour;
        motionSeekXY ( x, y );
        motionLinearZ ( zDepth, zFeedrate );
        
        int dir = +1;
        for ( int i = 1; i <= n; i++ ) {
            y = dir == 1 ? y = yDimension - distFromContour : distFromContour;
            motionLinearXY ( x, y, xyFeedrate );
            if ( i < n ) {
                x += dx;
                motionLinearXY ( x, y, xyFeedrate );
            }
            dir = -dir;
        }

        LOG.debug ( "generateGcodeCore: end" );

    }

    @Override
    public void setControlsEnabled ( boolean enabled ) {

        xDimensionText.setEnabled ( enabled );
        yDimensionText.setEnabled ( enabled );
        millDiameterText.setEnabled ( enabled );
        spindleSpeedText.setEnabled ( enabled );
        zClearanceText.setEnabled ( enabled );
        xyFeedrateText.setEnabled ( enabled );
        zLiftupText.setEnabled ( enabled );
        zDepthText.setEnabled ( enabled );
        overlapText.setEnabled ( enabled );
        zFeedrateText.setEnabled ( enabled );
        cornerCompensationCheckButton.setEnabled ( enabled );

    }

}
