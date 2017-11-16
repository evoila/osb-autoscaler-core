package de.cf.autoscaler.tests.wrapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.cf.autoscaler.applications.RamWrapper;
import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.tests.TestBase;

public class RamWrapperTest extends TestBase {
	
	@Test
	public void testRam() {
		RamWrapper ram = app.getRam();
		
		ram.setThresholdPolicy(ScalableApp.MAX);
		assertEquals(metricReader.getRamMax(), ram.getValueOfRam());
		
		ram.setThresholdPolicy(ScalableApp.MEAN);
		assertEquals(metricReader.getRamMean() , ram.getValueOfRam());
		
		ram.setThresholdPolicy(ScalableApp.MIN);
		assertEquals(metricReader.getRamMin(), ram.getValueOfRam());
	}

}
