package de.cf.autoscaler.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.cf.autoscaler.tests.scalingaction.ScalingActionTestSuite;
import de.cf.autoscaler.tests.wrapper.WrapperTestSuite;

@RunWith(Suite.class)
@SuiteClasses({WrapperTestSuite.class, ScalingActionTestSuite.class})
public class MainTestSuite {

}
