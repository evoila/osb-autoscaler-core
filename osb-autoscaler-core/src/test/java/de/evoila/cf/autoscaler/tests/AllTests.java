package de.evoila.cf.autoscaler.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.evoila.cf.autoscaler.tests.kafka.KafkaTest;
import de.evoila.cf.autoscaler.tests.wrapper.WrapperTestSuite;

@RunWith(Suite.class)
@SuiteClasses({KafkaTest.class, WrapperTestSuite.class})
public class AllTests {

}
