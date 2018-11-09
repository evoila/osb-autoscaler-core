package de.evoila.cf.autoscaler.tests;

import de.evoila.cf.autoscaler.tests.scalingaction.ScalingActionTestSuite;
import de.evoila.cf.autoscaler.tests.wrapper.WrapperTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({WrapperTestSuite.class, ScalingActionTestSuite.class})
public class MainTestSuite {

}
