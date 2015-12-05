package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
        // l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, null, -1 );

        checkinitialGcodeline ( l, lineNo, line );

    }

    private void checkinitialGcodeline ( IGcodeLine l, int lineNo, String line ) {

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertNull ( "gcode mode", l.getGcodeMode () );
        assertNull ( "start", l.getStart () );
        assertNull ( "end", l.getEnd () );
        assertEquals ( "feedrate", 0.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertFalse ( "is motion mode", l.isMotionMode () );

    }

    @Test
    public void testParseGcodeG0 () {

        int lineNo = 101;
        String line = "G0X10Y20Z30F40";
        
        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        // l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_SEEK, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 20.0, 30.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );

    }

    @Test
    public void testParseGcodeG1 () {

        int lineNo = 101;
        String line = "G1X1Y20Z30F40"; // !!! Coordinate with 1 char

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        // l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 1.0, 20.0, 30.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );

    }

    @Test
    public void testParseGcodeNoG0G1 () {

        int lineNo = 101;
        String line = "X10F40";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        // l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, expectedY, expectedZ ), l.getEnd () );
        assertEquals ( "feedrate", 40.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );

    }

    @Test
    public void testParseGcodeNoG0G1F () {

        int lineNo = 101;
        String line = "X10";

        double expectedX = 1.0;
        double expectedY = 2.0;
        double expectedZ = 3.0;

        IGcodePoint lastEnd = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );
        // l.parseGcode ( EGcodeMode.MOTION_MODE_LINEAR, lastEnd, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.MOTION_MODE_LINEAR, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, expectedY, expectedZ ), l.getEnd () );
        assertEquals ( "feedrate", -1.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertTrue ( "is motion mode", l.isMotionMode () );

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
        // l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.COMMENT, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 20.0, 30.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertFalse ( "is motion mode", l.isMotionMode () );

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
        // l.parseGcode ( EGcodeMode.GCODE_MODE_UNDEF, lastEnd, -1 );

        assertEquals ( "lineNo", lineNo, l.getLineNo () );
        assertEquals ( "line", line, l.getLine () );

        assertEquals ( "gcode mode", EGcodeMode.COMMENT, l.getGcodeMode () );
        assertNotNull ( "start", l.getStart () );
        assertSame ( "start", lastEnd, l.getStart () );
        assertNotNull ( "end", l.getEnd () );
        assertEquals ( "end", new GcodePointImpl ( 10.0, 20.0, 30.0 ), l.getEnd () );
        assertEquals ( "feedrate", 40.0, l.getFeedrate (), delta );
        assertFalse ( "is processed", l.isProcessed () );
        assertFalse ( "is motion mode", l.isMotionMode () );

    }


    private boolean visited = false;

    @Test
    public void testVisit () {

        int lineNo = 101;
        String line = "bla bla";


        // GcodeLineImpl l = new GcodeLineImpl ( lineNo, line );

        assertFalse ( "before visit", visited );

        // l.visit ( new IGcodeModelVisitor () {
        //
        // @Override
        // public void visit ( IGcodeLine gcodeLine ) {
        // visited = true;
        // }
        // } );

        assertTrue ( "after visit", visited );

    }

}
