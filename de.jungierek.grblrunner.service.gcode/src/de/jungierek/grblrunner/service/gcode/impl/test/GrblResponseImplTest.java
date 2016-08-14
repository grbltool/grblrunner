package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.impl.GrblResponseImpl;

public class GrblResponseImplTest {

    private class GrblResponseImplTestee extends GrblResponseImpl {

        public GrblResponseImplTestee ( String message ) {
            super ( true, message );
        }

    }

    @Test
    public void testConstructor () {
        
        GrblResponseImpl grblRequest = new GrblResponseImplTestee ( "xyz" );

        assertTrue ( "suppress", grblRequest.isSuppressInTerminal () );
        assertEquals ( "message", "xyz", grblRequest.getMessage () );

    }

}
