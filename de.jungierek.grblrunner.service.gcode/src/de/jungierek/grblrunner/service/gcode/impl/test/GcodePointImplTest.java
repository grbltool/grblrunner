package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungierek.grblrunner.constants.IConstant;
import de.jungierek.grblrunner.service.gcode.IGcodePoint;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;

public class GcodePointImplTest {

    private final static double DELTA = 0.0001;

    @Test
    public void testEmptyConstructor () {

        double expectedX = 0.0;
        double expectedY = 0.0;
        double expectedZ = 0.0;

        GcodePointImpl p = new GcodePointImpl ();

        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testArrayConstructor () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testXyzConstructor () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p = new GcodePointImpl ( new double [] { expectedX, expectedY, expectedZ } );

        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

        double [] cooridnates = p.getCooridnates ();
        assertEquals ( "coordinates array length is 3", 3, cooridnates.length );

    }

    @Test
    public void testEquals () {

        GcodePointImpl p1 = new GcodePointImpl ();
        GcodePointImpl p2 = new GcodePointImpl ();
        GcodePointImpl p3 = new GcodePointImpl ( 1, 2, 3 );

        assertEquals ( "null object", false, p1.equals ( null ) );
        assertEquals ( "any object", false, p1.equals ( "xyz" ) );

        assertTrue ( "p1 = p1", p1.equals ( p1 ) );
        assertTrue ( "p1 = p2", p1.equals ( p2 ) );
        assertFalse ( "p1 != p3", p1.equals ( p3 ) );

    }

    @Test
    public void testClone () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );
        IGcodePoint p2 = p1.clone ();

        assertNotSame ( "not same", p1, p2 );
        assertEquals ( "X is " + expectedX, expectedX, p2.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p2.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p2.getZ (), DELTA );

    }
    
    @Test
    public void testMin1 () {
        
        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );
        
        IGcodePoint p = p1.min ( null );
        assertNotNull ( "min ( null ) 1", p1 );
        assertNotSame ( "min ( null ) 2", p1, p );
        assertEquals ( "min ( null ) 3", p1, p );
        
    }

    @Test
    public void testMin2 () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p1.min ( p2 );

        assertNotNull ( "p1.min ( p2 ) 1", p );
        assertNotSame ( "p1.min ( p2 ) 2", p, p2 );
        assertEquals ( "p1.min ( p2 ) X 1", p.getX (), p1.getX (), DELTA );
        assertTrue ( "p1.min ( p2 ) X 2", p.getX () < p2.getX () );
        assertEquals ( "p1.min ( p2 ) Y 1", p.getY (), p1.getY (), DELTA );
        assertTrue ( "p1.min ( p2 ) Y 2", p.getY () < p2.getY () );
        assertEquals ( "p1.min ( p2 ) Z 1", p.getZ (), p1.getZ (), DELTA );
        assertTrue ( "p1.min ( p2 ) Z 2", p.getZ () < p2.getZ () );

    }

    @Test
    public void testMin3 () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p2.min ( p1 );

        assertNotNull ( "p2.min ( p1 ) 1", p );
        assertNotSame ( "p2.min ( p1 ) 2", p, p2 );
        assertEquals ( "p2.min ( p1 ) X 1", p.getX (), p1.getX (), DELTA );
        assertTrue ( "p2.min ( p1 ) X 2", p.getX () < p2.getX () );
        assertEquals ( "p2.min ( p1 ) Y 1", p.getY (), p1.getY (), DELTA );
        assertTrue ( "p2.min ( p1 ) Y 2", p.getY () < p2.getY () );
        assertEquals ( "p2.min ( p1 ) Z 1", p.getZ (), p1.getZ (), DELTA );
        assertTrue ( "p2.min ( p1 ) Z 2", p.getZ () < p2.getZ () );

    }

    @Test
    public void testMax1 () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p1.max ( null );
        assertNotNull ( "max ( null ) 1", p1 );
        assertNotSame ( "max ( null ) 2", p1, p );
        assertEquals ( "max ( null ) 3", p1, p );

    }

    @Test
    public void testMax2 () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p1.max ( p2 );

        assertNotNull ( "p1.max ( p2 ) 1", p );
        assertNotSame ( "p1.max ( p2 ) 2", p, p2 );
        assertEquals ( "p1.max ( p2 ) X 1", p.getX (), p2.getX (), DELTA );
        assertTrue ( "p1.max ( p2 ) X 2", p.getX () > p1.getX () );
        assertEquals ( "p1.max ( p2 ) Y 1", p.getY (), p2.getY (), DELTA );
        assertTrue ( "p1.max ( p2 ) Y 2", p.getY () > p1.getY () );
        assertEquals ( "p1.max ( p2 ) Z 1", p.getZ (), p2.getZ (), DELTA );
        assertTrue ( "p1.max ( p2 ) Z 2", p.getZ () > p1.getZ () );

    }

    @Test
    public void testMax3 () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p2.max ( p1 );

        assertNotNull ( "p2.max ( p1 ) 1", p );
        assertNotSame ( "p2.max ( p1 ) 2", p, p2 );
        assertEquals ( "p1.max ( p2 ) X 1", p.getX (), p2.getX (), DELTA );
        assertTrue ( "p1.max ( p2 ) X 2", p.getX () > p1.getX () );
        assertEquals ( "p1.max ( p2 ) Y 1", p.getY (), p2.getY (), DELTA );
        assertTrue ( "p1.max ( p2 ) Y 2", p.getY () > p1.getY () );
        assertEquals ( "p1.max ( p2 ) Z 1", p.getZ (), p2.getZ (), DELTA );
        assertTrue ( "p1.max ( p2 ) Z 2", p.getZ () > p1.getZ () );

    }

    @Test
    public void testAdd () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p1.add ( p2 );
        
        assertNotSame ( "add 1", p, p1 );
        assertNotSame ( "add 2", p, p2 );

        assertEquals ( "add X", 5.0, p.getX (), DELTA );
        assertEquals ( "add Y", 7.0, p.getY (), DELTA );
        assertEquals ( "add Z", 9.0, p.getZ (), DELTA );

    }

    @Test
    public void testSub () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );
        GcodePointImpl p2 = new GcodePointImpl ( 4, 5, 6 );

        IGcodePoint p = p1.sub ( p2 );

        assertNotSame ( "add 1", p, p1 );
        assertNotSame ( "add 2", p, p2 );

        assertEquals ( "add X", -3.0, p.getX (), DELTA );
        assertEquals ( "add Y", -3.0, p.getY (), DELTA );
        assertEquals ( "add Z", -3.0, p.getZ (), DELTA );

    }

    @Test
    public void testMult () {

        GcodePointImpl p1 = new GcodePointImpl ( 1, 2, 3 );

        IGcodePoint p = p1.mult ( 2.5 );

        assertNotSame ( "add 1", p, p1 );

        assertEquals ( "add X", 2.5 * 1.0, p.getX (), DELTA );
        assertEquals ( "add Y", 2.5 * 2.0, p.getY (), DELTA );
        assertEquals ( "add Z", 2.5 * 3.0, p.getZ (), DELTA );

    }

    @Test
    public void testZeroAxisNone () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.zeroAxis ( 'F' );

        assertSame ( "same", p, p1 );

        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testZeroAxisX () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.zeroAxis ( 'X' );

        assertNotSame ( "not same", p, p1 );
        assertEquals ( "X is " + 0.0, 0.0, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testZeroAxisY () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.zeroAxis ( 'Y' );

        assertNotSame ( "not same", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + 0.0, 0.0, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testZeroAxisZ () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.zeroAxis ( 'Z' );

        assertNotSame ( "not same", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + 0.0, 0.0, p.getZ (), DELTA );

    }

    @Test
    public void testAddAxisNone () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );
        GcodePointImpl p2 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.addAxis ( 'F', p2 );

        assertSame ( "same", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testAddAxisX () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );
        GcodePointImpl p2 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.addAxis ( 'X', p2 );

        assertNotSame ( "not same", p, p1 );
        assertEquals ( "X is " + 2.0 * expectedX, 2.0 * expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testAddAxisY () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );
        GcodePointImpl p2 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.addAxis ( 'Y', p2 );

        assertNotSame ( "not same", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + 2.0 * expectedY, 2.0 * expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testAddAxisZ () {

        double expectedX = 1.1;
        double expectedY = 2.2;
        double expectedZ = 3.3;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );
        GcodePointImpl p2 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.addAxis ( 'Z', p2 );

        assertNotSame ( "not same", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + 2.0 * expectedZ, 2.0 * expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testEpsilon () {
        
        GcodePointImpl p = new GcodePointImpl ( 1.0, 2.0, 3.0 );
        
        for ( double eps = 0.0; eps < GcodePointImpl.EPSILON; eps += 0.0001 ) {
            assertEquals ( "" + eps, p, new GcodePointImpl ( p.x + eps, p.y + eps, p.z + eps ) );
        }
    }

    @Test
    public void testRotateX () {

        double expectedX = 1.0;
        double expectedY = 1.0;
        double expectedZ = 1.0;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.rotate ( 'X', -90 * IConstant.ONE_DEGREE );

        assertNotSame ( "not same p1", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testRotateY () {

        double expectedX = 1.0;
        double expectedY = 1.0;
        double expectedZ = 1.0;

        GcodePointImpl p1 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.rotate ( 'Y', -90 * IConstant.ONE_DEGREE );

        assertNotSame ( "not same p1", p, p1 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }

    @Test
    public void testRotateZ () {

        double expectedX = 0.0;
        double expectedY = 1.0;
        double expectedZ = 0.0;

        GcodePointImpl p1 = new GcodePointImpl ( 1.0, 0.0, 0.0 );
        GcodePointImpl p2 = new GcodePointImpl ( expectedX, expectedY, expectedZ );

        IGcodePoint p = p1.rotate ( 'Z', -90 * IConstant.ONE_DEGREE );

        assertNotSame ( "not same p1", p, p1 );
        assertNotSame ( "not same p2", p, p2 );
        assertEquals ( "X is " + expectedX, expectedX, p.getX (), DELTA );
        assertEquals ( "Y is " + expectedY, expectedY, p.getY (), DELTA );
        assertEquals ( "Z is " + expectedZ, expectedZ, p.getZ (), DELTA );

    }


}
