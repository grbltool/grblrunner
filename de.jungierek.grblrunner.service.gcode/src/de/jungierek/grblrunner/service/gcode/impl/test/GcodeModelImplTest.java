package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.EGcodeMode;
import de.jungierek.grblrunner.service.gcode.IGcodeLine;
import de.jungierek.grblrunner.service.gcode.IGcodeModelVisitor;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.impl.GcodeModelImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;

public class GcodeModelImplTest {

    private double delta = 0.001;

    private GcodeModelImpl underTest;

    @Before
    public void setUp () {

        underTest = new GcodeModelImpl ();

    }

    @Test
    public void testDefaultConstructor () {

        assertNull ( "min", underTest.getMin () );
        assertNull ( "max", underTest.getMax () );
        assertEquals ( "line count", 0, underTest.getLineCount () );
        assertEquals ( "step width x", 0, underTest.getStepWidthX (), delta );
        assertEquals ( "step width y", 0, underTest.getStepWidthY (), delta );
        assertNull ( "matrix", underTest.getScanMatrix () );
        assertEquals ( "shift", new GcodePointImpl ( 0.0, 0.0, 0.0 ), underTest.getShift () );

    }

    @Test
    public void testAppendGcodeLine () {
        
        String lines [] = { "G0X0Y0Z0", "X4", "Y4", "X0", "Y0", };

        for ( String l : lines ) {
            underTest.appendGcodeLine ( l );
        }

        assertEquals ( "line count", 5, underTest.getLineCount () );
        assertNull ( "min", underTest.getMin () );
        assertNull ( "max", underTest.getMax () );

        underTest.visit ( new IGcodeModelVisitor () {
            
            private int i = 0;

            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                assertEquals ( "line i=" + i, lines[i], gcodeLine.getLine () );
                assertNull ( "state i=" + i, gcodeLine.getGcodeMode () );
                assertNull ( "start i=" + i, gcodeLine.getStart () );
                assertNull ( "end i=" + i, gcodeLine.getEnd () );
                assertEquals ( "feedrate i=" + i, 0, gcodeLine.getFeedrate () );
                i++;
            }

        } );

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
        GcodePointImpl p40 = new GcodePointImpl ( 4.0, 0.0, 0.0 );
        GcodePointImpl p04 = new GcodePointImpl ( 0.0, 4.0, 0.0 );
        GcodePointImpl p44 = new GcodePointImpl ( 4.0, 4.0, 0.0 );

        IGcodePoint [] expectedStart = new IGcodePoint [] { p00, p00, p40, p44, p04 };
        IGcodePoint [] expectedEnd = new IGcodePoint [] { p00, p40, p44, p04, p00 };

        testAppendGcodeLine ();

        underTest.parseGcode ();

        assertEquals ( "line count", 5, underTest.getLineCount () );

        assertEquals ( "min", p00, underTest.getMin () );
        assertEquals ( "max", p44, underTest.getMax () );

        underTest.visit ( new IGcodeModelVisitor () {

            private int i = 0;

            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                assertEquals ( "line i=" + i, EGcodeMode.MOTION_MODE_SEEK, gcodeLine.getGcodeMode () );
                assertEquals ( "start i=" + i, expectedStart[i], gcodeLine.getStart () );
                assertEquals ( "end i=" + i, expectedEnd[i], gcodeLine.getEnd () );
                assertEquals ( "feedrate i=" + i, 0, gcodeLine.getFeedrate () );
                i++;
            }

        } );

    }

    @Test
    public void testPrepareAutolevelScan () {

        testParseGcode ();

        underTest.prepareAutolevelScan ( 4, 8 );

        assertEquals ( "step width x", 1.0, underTest.getStepWidthX (), delta );
        assertEquals ( "step width y", 0.5, underTest.getStepWidthY (), delta );

        IGcodePoint [][] m = underTest.getScanMatrix ();

        assertEquals ( "dim x", 5, m.length );
        assertEquals ( "dim y", 9, m[0].length );

        assertEquals ( "(0,0)", new GcodePointImpl ( 0.0, 0.0, 0.0 ), m[0][0] );
        assertEquals ( "(1,1)", new GcodePointImpl ( 1.0, 0.5, 0.0 ), m[1][1] );

    }

    @Test
    public void testSetProbePoint () {

        testPrepareAutolevelScan ();

        underTest.setProbePoint ( new GcodePointImpl ( 2.0, 3.5, -0.5 ) );
        
        assertEquals ( "probe", 0.0, underTest.getScanMatrix ()[3][4].getZ (), delta );
        assertEquals ( "probe", -0.5, underTest.getScanMatrix ()[2][7].getZ (), delta );

    }

    @Test
    public void testSetShift () {

        GcodePointImpl p = new GcodePointImpl ( 1.0, 2.0, 3.0 );

        underTest.setShift ( p );
        assertSame ( "same1", p, underTest.getShift () );

        underTest.setShift ( null );
        assertSame ( "same2", p, underTest.getShift () );

    }


    @Test
    public void testResetProcessed () {

        testAppendGcodeLine ();
        assertEquals ( "line no", 5, underTest.getLineCount () );

        underTest.visit ( new IGcodeModelVisitor () {
            private int i = 0;
            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                assertFalse ( "befor i=" + i, gcodeLine.isProcessed () );
                gcodeLine.setProcessed ( true );
            }
        } );

        underTest.visit ( new IGcodeModelVisitor () {
            private int i = 0;
            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                assertTrue ( "after set i=" + i, gcodeLine.isProcessed () );
            }
        } );

        underTest.clear ();

        underTest.visit ( new IGcodeModelVisitor () {
            private int i = 0;
            @Override
            public void visit ( IGcodeLine gcodeLine ) {
                assertFalse ( "after clear i=" + i, gcodeLine.isProcessed () );
            }
        } );

    }

    private void initProbes () {
        /* formatter:off */
        GcodePointImpl zProbes [] = { 
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
        /* formatter:on */

        for ( GcodePointImpl p : zProbes ) {
            underTest.setProbePoint ( p );
        }
    }

    /* @formatter:off */
    Object tests [][] = new Object [] [] {
            
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

    };
    /* @formatter:on */

    @SuppressWarnings("unused")
    @Test
    public void testInterpolateLine () {
    
        testParseGcode ();
    
        underTest.prepareAutolevelScan ( 4, 4 );
    
        assertEquals ( "step width x", 1.0, underTest.getStepWidthX (), delta );
        assertEquals ( "step width y", 1.0, underTest.getStepWidthY (), delta );
    
        IGcodePoint [][] m = underTest.getScanMatrix ();
    
        assertEquals ( "dim x", 5, m.length );
        assertEquals ( "dim y", 5, m[0].length );
    
        assertEquals ( "(0,0)", new GcodePointImpl ( 0.0, 0.0, 0.0 ), m[0][0] );
        assertEquals ( "(1,1)", new GcodePointImpl ( 1.0, 1.0, 0.0 ), m[1][1] );
    
        initProbes ();

        for ( int i = 0; i < tests.length; i++ ) {

            int j = 0;
            final Object name = tests[i][j++];
            final IGcodePoint p1 = (IGcodePoint) tests[i][j++];
            final IGcodePoint p2 = (IGcodePoint) tests[i][j++];
            final Object expectedPathLength = tests[i][j++];

            IGcodePoint [] path = underTest.interpolateLine ( p1, p2 );

            assertEquals ( name + " len", expectedPathLength, path.length );
            assertEquals ( name + " p1", p1, path[0].zeroAxis ( 'Z' ) );
            assertEquals ( name + " p2", p2, path[path.length - 1].zeroAxis ( 'Z' ) );

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
