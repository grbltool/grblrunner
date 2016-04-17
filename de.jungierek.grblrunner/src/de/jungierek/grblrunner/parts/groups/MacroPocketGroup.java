package de.jungierek.grblrunner.parts.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IPreferenceKey;
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
    private Button climbCheckButton;

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
        xDimensionText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z liftup", 1 );
        zLiftupText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate xy", 1 );
        xyFeedrateText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ), 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "dimension y", 1 );
        yDimensionText = GuiFactory.createDoubleText ( group, formatCoordinate ( getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

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

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "overlap", 1 );
        overlapText = GuiFactory.createIntegerText ( group, "" + getIntPreference ( IPreferenceKey.POCKET_MILL_OVERLAP ), 1, true, 0, 99 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "%" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "corner comp.", 1 );
        cornerCompensationCheckButton = GuiFactory.createButton ( group, SWT.CHECK, null, SWT.LEFT, SWT.CENTER, true, false );
        cornerCompensationCheckButton.setSelection ( true );
        GuiFactory.createHiddenLabel ( group );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "climb", 1 );
        climbCheckButton = GuiFactory.createButton ( group, SWT.CHECK, null, SWT.LEFT, SWT.CENTER, true, false );
        climbCheckButton.setSelection ( true );
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
        climbCheckButton.addSelectionListener ( buttonSelectionListener );

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
        overlapText.setText ( "" + getIntPreference ( IPreferenceKey.POCKET_MILL_OVERLAP ) );
        // cornerCompensationCheckButton.setSelection ( true );
        // climbCheckButton.setSelection ( true );

    }

    @Override
    protected String getTitle () {

        return "milling pockets";

    }

    @Override
    public void generateGcodeCore ( IGcodeProgram gcodeProgram ) {

        LOG.debug ( "generateGcodeCore: start" );

        int xyFeedrate = toolbox.parseIntegerField ( xyFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ) );
        int spindleSpeed = toolbox.parseIntegerField ( spindleSpeedText, getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ) );
        double xDimension = toolbox.parseDoubleField ( xDimensionText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) );
        double yDimension = toolbox.parseDoubleField ( yDimensionText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) );
        double millDiameter = toolbox.parseDoubleField ( millDiameterText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIAMETER ) );
        double zClearance = toolbox.parseDoubleField ( zClearanceText, getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) );
        double zLiftup = toolbox.parseDoubleField ( zLiftupText, getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) );
        double zDepth = toolbox.parseDoubleField ( zDepthText, getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) );
        double overlap = toolbox.parseIntegerField ( overlapText, getIntPreference ( IPreferenceKey.POCKET_MILL_OVERLAP ) ) / 100.0;
        boolean isCornerCompensation = cornerCompensationCheckButton.getSelection ();
        boolean isClimb = climbCheckButton.getSelection ();
        int zFeedrate = toolbox.parseIntegerField ( zFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ) );

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
        if ( millDiameter > 0.0 ) {

            final double dist = (1 - overlap) * millDiameter;
            // x = distFromContour;
            // y = distFromContour;
            double x1 = millRadius + dist;
            double y1 = millRadius + dist;
            double x2 = xDimension - millRadius - dist;
            double y2 = yDimension - millRadius - dist;

            motionSeekXY ( x1, y1 );
            motionLinearZ ( zDepth, zFeedrate );

            while ( x1 < x2 && y1 < y2 ) {

                if ( isClimb ) {
                    motionLinearXY ( x1, y2, xyFeedrate );
                    motionLinearXY ( x2, y2, xyFeedrate );
                    motionLinearXY ( x2, y1, xyFeedrate );
                    motionLinearXY ( x1 + dist, y1, xyFeedrate );
                }
                else {
                    motionLinearXY ( x2, y1, xyFeedrate );
                    motionLinearXY ( x2, y2, xyFeedrate );
                    motionLinearXY ( x1, y2, xyFeedrate );
                    motionLinearXY ( x1, y1 + dist, xyFeedrate );
                }


                x1 += dist;
                y1 += dist;
                x2 -= dist;
                y2 -= dist;

            }

        }

        LOG.debug ( "generateGcodeCore: end" );

    }

    public void generateGcodeCoreOLD ( IGcodeProgram gcodeProgram ) {
        
        LOG.debug ( "generateGcodeCore: start" );
        
        int xyFeedrate = toolbox.parseIntegerField ( xyFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_XY_FEEDRATE ) );
        int spindleSpeed = toolbox.parseIntegerField ( spindleSpeedText, getIntPreference ( IPreferenceKey.MACRO_SPINDLE_SPEED ) );
        double xDimension = toolbox.parseDoubleField ( xDimensionText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) );
        double yDimension = toolbox.parseDoubleField ( yDimensionText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIMENSION ) );
        double millDiameter = toolbox.parseDoubleField ( millDiameterText, getDoublePreference ( IPreferenceKey.POCKET_MILL_DIAMETER ) );
        double zClearance = toolbox.parseDoubleField ( zClearanceText, getDoublePreference ( IPreferenceKey.Z_CLEARANCE ) );
        double zLiftup = toolbox.parseDoubleField ( zLiftupText, getDoublePreference ( IPreferenceKey.MACRO_Z_LIFTUP ) );
        double zDepth = toolbox.parseDoubleField ( zDepthText, getDoublePreference ( IPreferenceKey.POCKET_MILL_Z_DEPTH ) );
        double overlap = toolbox.parseIntegerField ( overlapText, getIntPreference ( IPreferenceKey.POCKET_MILL_OVERLAP ) ) / 100.0;
        boolean isCornerCompensation = cornerCompensationCheckButton.getSelection ();
        int zFeedrate = toolbox.parseIntegerField ( zFeedrateText, getIntPreference ( IPreferenceKey.POCKET_MILL_Z_FEEDRATE ) );

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
        climbCheckButton.setEnabled ( enabled );

    }

}
