package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.impl.GcodeLineImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;

public class GcodeLineImplTest {

    private double delta = 0.001;

    @Test
    public void testConstructor () {
        
        int lineNo = 101;
        String line = "bla bla";
        
        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );

        checkinitialGcodeline ( l, lineNo, line );

    }

    @Test
    public void testParseGcodeBlaBla () {

        int lineNo = 101;
        String line = "bla bla";

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, null, -1, -1 );

        checkinitialGcodeline ( l, lineNo, line );

    }

    private void checkinitialGcodeline ( IGcodeLine l, int lineNo, String line ) {

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertNull ( "gcode mode", l.getGcodeMode () );
        checkinitialGcodelineWithoutMode ( l, lineNo, line );

    }

    private void checkinitialGcodelineWithoutMode ( IGcodeLine l, int lineNo, String line ) {

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertNull ( "start", l.getStart () );
        assertNull ( "end", l.getEnd () );
        assertEquals ( "feedrate", 0, l.getFeedrate () );
        assertEquals ( "radius", 0.0, l.getRadius (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertFalse ( "is processed", l.isProcessed () );
        assertFalse ( "is move in xyz", l.isMoveInXYZ () );
        assertFalse ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );
        assertFalse ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertFalse ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );

    }

    @Test
    public void testParseGcodeG0WithMove () {

        int lineNo = 101;
        String line = "G0X10Y20Z30F40";
        
        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_SEEK, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 20.0, 30.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertTrue ( "is motion mode seek", l.isMotionModeSeek () );
        assertFalse ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertTrue ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertTrue ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeG1WithPartialMoveInYZ () {

        int lineNo = 101;
        String line = "G1X1Y20Z30F40"; // !!! Coordinate with 1 char

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 1.0, 20.0, 30.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertTrue ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeG2WithPartialMoveInXY () {

        int lineNo = 101;
        String line = "G2X10Y20R23F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_CW_ARC, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 20.0, 3.0 ), l.getEnd () );
        assertEquals ( "radius", 23.0, l.getRadius (), delta );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertFalse ( "is motion mode linear", l.isMotionModeLinear () );
        assertTrue ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertTrue ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeG3WithPartialMoveInXY () {

        int lineNo = 101;
        String line = "G3X10Y20R-23F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_CCW_ARC, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 20.0, 3.0 ), l.getEnd () );
        assertEquals ( "radius", -23.0, l.getRadius (), delta );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertFalse ( "is motion mode linear", l.isMotionModeLinear () );
        assertTrue ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertTrue ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeNoG0G1G2G3MoveOnlyInX () {

        int lineNo = 101;
        String line = "X10F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, expectedY, expectedZ ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertTrue ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeNoG0G1G2G3MoveOnlyInY () {

        int lineNo = 101;
        String line = "Y10F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( expectedX, 10.0, expectedZ ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeNoG0G1G2G3MoveOnlyInZ () {

        int lineNo = 101;
        String line = "Z10F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( expectedX, expectedY, 10.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertFalse ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertTrue ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeNoG0G1G2G3FMoveOnlyInX () {

        int lineNo = 101;
        String line = "X10";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, expectedY, expectedZ ), l.getEnd () );
        assertEquals ( "feedrate", -1, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertTrue ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeNoG0G1G2G3FMoveOnlyInY () {

        int lineNo = 101;
        String line = "Y10";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( expectedX, 10.0, expectedZ ), l.getEnd () );
        assertEquals ( "feedrate", -1, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeNoG0G1G2G3FMoveOnlyInZ () {

        int lineNo = 101;
        String line = "Z10";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( expectedX, expectedY, 10.0 ), l.getEnd () );
        assertEquals ( "feedrate", -1, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertFalse ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertTrue ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testParseGcodeComment1 () {

        int lineNo = 101;
        String line = "(G1X10Y20Z30F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1, -1 );
        assertEquals ( "gcode mode", EGcodeMode.COMMENT, l.getGcodeMode () );
        checkinitialGcodelineWithoutMode ( l, lineNo, line );

    }

    @Test
    public void testParseGcodeComment2 () {

        int lineNo = 101;
        String line = ";G1X10Y20Z30F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1, -1 );
        assertEquals ( "gcode mode", EGcodeMode.COMMENT, l.getGcodeMode () );
        checkinitialGcodelineWithoutMode ( l, lineNo, line );

    }

    @Test
    public void testRotate () {

        int lineNo = 101;
        String line = "X10";

        double expectedX = 0.0;
        double expectedY = 0.0;
        double expectedZ = 0.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1, 40 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 0.0, 0.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertTrue ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

        l.rotate ( -90 * IConstant.ONE_DEGREE, lastEnd );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 0.0, 10.0, 0.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertTrue ( "is move in xyz", l.isMoveInXYZ () );
        assertTrue ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertTrue ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testMessInLineWithLastEndNull () {

        int lineNo = 101;
        String line = "bla bla";

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, null, -1, -1 );

        checkinitialGcodeline ( l, lineNo, line );

    }

    @Test
    public void testMessInLine () {

        int lineNo = 101;
        String line = "bla bla";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        int lastFeddrate = 20;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1.0, lastFeddrate );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", lastEnd, l.getEnd () );
        assertEquals ( "feedrate", lastFeddrate, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertFalse ( "is move in xyz", l.isMoveInXYZ () );
        assertFalse ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testNonMoveInX1 () {

        int lineNo = 101;
        String line = "G1X1.0Y2.0Z3.0";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        int lastFeddrate = 20;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1.0, lastFeddrate );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", lastEnd, l.getEnd () );
        assertEquals ( "feedrate", lastFeddrate, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertFalse ( "is move in xyz", l.isMoveInXYZ () );
        assertFalse ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

    @Test
    public void testNonMoveInX2 () {

        int lineNo = 101;
        String line = "G1X1.0Y2.0Z3.0";

        double expectedX = 1.0 + IGcodePoint.EPSILON - 0.00001;
        double expectedY = 2.0 + IGcodePoint.EPSILON - 0.00001;
        double expectedZ = 3.0 + IGcodePoint.EPSILON - 0.00001;

        int lastFeddrate = 20;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1.0, lastFeddrate );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        // assertEquals ( "end", lastEnd, l.getEnd () );
        assertTrue ( "end", lastEnd.equals ( l.getEnd () ) );
        assertEquals ( "feedrate", lastFeddrate, l.getFeedrate () );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );
        assertFalse ( "is motion mode seek", l.isMotionModeSeek () );
        assertTrue ( "is motion mode linear", l.isMotionModeLinear () );
        assertFalse ( "is motion mode arc", l.isMotionModeArc () );
        assertFalse ( "is move in xyz", l.isMoveInXYZ () );
        assertFalse ( "is move in xy", l.isMoveInXY () );
        assertFalse ( "is move in x", l.isMoveInX () );
        assertFalse ( "is move in y", l.isMoveInY () );
        assertFalse ( "is move in z", l.isMoveInZ () );

    }

}
