package de.evoila.cf.autoscaler.tests.scalingaction;

import de.evoila.cf.autoscaler.core.model.RamWrapper;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.scaling.ScalingAction;
import de.evoila.cf.autoscaler.core.scaling.ScalingChecker;
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog;
import de.evoila.cf.autoscaler.tests.TestBase;
import org.junit.Test;

public class RamScalingActionTest extends TestBase{
	
	private static int reason = ScalingLog.CONTAINER_RAM_BASED;
	private static RamWrapper componentWrapper = app.getRam();
	private ScalingAction act;
	
	@Test
	public void testNoScaleWithQuotient() {
		app.getRequest().setQuotientScalingEnabled(true);
		app.getRequest().setQuotient(100);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getRamMax()+1);
		componentWrapper.setLowerLimit(metricReader.getRamMax()-1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertNoscale(act,reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getRamMean()+1);
		componentWrapper.setLowerLimit(metricReader.getRamMean()-1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertNoscale(act,reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getRamMin()+1);
		componentWrapper.setLowerLimit(metricReader.getRamMin()-1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertNoscale(act,reason);
		
		app.getRequest().resetQuotient();
	}
	
	@Test
	public void testUpScaleWithQuotient() {
		app.getRequest().setQuotientScalingEnabled(true);
		app.getRequest().setQuotient(100);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getRamMax()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertUpscale(act, reason);

		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getRamMean()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertUpscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getRamMin()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForRam(app);
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
		componentWrapper.setLowerLimit(metricReader.getRamMax()+1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertDownscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getRamMean()+1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertDownscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getRamMin()+1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertDownscale(act, reason);
		
		app.getRequest().resetQuotient();
	}
	
	@Test
	public void testNoScaleWithoutQuotient() {
		app.getRequest().setQuotientScalingEnabled(false);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getRamMax()+1);
		componentWrapper.setLowerLimit(metricReader.getRamMax()-1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertNoscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getRamMean()+1);
		componentWrapper.setLowerLimit(metricReader.getRamMean()-1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertNoscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getRamMin()+1);
		componentWrapper.setLowerLimit(metricReader.getRamMin()-1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertNoscale(act, reason);
	}
	
	@Test
	public void testUpScaleWithoutQuotient() {
		app.getRequest().setQuotientScalingEnabled(false);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(metricReader.getRamMax()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertUpscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(metricReader.getRamMean()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertUpscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(metricReader.getRamMin()-1);
		componentWrapper.setLowerLimit(0);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertUpscale(act, reason);
		
		app.getRequest().resetQuotient();
	}

	@Test
	public void testDownScaleWithoutQuotient() {
		app.getRequest().setQuotientScalingEnabled(false);
		
		// testing for MAX as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MAX);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getRamMax()+1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertDownscale(act, reason);
		
		// testing for MEAN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MEAN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getRamMean()+1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertDownscale(act, reason);
		
		// testing for MIN as threshold
		componentWrapper.setThresholdPolicy(ScalableApp.MIN);
		componentWrapper.setUpperLimit(Integer.MAX_VALUE);
		componentWrapper.setLowerLimit(metricReader.getRamMin()+1);
		act = ScalingChecker.chooseScalingActionForRam(app);
		assertDownscale(act, reason);
		
		app.getRequest().resetQuotient();
	}

}
