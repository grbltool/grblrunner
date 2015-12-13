package de.jungierek.grblrunner.parts.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungierek.grblrunner.constants.IConstants;
import de.jungierek.grblrunner.constants.IPreferences;
import de.jungierek.grblrunner.service.gcode.IGcodeProgram;
import de.jungierek.grblrunner.tools.GuiFactory;

public class MacroHobbedBoltGroup extends MacroGroup {

    private static final Logger LOG = LoggerFactory.getLogger ( MacroHobbedBoltGroup.class );

    private static final String GROUP_NAME = "Hobbed Bolt";
    private static final int PART_COLS = 9;

    private Text feedrateText;
    private Text spindleSpeedText;
    private Text angleText;
    private Text boltDiameterText;
    private Text zClearanceText;
    private Text xClearanceText;
    private Text countRetractionText;
    private Text retractionText;
    private Text waitAtTargetText;

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

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "angle", 1 );
        angleText = GuiFactory.createIntegerText ( group, "" + IPreferences.HOBBED_BOLT_ANGLE, 1, true, 0, 90 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "°" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "z liftup", 1 );
        zClearanceText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.MACRO_Z_LIFTUP ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "feedrate", 1 );
        feedrateText = GuiFactory.createIntegerText ( group, "" + IPreferences.HOBBED_BOLT_FEEDRATE, 1, true, 0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm/min" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "bolt diameter", 1 );
        boltDiameterText = GuiFactory.createIntegerText ( group, "" + IPreferences.HOBBED_BOLT_BOLT_DIAMETER, 1, true, 0, 12 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "x clearance", 1 );
        xClearanceText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.HOBBED_BOLT_X_CLEARANCE ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "spindle speed", 1 );
        spindleSpeedText = GuiFactory.createIntegerText ( group, "" + IPreferences.MACRO_SPINDLE_SPEED, 1, true, IPreferences.SPINDLE_MIN_RPM, IPreferences.SPINDLE_MAX_RPM );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "rpm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "retraction count", 1 );
        countRetractionText = GuiFactory.createIntegerText ( group, "" + IPreferences.HOBBED_BOLT_COUNT_RETRACTION, 1, true, 0, 12000 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "retraction", 1 );
        retractionText = GuiFactory.createDoubleText ( group, formatCoordinate ( IPreferences.HOBBED_BOLT_RETRACTION ), 1, true, 0.0 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "mm" );

        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "wait at target", 1 );
        waitAtTargetText = GuiFactory.createIntegerText ( group, "" + IPreferences.HOBBED_BOLT_WAIT_AT_TARGET, 1, true, 0, 10 );
        GuiFactory.createHeadingLabel ( group, SWT.LEFT, "sec" );

        angleText.addModifyListener ( textFieldModifyListener );
        boltDiameterText.addModifyListener ( textFieldModifyListener );
        spindleSpeedText.addModifyListener ( textFieldModifyListener );
        zClearanceText.addModifyListener ( textFieldModifyListener );
        xClearanceText.addModifyListener ( textFieldModifyListener );
        feedrateText.addModifyListener ( textFieldModifyListener );

    }

    @Override
    protected String getTitle () {

        return "milling hoobed bolts for a 3D printer";

    }

    @Override
    public void generateGcodeCore ( IGcodeProgram gcodeProgram ) {
        
        LOG.debug ( "generateGcodeCore: start" );
        
        int feedrate = partTools.parseIntegerField ( feedrateText, IPreferences.HOBBED_BOLT_FEEDRATE );
        int spindleSpeed = partTools.parseIntegerField ( spindleSpeedText, IPreferences.MACRO_SPINDLE_SPEED );
        int angle = partTools.parseIntegerField ( angleText, IPreferences.HOBBED_BOLT_ANGLE );
        int boltDiameter = partTools.parseIntegerField ( boltDiameterText, IPreferences.HOBBED_BOLT_BOLT_DIAMETER );
        double zLiftup = partTools.parseDoubleField ( zClearanceText, IPreferences.MACRO_Z_LIFTUP );
        double xClearance = partTools.parseDoubleField ( xClearanceText, IPreferences.HOBBED_BOLT_X_CLEARANCE );
        int countRetraction = partTools.parseIntegerField ( countRetractionText, IPreferences.HOBBED_BOLT_COUNT_RETRACTION );
        double retraction = partTools.parseDoubleField ( retractionText, IPreferences.HOBBED_BOLT_RETRACTION );
        int waitAtTarget = partTools.parseIntegerField ( waitAtTargetText, IPreferences.HOBBED_BOLT_WAIT_AT_TARGET );

        double boltRadius = (double) boltDiameter / 2;
        final double y = 0.0;

        motionSeekZ ( zLiftup );
        // motionSeekXY ( 0.0, y );
        spindleOn ( spindleSpeed );
        motionSeekXY ( -xClearance, y );
        motionSeekZ ( -boltRadius * (1 - Math.sin ( angle * IConstants.ONE_DEGREE )) );
        final double x = -boltRadius * Math.cos ( angle * IConstants.ONE_DEGREE );
        for ( int i = 0; i < countRetraction; i++ ) {
            motionLinearXY ( x, y, feedrate );
            wait ( waitAtTarget );
            motionLinearXY ( x - retraction, y, feedrate );
        }
        motionSeekXY ( -xClearance, y );

        LOG.debug ( "generateGcodeCore: end" );

    }

    @Override
    protected void setControlsEnabled ( boolean enabled ) {

        angleText.setEnabled ( enabled );
        feedrateText.setEnabled ( enabled );
        boltDiameterText.setEnabled ( enabled );
        spindleSpeedText.setEnabled ( enabled );
        zClearanceText.setEnabled ( enabled );
        xClearanceText.setEnabled ( enabled );
        countRetractionText.setEnabled ( enabled );
        retractionText.setEnabled ( enabled );
        waitAtTargetText.setEnabled ( enabled );

    }

}
