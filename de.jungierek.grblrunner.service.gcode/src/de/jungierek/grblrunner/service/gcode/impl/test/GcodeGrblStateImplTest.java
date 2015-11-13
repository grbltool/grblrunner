package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.EGrblState;
import de.jungierek.grblrunner.service.gcode.impl.GcodeGrblStateImpl;
import de.jungierek.grblrunner.service.gcode.impl.GcodePointImpl;

public class GcodeGrblStateImplTest {
    
    private double delta = 0.001;

    private final double [] mc = new double [] { 1.1, 2.2, 3.3 };
    private GcodePointImpl machineCoordinates = new GcodePointImpl ( mc );

    private final double [] wc = new double [] { 4.4, 5.5, 6.6 };
    private GcodePointImpl workCoordinates = new GcodePointImpl ( wc );
    
    private GcodeGrblStateImpl underTest;

    @Before
    public void setUp () throws Exception {

        underTest = new GcodeGrblStateImpl ( EGrblState.ALARM, machineCoordinates, workCoordinates );

    }

    @Test
    public void testConstructor () {
        
        assertEquals ( "state", EGrblState.ALARM, underTest.getGrblState () );
        assertArrayEquals ( "mc", mc, underTest.getMachineCoordindates ().getCooridnates (), delta );
        assertArrayEquals ( "wc", wc, underTest.getWorkCoordindates ().getCooridnates (), delta );

    }

    @Test
    public void testEquals () {
        
        assertFalse ( "null", underTest.equals ( null ) );
        assertFalse ( "any object", underTest.equals ( new Object () ) );
        assertTrue ( "same", underTest.equals ( underTest ) );
        assertFalse ( "not equals 1", underTest.equals ( new GcodeGrblStateImpl ( EGrblState.CHECK, machineCoordinates, workCoordinates ) ) );
        assertFalse ( "not equals 2", underTest.equals ( new GcodeGrblStateImpl ( EGrblState.ALARM, workCoordinates, workCoordinates ) ) );
        assertFalse ( "not equals 3", underTest.equals ( new GcodeGrblStateImpl ( EGrblState.ALARM, machineCoordinates, machineCoordinates ) ) );

    }

}
