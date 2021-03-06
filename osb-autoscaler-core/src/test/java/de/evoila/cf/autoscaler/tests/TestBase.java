package de.evoila.cf.autoscaler.tests;


import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.api.binding.BindingContext;
import de.evoila.cf.autoscaler.core.applications.AppBlueprint;
import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.applications.ScalableAppService;
import de.evoila.cf.autoscaler.core.kafka.producer.ProtobufProducer;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import de.evoila.cf.autoscaler.core.properties.DefaultValueBean;
import de.evoila.cf.autoscaler.core.scaling.ScalingAction;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.evoila.cf.autoscaler.kafka.messages.ContainerMetric;
import de.evoila.cf.autoscaler.kafka.messages.HttpMetric;
import org.junit.BeforeClass;

import java.util.List;

import static org.junit.Assert.*;

public class TestBase {

	protected static MetricReader metricReader;
	
	protected static ScalableApp app;
	static KafkaPropertiesBean kafkaProps;
	static DefaultValueBean defaults;
	static AutoscalerPropertiesBean autoscalerProps;
	static ProtobufProducer producer;
	
	@BeforeClass
	public static void setUp() {
		kafkaProps = new KafkaPropertiesBean();
		defaults = new DefaultValueBean();
		autoscalerProps = new AutoscalerPropertiesBean();
		autoscalerProps.setMaxMetricListSize(10000);
		autoscalerProps.setMaxMetricAge(35 * 1000);
		producer = new ProtobufProducer(kafkaProps);
		AppBlueprint bp;
		bp = setUpBluePrint();
		app = new ScalableApp(bp, kafkaProps, autoscalerProps, producer);
		
		metricReader = new MetricReader();
		metricReader.readFromFile(MetricReader.PATH);
		
		List<ValuePair<Integer>> cpuValues = metricReader.getCpuPairs();
		List<ValuePair<Long>> ramValues = metricReader.getRamPairs();
		int[] latencyValues = metricReader.getLatencyValues();
		int[] requestValues = metricReader.getRequestValues();
		
		for (int i = 0; i < metricReader.getMetricCount(); i++) {
			long now = System.currentTimeMillis();
			app.addMetric(new ContainerMetric(now, "testContainerMetric", "testId", "testName", "test-space", "testOrgGuid", cpuValues.get(i).getValue(), ramValues.get(i).getValue() * 1024 * 1024, cpuValues.get(i).getInstanceIndex(), ""));
			app.addMetric(new HttpMetric(now, "testHttpMetric", "testId", requestValues[i], latencyValues[i],""));
			ScalableAppService.aggregateInstanceMetrics(app, null);
		}
	}

	private static AppBlueprint setUpBluePrint() {
		AppBlueprint bp = new AppBlueprint();
		
		BindingContext context = new BindingContext("cloudfoundry", "default", "evoila");
		Binding binding = new Binding("testBindingId","testId", "testName", "testScalerId", "testServiceId", 0, context);
		bp.setBinding(binding);
		
		bp.setScalingEnabled(true);
		bp.setPredictionScalingEnabled(true);
		bp.setBillingIntervalConsidered(true);
		bp.setScalingIntervalMultiplier(1);
		bp.setCurrentIntervalState(0);
		bp.setMinInstances(1);
		bp.setMaxInstances(30);
		bp.setCooldownTime(10000);
		
		bp.setCpuUpperLimit(90);
		bp.setCpuLowerLimit(50);
		bp.setCpuThresholdPolicy("mean");
		bp.setCpuScalingEnabled(true);
		
		bp.setRamUpperLimit(734003200);
		bp.setRamLowerLimit(536870912);
		bp.setRamThresholdPolicy("mean");
		bp.setRamScalingEnabled(true);
		
		bp.setLatencyUpperLimit(5000);
		bp.setLatencyLowerLimit(25);
		bp.setLatencyThresholdPolicy("mean");
		bp.setRamScalingEnabled(true);
		
		bp.setQuotient(0);
		bp.setMinQuotient(0);
		bp.setRequestThresholdPolicy("mean");
		bp.setQuotientBasedScalingEnabled(true);
		
		bp.setLearningEnabled(true);
		bp.setLearningTimeMultiplier(1);
		bp.setLearningStartTime(0);
		
		return bp;
	}
	
	protected void assertUpscale(ScalingAction act, int reason) {
		if (act.getOldInstances() == app.getMaxInstances()) {
			assertNoscale(act,reason);
		} else {
			assertTrue(act.isNeedToScale());
			assertFalse(act.isDownscale());
			assertTrue(act.isUpscale());
			assertTrue(act.getNewInstances() > act.getOldInstances());
			assertTrue(act.getReason() == reason);
		}
	}
	protected void assertDownscale(ScalingAction act, int reason) {
		if (act.getOldInstances() == app.getMinInstances()) {
			assertNoscale(act,reason);
		} else {
			assertTrue(act.isNeedToScale());
			assertTrue(act.isDownscale());
			assertFalse(act.isUpscale());
			assertTrue(act.getNewInstances() < act.getOldInstances());
			assertTrue(act.getReason() == reason);
		}
		
	}
	protected void assertNoscale(ScalingAction act, int reason) {
		if (act.getOldInstances() > app.getMaxInstances()) {
			assertDownscale(act, reason);
		} else if(act.getOldInstances() < app.getMinInstances()) {
			assertUpscale(act,reason);
		} else {
			assertFalse(act.isNeedToScale());
			assertFalse(act.isDownscale());
			assertFalse(act.isUpscale());
			assertEquals(act.getOldInstances(), act.getNewInstances());
			assertTrue(act.getReason() == reason);
		}
		
	}
}
