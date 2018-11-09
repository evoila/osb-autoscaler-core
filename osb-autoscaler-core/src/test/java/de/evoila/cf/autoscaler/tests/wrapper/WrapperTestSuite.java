package de.evoila.cf.autoscaler.tests.wrapper;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CpuWrapperTest.class, LatencyWrapperTest.class, RamWrapperTest.class, RequestWrapperTest.class })
public class WrapperTestSuite {
	
}
