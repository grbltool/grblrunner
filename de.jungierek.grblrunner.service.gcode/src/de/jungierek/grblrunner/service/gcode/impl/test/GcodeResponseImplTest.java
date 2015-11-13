package de.jungierek.grblrunner.service.gcode.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.jungierek.grblrunner.service.gcode.impl.GcodeResponseImpl;

public class GcodeResponseImplTest {

    @Test
    public void testConstructor () {

        GcodeResponseImpl r = new GcodeResponseImpl ( true, "xyz" );

        assertTrue ( "suppress", r.suppressInTerminal () );
        assertEquals ( "line", "xyz", r.getLine () );
        assertFalse ( "is reset", r.isReset () );

    }

    @Test
    public void testConstructorReset () {

        GcodeResponseImpl r = new GcodeResponseImpl ( true, new String ( new byte [] { 0x18 } ) );

        assertTrue ( "suppress", r.suppressInTerminal () );
        assertEquals ( "len", 1, r.getLine ().length () );
        assertEquals ( "char at [0]", 0x18, r.line.charAt ( 0 ) );
        assertTrue ( "is reset", r.isReset () );

    }

}
