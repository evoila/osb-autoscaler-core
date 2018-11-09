package de.evoila.cf.autoscaler.tests.scalingaction;

import de.evoila.cf.autoscaler.core.applications.CpuWrapper;
import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.scaling.ScalingAction;
import de.evoila.cf.autoscaler.core.scaling.ScalingChecker;
import de.evoila.cf.autoscaler.tests.TestBase;
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog;
import org.junit.Test;

public class CpuScalingActionTest extends TestBase{
	
	private static int reason = ScalingLog.CONTAINER_CPU_BASED;
	private static CpuWrapper componentWrapper = app.getCpu();
	private ScalingAction act;

	
	
	@Test
	public void testNoScaleWithQuotient() {
		app.getRequest().setQuotientScalingEnabled(true);
		app.getRequest().setQuotient(100);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getCpuMax()+1);
		componentWrapper.setLowerLimit(metricReader.getCpuMax()-1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertNoscale(act,reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getCpuMean()+1);
		componentWrapper.setLowerLimit(metricReader.getCpuMean()-1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertNoscale(act,reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getCpuMin()+1);
		componentWrapper.setLowerLimit(metricReader.getCpuMin()-1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertNoscale(act,reason);
		
		app.getRequest().resetQuotient();
	}
	
	@Test
	public void testUpScaleWithQuotient() {
		app.getRequest().setQuotientScalingEnabled(true);
		app.getRequest().setQuotient(100);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getCpuMax()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertUpscale(act, reason);

		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getCpuMean()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertUpscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getCpuMin()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertUpscale(act, reason);
		
		app.getRequest().resetQuotient();
	}
	
	@Test
	public void testDownScaleWithQuotient() {
		app.getRequest().setQuotientScalingEnabled(true);
		app.getRequest().setQuotient(100);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getCpuMax()+1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertDownscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getCpuMean()+1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertDownscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getCpuMin()+1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertDownscale(act, reason);
		
		app.getRequest().resetQuotient();
	}
	
	@Test
	public void testNoScaleWithoutQuotient() {
		app.getRequest().setQuotientScalingEnabled(false);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getCpuMax()+1);
		componentWrapper.setLowerLimit(metricReader.getCpuMax()-1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertNoscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getCpuMean()+1);
		componentWrapper.setLowerLimit(metricReader.getCpuMean()-1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertNoscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getCpuMin()+1);
		componentWrapper.setLowerLimit(metricReader.getCpuMin()-1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertNoscale(act, reason);
	}
	
	@Test
	public void testUpScaleWithoutQuotient() {
		app.getRequest().setQuotientScalingEnabled(false);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getCpuMax()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertUpscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getCpuMean()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertUpscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getCpuMin()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertUpscale(act, reason);
		
		app.getRequest().resetQuotient();
	}

	@Test
	public void testDownScaleWithoutQuotient() {
		app.getRequest().setQuotientScalingEnabled(false);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getCpuMax()+1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertDownscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getCpuMean()+1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertDownscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getCpuMin()+1);
		act = ScalingChecker.chooseScalingActionForCpu(app);
		assertDownscale(act, reason);
		
		app.getRequest().resetQuotient();
	}

}
