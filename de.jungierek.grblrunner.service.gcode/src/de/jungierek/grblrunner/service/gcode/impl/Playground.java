package de.jungierek.grblrunner.service.gcode.impl;

import de.jungierek.grblrunner.service.gcode.EGrblState;

public class Playground {

    public static void main ( String [] args ) {
        
        EGrblState s1 = EGrblState.RUN;
        EGrblState s2 = EGrblState.identify ( "<Run" );
        //EGrblState s2 = EGrblState.CHECK;
        
        System.out.println ( "s1=" + s1 + " s2=" + s2 + " result=" + s1.equals ( s2 ) );
        System.out.println ( "s1=" + s1 + " s2=" + s2 + " result=" + (s1 == s2) );
/*        
        GcodePointImpl p1 = new GcodePointImpl ( 0, 0, 0 );
        GcodePointImpl p2 = new GcodePointImpl ( 1, 0, 0 );
        
        System.out.println ( "p1=" + p1 + " p2=" + p2 + " result=" + p1.equals ( p2 ) );
        
        GcodeStateImpl st1 = new GcodeStateImpl ( s1, p1, p2 );
        GcodeStateImpl st2 = new GcodeStateImpl ( s1, p1, p2 );

        System.out.println ( "st1=" + st1 + " st2=" + st2 + " result=" + st1.equals ( st2 ) );
*/        
        
    }
}