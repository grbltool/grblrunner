package de.jungierek.grblrunner.service.gcode.impl.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ GcodeGrblStateImplTest.class, GcodeLineImplTest.class, GcodeModelImplTest.class, GcodePointImplTest.class, GcodeResponseImplTest.class, GcodeServiceImplTest.class })
public class AllTests {

}
