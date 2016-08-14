package de.jungierek.grblrunner.service.gcode.impl.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// @formatter:off
@RunWith ( Suite.class )
@SuiteClasses({ 
    
    GcodeGrblStateImplTest.class, 
    GcodeLineImplTest.class,
    GcodePointImplTest.class,
    GcodeProgramImplTest.class,
    GcodeServiceImplTest.class,
    GrblMessageTest.class,
    GrblRequestImplTest.class, 
    GrblResponseImplTest.class, 

})
//@formatter:on

public class AllTests {

}
