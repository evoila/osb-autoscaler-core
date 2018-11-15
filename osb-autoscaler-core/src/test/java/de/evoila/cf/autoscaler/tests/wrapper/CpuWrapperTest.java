package de.evoila.cf.autoscaler.tests.wrapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.evoila.cf.autoscaler.core.model.CpuWrapper;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.tests.TestBase;

public class CpuWrapperTest extends TestBase{
	

	@Test
	public void testCpu() {
		CpuWrapper cpu = app.getCpu();
		
		cpu.setThresholdPolicy(ScalableApp.MAX);
		assertEquals(metricReader.getCpuMax(), cpu.getValueOfCpu());
		
		cpu.setThresholdPolicy(ScalableApp.MEAN);
		assertEquals(metricReader.getCpuMean(), cpu.getValueOfCpu());
		
		cpu.setThresholdPolicy(ScalableApp.MIN);
		assertEquals(metricReader.getCpuMin(), cpu.getValueOfCpu());
	}

}
