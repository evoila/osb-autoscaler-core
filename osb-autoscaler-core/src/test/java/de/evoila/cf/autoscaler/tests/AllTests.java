package de.evoila.cf.autoscaler.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.evoila.cf.autoscaler.tests.wrapper.WrapperTestSuite;

@RunWith(Suite.class)
@SuiteClasses({WrapperTestSuite.class})
public class AllTests {

}
