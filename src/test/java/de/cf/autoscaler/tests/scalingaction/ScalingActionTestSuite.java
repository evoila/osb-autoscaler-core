package de.cf.autoscaler.tests.scalingaction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CpuScalingActionTest.class, RamScalingActionTest.class })
public class ScalingActionTestSuite {

}
