package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.impl.GrblMessage;

public class GrblMessageTest {

    @Test
    public void testConstructor () {

        GrblMessage grblMessage = new GrblMessage ( true, "bla bla" ) {

            @Override
            protected String getToStringName () {
                return "message_name";
            }

        };

        assertTrue ( "suppress", grblMessage.isSuppressInTerminal () );
        assertTrue ( "string", grblMessage.toString ().startsWith ( "message_name" ) );
        assertEquals ( "message", "bla bla", grblMessage.getMessage () );

    }

}
