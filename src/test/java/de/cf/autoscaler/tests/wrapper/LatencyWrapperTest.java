package de.cf.autoscaler.tests.wrapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.cf.autoscaler.applications.LatencyWrapper;
import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.tests.TestBase;

public class LatencyWrapperTest extends TestBase {

	@Test
	public void testLatency() {
		LatencyWrapper lat = app.getLatency();
		
		lat.setThresholdPolicy(ScalableApp.MAX);
		assertEquals(metricReader.getLatencyMax(), lat.getValueOfLatency());
		
		lat.setThresholdPolicy(ScalableApp.MEAN);
		assertEquals(metricReader.getLatencyMean(), lat.getValueOfLatency());
		
		lat.setThresholdPolicy(ScalableApp.MIN);
		assertEquals(metricReader.getLatencyMin(), lat.getValueOfLatency());
	}

}
