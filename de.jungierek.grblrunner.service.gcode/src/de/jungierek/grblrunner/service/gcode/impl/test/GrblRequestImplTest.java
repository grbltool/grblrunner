package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.impl.GrblRequestImpl;

public class GrblRequestImplTest {

    private class GrblRequestImplTestee extends GrblRequestImpl {

        public GrblRequestImplTestee ( String message ) {
            super ( true, message );
        }

    }

    @Test
    public void testConstructor () {
        
        GrblRequestImpl grblRequest = new GrblRequestImplTestee ( "xyz" );

        assertTrue ( "suppress", grblRequest.isSuppressInTerminal () );
        assertEquals ( "message", "xyz", grblRequest.getMessage () );
        assertFalse ( "is reset", grblRequest.isReset () );
        assertFalse ( "is home", grblRequest.isHome () );
        assertFalse ( "is unock", grblRequest.isUnlock () );

    }

    @Test
    public void testConstructorReset () {

        GrblRequestImpl grblRequest = new GrblRequestImplTestee ( new String ( new byte [] { 0x18 } ) );

        assertTrue ( "suppress", grblRequest.isSuppressInTerminal () );
        // assertEquals ( "message", "xyz", grblRequest.getMessage () );
        assertTrue ( "is reset", grblRequest.isReset () );
        assertFalse ( "is home", grblRequest.isHome () );
        assertFalse ( "is unock", grblRequest.isUnlock () );

    }

    @Test
    public void testConstructorHome () {

        GrblRequestImpl grblRequest = new GrblRequestImplTestee ( "$H" );

        assertTrue ( "suppress", grblRequest.isSuppressInTerminal () );
        assertEquals ( "message", "$H", grblRequest.getMessage () );
        assertFalse ( "is reset", grblRequest.isReset () );
        assertTrue ( "is home", grblRequest.isHome () );
        assertFalse ( "is unock", grblRequest.isUnlock () );

    }

    @Test
    public void testConstructorUnlock () {

        GrblRequestImpl grblRequest = new GrblRequestImplTestee ( "$X" );

        assertTrue ( "suppress", grblRequest.isSuppressInTerminal () );
        assertEquals ( "message", "$X", grblRequest.getMessage () );
        assertFalse ( "is reset", grblRequest.isReset () );
        assertFalse ( "is home", grblRequest.isHome () );
        assertTrue ( "is unock", grblRequest.isUnlock () );

    }

}
