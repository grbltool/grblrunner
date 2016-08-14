package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodeProgramImpl;

public class GcodeProgramImplTest {

    private double delta = 0.001;

    private GcodeProgramImpl underTest;

    @Before
    public void setUp () {

        underTest = new GcodeProgramImpl ();

    }

    @Test
    public void testDefaultConstructor () {

        assertNull ( "min", underTest.getMin () );
        assertNull ( "max", underTest.getMax () );
        assertEquals ( "line count", 0, underTest.getLineCount () );
        assertEquals ( "steps x", 1, underTest.getXSteps () );
        assertEquals ( "steps y", 1, underTest.getYSteps () );
        assertEquals ( "step width x", 0.0, underTest.getStepWidthX (), delta );
        assertEquals ( "step width y", 0.0, underTest.getStepWidthY (), delta );
        assertEquals ( "duration", 0, underTest.getDuration () );
        assertNull ( "program file", underTest.getGcodeProgramFile () );
        assertNull ( "probe file", underTest.getAutolevelDataFile () );
        assertEquals ( "rotation angle", 0.0, underTest.getRotationAngle (), delta );
        assertFalse ( "optimized", underTest.isOptimized () );
        assertFalse ( "loaded", underTest.isLoaded () );
        assertFalse ( "playing", underTest.isPlaying () );
        assertFalse ( "autolevel prepared", underTest.isAutolevelScanPrepared () );
        assertFalse ( "autolevel running", underTest.isAutolevelScan () );
        assertFalse ( "autolevel completed", underTest.isAutolevelScanComplete () );

    }

    @Test
    public void testAppendGcodeLine () {
        
        String lines [] = { "G0X0Y0Z0", "X4Z1", "Y4Z3", "X0Z2", "Y0Z0", };

        for ( String l : lines ) {
            underTest.appendLine ( l );
        }

        assertEquals ( "line count", 5, underTest.getLineCount () );
        assertNull ( "min", underTest.getMin () );
        assertNull ( "max", underTest.getMax () );
        
        IGcodeLine [] allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines[i];
            assertEquals ( "line i=" + i, lines[i], gcodeLine.getLine () );
            assertNull ( "state i=" + i, gcodeLine.getGcodeMode () );
            assertNull ( "start i=" + i, gcodeLine.getStart () );
            assertNull ( "end i=" + i, gcodeLine.getEnd () );
            assertEquals ( "feedrate i=" + i, 0, gcodeLine.getFeedrate () );
        }
        

    }
    
    @Test
    public void testParse () {
        
        final String lines [] = { "G0X0Y0Z0", "G1X4Z1F100", "Y4Z3", "X0Z2", "Y0Z0", };

        //@formatter:off
        final Object expected [] [] = new Object [] [] {
                { EGcodeMode.MOTION_MODE_SEEK, new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ), 0 },
                { EGcodeMode.MOTION_MODE_LINEAR, new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 4.0, 0.0, 1.0 ), 100 },
                { EGcodeMode.MOTION_MODE_LINEAR, new GcodePointImpl ( 4.0, 0.0, 1.0 ), new GcodePointImpl ( 4.0, 4.0, 3.0 ), 100 },
                { EGcodeMode.MOTION_MODE_LINEAR, new GcodePointImpl ( 4.0, 4.0, 3.0 ), new GcodePointImpl ( 0.0, 4.0, 2.0 ), 100 },
                { EGcodeMode.MOTION_MODE_LINEAR, new GcodePointImpl ( 0.0, 4.0, 2.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ), 100 },
        };
        //@formatter:on

        assertEquals ( "expected count", 5, expected.length );
        assertEquals ( "lines count", 5, lines.length );

        for ( String l : lines ) {
            underTest.appendLine ( l );
        }

        underTest.parse ();
        assertEquals ( "line count", 5, underTest.getLineCount () );

        IGcodeLine [] allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines [i];
            int j = 0;
            assertEquals ( "line i=" + i, lines[i], gcodeLine.getLine () );
            assertEquals ( "state i=" + i, expected[i][j++], gcodeLine.getGcodeMode () );
            assertEquals ( "start i=" + i, expected[i][j++], gcodeLine.getStart () );
            assertEquals ( "end i=" + i, expected[i][j++], gcodeLine.getEnd () );
            assertEquals ( "feedrate i=" + i, expected[i][j++], gcodeLine.getFeedrate () );
        }

    }

    @Test
    public void testMinMax () {
        
        String lines [] = { "G0X1Y2Z0", "X4Z1", "Y4Z3", "X1Z2", "Y2Z0", };

        for ( String l : lines ) {
            underTest.appendLine ( l );
        }

        underTest.parse ();

        assertEquals ( "min", new GcodePointImpl ( 1.0, 2.0, 0.0 ), underTest.getMin () );
        assertEquals ( "max", new GcodePointImpl ( 4.0, 4.0, 3.0 ), underTest.getMax () );

    }

    @Test
    public void testClear () {

        testAppendGcodeLine ();

        underTest.clear ();

        assertEquals ( "line count", 0, underTest.getLineCount () );

    }

    @Test
    public void testParseGcode () {

        GcodePointImpl p00 = new GcodePointImpl ( 0.0, 0.0, 0.0 );
        GcodePointImpl p40 = new GcodePointImpl ( 4.0, 0.0, 1.0 );
        GcodePointImpl p04 = new GcodePointImpl ( 0.0, 4.0, 2.0 );
        GcodePointImpl p44 = new GcodePointImpl ( 4.0, 4.0, 3.0 );

        IGcodePoint [] expectedStart = new IGcodePoint [] { p00, p00, p40, p44, p04 };
        IGcodePoint [] expectedEnd = new IGcodePoint [] { p00, p40, p44, p04, p00 };

        testAppendGcodeLine ();

        underTest.parse ();

        assertEquals ( "line count", 5, underTest.getLineCount () );

        assertEquals ( "min", p00, underTest.getMin () );
        assertEquals ( "max", p44, underTest.getMax () );

        IGcodeLine [] allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines[i];
            assertEquals ( "line i=" + i, EGcodeMode.MOTION_MODE_SEEK, gcodeLine.getGcodeMode () );
            assertEquals ( "start i=" + i, expectedStart[i], gcodeLine.getStart () );
            assertEquals ( "end i=" + i, expectedEnd[i], gcodeLine.getEnd () );
            assertEquals ( "feedrate i=" + i, 0, gcodeLine.getFeedrate () );
        }

    }

    @Test
    public void testRotate () {

        GcodePointImpl p00 = new GcodePointImpl ( 0.0, 0.0, 0.0 );
        GcodePointImpl p40 = new GcodePointImpl ( 0.0, -4.0, 1.0 );
        GcodePointImpl p04 = new GcodePointImpl ( 4.0, 0.0, 2.0 );
        GcodePointImpl p44 = new GcodePointImpl ( 4.0, -4.0, 3.0 );

        IGcodePoint [] expectedStart = new IGcodePoint [] { p00, p00, p40, p44, p04 };
        IGcodePoint [] expectedEnd = new IGcodePoint [] { p00, p40, p44, p04, p00 };

        testAppendGcodeLine ();

        underTest.parse ();
        underTest.rotate ( 90 );

        assertEquals ( "line count", 5, underTest.getLineCount () );

        assertEquals ( "min", new GcodePointImpl ( 0.0, -4.0, 0.0 ), underTest.getMin () );
        assertEquals ( "max", new GcodePointImpl ( 4.0, 0.0, 3.0 ), underTest.getMax () );

        IGcodeLine [] allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines[i];
            assertEquals ( "line i=" + i, EGcodeMode.MOTION_MODE_SEEK, gcodeLine.getGcodeMode () );
            assertEquals ( "start i=" + i, expectedStart[i], gcodeLine.getStart () );
            assertEquals ( "end i=" + i, expectedEnd[i], gcodeLine.getEnd () );
            assertEquals ( "feedrate i=" + i, 0, gcodeLine.getFeedrate () );
        }

    }

    @Test
    public void testPrepareAutolevelScan () {

        testParseGcode ();

        underTest.prepareAutolevelScan ( 4, 8 );

        assertTrue ( "prepared", underTest.isAutolevelScanPrepared () );

        assertEquals ( "steps x", 4, underTest.getXSteps () );
        assertEquals ( "steps y", 8, underTest.getYSteps () );

        assertEquals ( "step width x", 1.0, underTest.getStepWidthX (), delta );
        assertEquals ( "step width y", 0.5, underTest.getStepWidthY (), delta );

        assertEquals ( "num points", (4 + 1) * (8 + 1), underTest.getNumProbePoints () );

        assertEquals ( "(0,0)", new GcodePointImpl ( 0.0, 0.0, 0.0 ), underTest.getProbePointAt ( 0, 0 ) );
        assertEquals ( "(1,1)", new GcodePointImpl ( 1.0, 0.5, 0.0 ), underTest.getProbePointAt ( 1, 1 ) );

    }

    @Test
    public void testSetProbePoint () {

        testPrepareAutolevelScan ();

        underTest.setProbePoint ( new GcodePointImpl ( 2.0, 3.5, -0.5 ) );
        
        assertEquals ( "probe", 0.0, underTest.getProbePointAt ( 3, 4 ).getZ (), delta );
        assertEquals ( "probe", -0.5, underTest.getProbePointAt ( 2, 7 ).getZ (), delta );

    }

    @Test
    public void testResetProcessed () {

        testAppendGcodeLine ();
        assertEquals ( "line no", 5, underTest.getLineCount () );

        IGcodeLine [] allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines[i];
            assertFalse ( "before i=" + i, gcodeLine.isProcessed () );
            gcodeLine.setProcessed ( true );
        }

        allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines[i];
            assertTrue ( "after set i=" + i, gcodeLine.isProcessed () );
        }

        underTest.clear ();

        allGcodeLines = underTest.getAllGcodeLines ();
        for ( int i = 0; i < allGcodeLines.length; i++ ) {
            IGcodeLine gcodeLine = allGcodeLines[i];
            assertFalse ( "after clear i=" + i, gcodeLine.isProcessed () );
        }

    }

    /* @formatter:off */
    static final private GcodePointImpl Z_PROBES [] = { 
        new GcodePointImpl ( 0.0, 0.0, 1.0 ),
        new GcodePointImpl ( 0.0, 1.0, 0.75 ),
        new GcodePointImpl ( 0.0, 2.0, 0.5 ),
        new GcodePointImpl ( 0.0, 3.0, 0.25 ),
        new GcodePointImpl ( 0.0, 4.0, 0.0 ),
        
        new GcodePointImpl ( 1.0, 0.0, 0.75 ),
        new GcodePointImpl ( 1.0, 1.0, 0.625 ),
        new GcodePointImpl ( 1.0, 2.0, 0.5 ),
        new GcodePointImpl ( 1.0, 3.0, 0.375 ),
        new GcodePointImpl ( 1.0, 4.0, 0.25 ),

        new GcodePointImpl ( 2.0, 0.0, 0.5 ),
        new GcodePointImpl ( 2.0, 1.0, 0.5 ),
        new GcodePointImpl ( 2.0, 2.0, 0.5 ),
        new GcodePointImpl ( 2.0, 3.0, 0.5 ),
        new GcodePointImpl ( 2.0, 4.0, 0.5 ),

        new GcodePointImpl ( 3.0, 0.0, 0.25 ),
        new GcodePointImpl ( 3.0, 1.0, 0.375 ),
        new GcodePointImpl ( 3.0, 2.0, 0.5 ),
        new GcodePointImpl ( 3.0, 3.0, 0.625 ),
        new GcodePointImpl ( 3.0, 4.0, 0.75 ),

        new GcodePointImpl ( 4.0, 0.0, 0.0 ),
        new GcodePointImpl ( 4.0, 1.0, 0.25 ),
        new GcodePointImpl ( 4.0, 2.0, 0.5 ),
        new GcodePointImpl ( 4.0, 3.0, 0.75 ),
        new GcodePointImpl ( 4.0, 4.0, 1.0 ),
    };

    static final private Object INTERPOLATE_TESTS [][] = new Object [] [] {
            
            { "straight right aligned",          new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 4.0, 0.0, 0.0 ), 5 },
            { "straight up aligned",             new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 4.0, 0.0 ), 5 },
            { "straight left aligned",           new GcodePointImpl ( 4.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ), 5 },
            { "straight down aligned",           new GcodePointImpl ( 0.0, 4.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ), 5 },

            { "straight right not aligned",      new GcodePointImpl ( 0.5, 0.5, 0.0 ), new GcodePointImpl ( 3.5, 0.5, 0.0 ), 5 },
            { "straight up not aligned",         new GcodePointImpl ( 0.5, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 3.5, 0.0 ), 5 },
            { "straight left not aligned",       new GcodePointImpl ( 3.5, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 0.5, 0.0 ), 5 },
            { "straight down not aligned",       new GcodePointImpl ( 0.5, 3.5, 0.0 ), new GcodePointImpl ( 0.5, 0.5, 0.0 ), 5 },

            { "diagnoal right up aligned",       new GcodePointImpl ( 0.0, 0.0, 0.0 ), new GcodePointImpl ( 4.0, 4.0, 0.0 ), 5 },
            { "diagnoal right up not aligned",   new GcodePointImpl ( 0.5, 0.5, 0.0 ), new GcodePointImpl ( 3.5, 3.5, 0.0 ), 5 },

            { "diagnoal left up aligned",        new GcodePointImpl ( 4.0, 0.0, 0.0 ), new GcodePointImpl ( 0.0, 4.0, 0.0 ), 5 },
            { "diagnoal left up not aligned",    new GcodePointImpl ( 3.5, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 3.5, 0.0 ), 5 },

            { "diagnoal right down aligned",     new GcodePointImpl ( 0.0, 4.0, 0.0 ), new GcodePointImpl ( 4.0, 0.0, 0.0 ), 5 },
            { "diagnoal right down not aligned", new GcodePointImpl ( 0.5, 3.5, 0.0 ), new GcodePointImpl ( 3.5, 0.5, 0.0 ), 5 },

            { "diagnoal left down aligned",      new GcodePointImpl ( 4.0, 4.0, 0.0 ), new GcodePointImpl ( 0.0, 0.0, 0.0 ), 5 },
            { "diagnoal left down not aligned",  new GcodePointImpl ( 3.5, 3.5, 0.0 ), new GcodePointImpl ( 0.5, 0.5, 0.0 ), 5 },

            { "no crossing",                     new GcodePointImpl ( 0.3, 0.5, 0.0 ), new GcodePointImpl ( 0.5, 0.7, 0.0 ), 2 },
            { "irregular",                       new GcodePointImpl ( 0.3, 0.5, 0.0 ), new GcodePointImpl ( 2.15, 1.78, 0.0 ), 5 },
            { "no move inside cell",             new GcodePointImpl ( 0.3, 0.5, 0.0 ), new GcodePointImpl ( 0.3, 0.5, 0.0 ), 2 },
            { "no move on cross",                new GcodePointImpl ( 1.0, 1.0, 0.0 ), new GcodePointImpl ( 1.0, 1.0, 0.0 ), 2 },

    };
    
    static final private GcodePointImpl EXPECTED_PATH [] [] = new GcodePointImpl[][] {

        { new GcodePointImpl ( 0.0, 0.0, 1.000 ), new GcodePointImpl ( 1.0, 0.0, 0.750 ), new GcodePointImpl ( 2.0, 0.0, 0.500 ), new GcodePointImpl ( 3.0, 0.0, 0.250 ), new GcodePointImpl ( 4.0, 0.0, 0.000 ) },
        { new GcodePointImpl ( 0.0, 0.0, 1.000 ), new GcodePointImpl ( 0.0, 1.0, 0.750 ), new GcodePointImpl ( 0.0, 2.0, 0.500 ), new GcodePointImpl ( 0.0, 3.0, 0.250 ), new GcodePointImpl ( 0.0, 4.0, 0.000 ) },
        { new GcodePointImpl ( 4.0, 0.0, 0.000 ), new GcodePointImpl ( 3.0, 0.0, 0.250 ), new GcodePointImpl ( 2.0, 0.0, 0.500 ), new GcodePointImpl ( 1.0, 0.0, 0.750 ), new GcodePointImpl ( 0.0, 0.0, 1.000 ) },
        { new GcodePointImpl ( 0.0, 4.0, 0.000 ), new GcodePointImpl ( 0.0, 3.0, 0.250 ), new GcodePointImpl ( 0.0, 2.0, 0.500 ), new GcodePointImpl ( 0.0, 1.0, 0.750 ), new GcodePointImpl ( 0.0, 0.0, 1.000 ) },
        
        { new GcodePointImpl ( 0.5, 0.5, 0.781 ), new GcodePointImpl ( 1.0, 0.5, 0.687 ), new GcodePointImpl ( 2.0, 0.5, 0.500 ), new GcodePointImpl ( 3.0, 0.5, 0.312 ), new GcodePointImpl ( 3.5, 0.5, 0.219 ) },
        { new GcodePointImpl ( 0.5, 0.5, 0.781 ), new GcodePointImpl ( 0.5, 1.0, 0.687 ), new GcodePointImpl ( 0.5, 2.0, 0.500 ), new GcodePointImpl ( 0.5, 3.0, 0.312 ), new GcodePointImpl ( 0.5, 3.5, 0.219 ) },
        { new GcodePointImpl ( 3.5, 0.5, 0.219 ), new GcodePointImpl ( 3.0, 0.5, 0.312 ), new GcodePointImpl ( 2.0, 0.5, 0.500 ), new GcodePointImpl ( 1.0, 0.5, 0.687 ), new GcodePointImpl ( 0.5, 0.5, 0.781 ) },
        { new GcodePointImpl ( 0.5, 3.5, 0.219 ), new GcodePointImpl ( 0.5, 3.0, 0.312 ), new GcodePointImpl ( 0.5, 2.0, 0.500 ), new GcodePointImpl ( 0.5, 1.0, 0.687 ), new GcodePointImpl ( 0.5, 0.5, 0.781 ) },
        
        { new GcodePointImpl ( 0.0, 0.0, 1.000 ), new GcodePointImpl ( 1.0, 1.0, 0.625 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 3.0, 3.0, 0.625 ), new GcodePointImpl ( 4.0, 4.0, 1.001 ) },
        { new GcodePointImpl ( 0.5, 0.5, 0.781 ), new GcodePointImpl ( 1.0, 1.0, 0.625 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 3.0, 3.0, 0.625 ), new GcodePointImpl ( 3.5, 3.5, 0.782 ) },
        
        { new GcodePointImpl ( 4.0, 0.0, 0.000 ), new GcodePointImpl ( 3.0, 1.0, 0.375 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 1.0, 3.0, 0.375 ), new GcodePointImpl ( 0.0, 4.0, 0.000 ) },
        { new GcodePointImpl ( 3.5, 0.5, 0.219 ), new GcodePointImpl ( 3.0, 1.0, 0.375 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 1.0, 3.0, 0.375 ), new GcodePointImpl ( 0.5, 3.5, 0.219 ) },
        
        { new GcodePointImpl ( 0.0, 4.0, 0.000 ), new GcodePointImpl ( 1.0, 3.0, 0.375 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 3.0, 1.0, 0.375 ), new GcodePointImpl ( 4.0, 0.0, 0.000 ) },
        { new GcodePointImpl ( 0.5, 3.5, 0.219 ), new GcodePointImpl ( 1.0, 3.0, 0.375 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 3.0, 1.0, 0.375 ), new GcodePointImpl ( 3.5, 0.5, 0.219 ) },
        
        { new GcodePointImpl ( 4.0, 4.0, 1.001 ), new GcodePointImpl ( 3.0, 3.0, 0.625 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 1.0, 1.0, 0.625 ), new GcodePointImpl ( 0.0, 0.0, 1.000 ) },
        { new GcodePointImpl ( 3.5, 3.5, 0.782 ), new GcodePointImpl ( 3.0, 3.0, 0.625 ), new GcodePointImpl ( 2.0, 2.0, 0.500 ), new GcodePointImpl ( 1.0, 1.0, 0.625 ), new GcodePointImpl ( 0.5, 0.5, 0.781 ) },
        
        { new GcodePointImpl ( 0.3, 0.5, 0.818 ), new GcodePointImpl ( 0.5, 0.7, 0.743 ) },
        { new GcodePointImpl ( 0.3, 0.5, 0.818 ), new GcodePointImpl ( 1.0, 0.984, 0.627 ), new GcodePointImpl ( 1.023, 1.0, 0.622 ), new GcodePointImpl ( 2.0, 1.676, 0.500 ), new GcodePointImpl ( 2.150, 1.780, 0.496 ) },
        { new GcodePointImpl ( 0.3, 0.5, 0.818 ), new GcodePointImpl ( 0.3, 0.5, 0.818 ) },
        { new GcodePointImpl ( 1.0, 1.0, 0.625 ), new GcodePointImpl ( 1.0, 1.0, 0.625 ) },
        
    };
    /* @formatter:on */

    @SuppressWarnings("unused")
    @Test
    public void testInterpolateLine () {
    
        String lines [] = { "G0X0Y0Z0", "X4", "Y4", "X0", "Y0", };
        for ( String l : lines ) {
            underTest.appendLine ( l );
        }
        underTest.parse ();
        underTest.prepareAutolevelScan ( 4, 4 );
    
        assertEquals ( "step width x", 1.0, underTest.getStepWidthX (), delta );
        assertEquals ( "step width y", 1.0, underTest.getStepWidthY (), delta );
    
        assertEquals ( "steps x", 4, underTest.getXSteps () );
        assertEquals ( "steps y", 4, underTest.getYSteps () );
        assertEquals ( "points", (4 + 1) * (4 + 1), underTest.getNumProbePoints () );
    
        assertEquals ( "(0,0)", new GcodePointImpl ( 0.0, 0.0, 0.0 ), underTest.getProbePointAt ( 0, 0 ) );
        assertEquals ( "(1,1)", new GcodePointImpl ( 1.0, 1.0, 0.0 ), underTest.getProbePointAt ( 1, 1 ) );
        assertEquals ( "(4,4)", new GcodePointImpl ( 4.0, 4.0, 0.0 ), underTest.getProbePointAt ( 4, 4 ) );
    
        for ( GcodePointImpl p : Z_PROBES ) {
            underTest.setProbePoint ( p );
        }

        for ( int i = 0; i < INTERPOLATE_TESTS.length; i++ ) {

            int j = 0;
            final Object name = INTERPOLATE_TESTS[i][j++];
            final IGcodePoint p1 = (IGcodePoint) INTERPOLATE_TESTS[i][j++];
            final IGcodePoint p2 = (IGcodePoint) INTERPOLATE_TESTS[i][j++];
            final Integer expectedPathLength = (Integer) INTERPOLATE_TESTS[i][j++];

            IGcodePoint [] path = underTest.interpolateLine ( p1, p2 );

            // TODO_TEST better proof of interpolation
            if ( expectedPathLength != path.length ) {
                System.out.println ( name + ": len=" + path.length + " p1=" + p1 + " p2=" + p2 );
                for ( int k = 0; k < path.length; k++ ) {
                    System.out.println ( "k=" + k + " p=" + path[k] );
                }
                System.out.println ( "-------------------------" );
            }

            assertEquals ( name + " len", expectedPathLength.intValue (), path.length );
            assertEquals ( name + " p1", p1, path[0].zeroAxis ( 'Z' ) );
            assertEquals ( name + " p2", p2, path[path.length - 1].zeroAxis ( 'Z' ) );

            // Test interpolated Pathes
            if ( true ) {
                GcodePointImpl [] expectedPath = EXPECTED_PATH[i];
                assertEquals ( name + " path", expectedPathLength.intValue (), expectedPath.length );
                for ( int k = 0; k < expectedPath.length; k++ ) {
                    assertEquals ( name + " path k=" + k, expectedPath[k], path[k] );
                }
            }

            // TODO_TEST better proof of interpolation
            if ( false ) {
                System.out.println ( name + ": len=" + path.length + " p1=" + p1 + " p2=" + p2 );
                for ( int k = 0; k < path.length; k++ ) {
                    System.out.println ( "k=" + k + " p=" + path[k] );
                }
                System.out.println ( "-------------------------" );
            }

        }
    }

}
