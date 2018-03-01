package de.cf.autoscaler.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A bean for storing properties dedicated to the Autoscaler.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Service
public class AutoscalerPropertiesBean {

	/**
	 * Maximum count a List storing Metrics is allowed the have.
	 */
	@Value("${scaler.maxMetricListSize}")
	private int maxMetricListSize;
	
	/**
	 * Maximum age a Metric is allowed the have.
	 */
	@Value("${scaler.maxMetricAge}")
	private long maxMetricAge;
	
	/**
	 * Boolean value, whether to ask the scaling engine for the application name when creating a new binding.
	 */
	@Value("${scaler.appname.get_from_scaling_engine_at_binding}")
	private boolean getAppNameFromScalingEngineAtBinding;
	
	/**
	 * Constructor for Spring to inject the bean.
	 */
	public AutoscalerPropertiesBean() { }

	public int getMaxMetricListSize() {
		return maxMetricListSize;
	}

	public void setMaxMetricListSize(int maxMetricListSize) {
		this.maxMetricListSize = maxMetricListSize;
	}

	public long getMaxMetricAge() {
		return maxMetricAge;
	}

	public void setMaxMetricAge(long maxMetricAge) {
		this.maxMetricAge = maxMetricAge;
	}

	public boolean isGetAppNameFromScalingEngineAtBinding() {
		return getAppNameFromScalingEngineAtBinding;
	}

	public void setGetAppNameFromScalingEngineAtBinding(boolean getAppNameFromScalingEngineAtBinding) {
		this.getAppNameFromScalingEngineAtBinding = getAppNameFromScalingEngineAtBinding;
	}
}
