package de.cf.autoscaler.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.cf.autoscaler.tests.kafka.KafkaTest;
import de.cf.autoscaler.tests.wrapper.WrapperTestSuite;

@RunWith(Suite.class)
@SuiteClasses({KafkaTest.class, WrapperTestSuite.class})
public class AllTests {

}
