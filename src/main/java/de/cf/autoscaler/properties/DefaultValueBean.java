package de.cf.autoscaler.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A bean for storing default values.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Service
public class DefaultValueBean {

	/**
	 * Default option whether scaling is enabled
	 */
	@Value("${default.scalingEnabled}")
	private boolean scalingEnabled;
	
	/**
	 * Default option whether CPU scaling is enabled
	 */
	@Value("${default.cpuScalingEnabled}")
	private boolean cpuScalingEnabled;
	
	/**
	 * Default option whether RAM scaling is enabled
	 */
	@Value("${default.ramScalingEnabled}")
	private boolean ramScalingEnabled;
	
	/**
	 * Default option whether latency scaling is enabled
	 */
	@Value("${default.latencyScalingEnabled}")
	private boolean latencyScalingEnabled;
	
	/**
	 * Default option whether latency scaling is enabled
	 */
	@Value("${default.quotientScalingEnabled}")
	private boolean quotientScalingEnabled;
	
	/**
	 * Default option whether prediction scaling is enabled
	 */
	@Value("${default.predictionScalingEnabled}")
	private boolean predictionScalingEnabled;
	
	/**
	 * Default value for the scaling interval multiplier
	 */
	@Value("${default.scalingIntervalMultiplier}")
	private int scalingIntervalMultiplier;
		
	/**
	 * Default value for the minimum of instances an application is allowed to have.
	 */
	@Value("${default.minInstances}")
	private int minInstances;
	
	/**
	 * Default value for the maximum of instances an application is allowed to have
	 */
	@Value("${default.maxInstances}")
	private int maxInstances;
	
	/**
	 * Default value for the cool down time.
	 */
	@Value("${default.cooldownTime}")
	private int cooldownTime;
	
	/**
	 * Default option whether learning is enabled.
	 */
	@Value("${default.learningEnabled}")
	private boolean learningEnabled;
	
	/**
	 * Default option whether billing interval is considered´.
	 */
	@Value("${default.billingIntervalConsidered}")
	private boolean billingIntervalConsidered;
	
	/**
	 * Default value for the learning time multiplier.
	 */
	@Value("${default.learningTimeMultiplier}")
	private int learningTimeMultiplier;
	
	/**
	 * Default value for threshold policies
	 */
	@Value("${default.thresholdPolicy}")
	private String thresholdPolicy;
	
	/**
	 * Default value for the upper limit of CPU
	 */
	@Value("${default.cpuUpperLimit}")
	private int cpuUpperLimit;
	
	/**
	 * Default value for the lower limit of CPU
	 */
	@Value("${default.cpuLowerLimit}")
	private int cpuLowerLimit;
	
	/**
	 * Default value for the upper limit of RAM
	 */
	@Value("${default.ramUpperLimit}")
	private int ramUpperLimit;
	
	/**
	 * Default value for the lower limit of RAM
	 */
	@Value("${default.ramLowerLimit}")
	private int ramLowerLimit;
	
	/**
	 * Default value for the upper limit of latency
	 */
	@Value("${default.latencyUpperLimit}")
	private int latencyUpperLimit;
	
	/**
	 * Default value for the lower limit of latency
	 */
	@Value("${default.latencyLowerLimit}")
	private int latencyLowerLimit;
	
	/**
	 * Default value for the minimum quotient
	 */
	@Value("${default.minQuotient}")
	private int minQuotient;
	
	/**
	 * Constructor for Spring to inject the bean.
	 */
	public DefaultValueBean() { }
	
	public boolean isScalingEnabled() {
		return scalingEnabled;
	}
	
	public void setScalingEnabled(boolean scalingEnabled) {
		this.scalingEnabled = scalingEnabled;
	}
	
	public boolean isCpuScalingEnabled() {
		return cpuScalingEnabled;
	}

	public void setCpuScalingEnabled(boolean cpuScalingEnabled) {
		this.cpuScalingEnabled = cpuScalingEnabled;
	}

	public boolean isRamScalingEnabled() {
		return ramScalingEnabled;
	}

	public void setRamScalingEnabled(boolean ramScalingEnabled) {
		this.ramScalingEnabled = ramScalingEnabled;
	}

	public boolean isLatencyScalingEnabled() {
		return latencyScalingEnabled;
	}

	public void setLatencyScalingEnabled(boolean latencyScalingEnabled) {
		this.latencyScalingEnabled = latencyScalingEnabled;
	}

	public boolean isQuotientScalingEnabled() {
		return quotientScalingEnabled;
	}
	
	public void setQuotientScalingEnabled(boolean quotientScalingEnabled) {
		this.quotientScalingEnabled = quotientScalingEnabled;
	}
	
	public boolean isPredictionScalingEnabled() {
		return predictionScalingEnabled;
	}

	public void setPredictionScalingEnabled(boolean predictionScalingEnabled) {
		this.predictionScalingEnabled = predictionScalingEnabled;
	}

	public int getScalingIntervalMultiplier() {
		return scalingIntervalMultiplier;
	}
	
	public void setScalingIntervalMultiplier(int scalingIntervalMultiplier) {
		this.scalingIntervalMultiplier = scalingIntervalMultiplier;
	}
	
	public int getMinInstances() {
		return minInstances;
	}
	
	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}
	
	public int getMaxInstances() {
		return maxInstances;
	}
	
	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}
	
	public int getCooldownTime() {
		return cooldownTime;
	}
	
	public void setCooldownTime(int cooldownTime) {
		this.cooldownTime = cooldownTime;
	}
	
	public boolean isLearningEnabled() {
		return learningEnabled;
	}
	
	public void setLearningEnabled(boolean learningEnabled) {
		this.learningEnabled = learningEnabled;
	}
	
	public boolean isBillingIntervalConsidered() {
		return billingIntervalConsidered;
	}
	
	public void setBillingIntervalConsidered(boolean billingIntervalConsidered) {
		this.billingIntervalConsidered = billingIntervalConsidered;
	}
	
	public int getLearningTimeMultiplier() {
		return learningTimeMultiplier;
	}
	
	public void setLearningTimeMultiplier(int learningTimeMultiplier) {
		this.learningTimeMultiplier = learningTimeMultiplier;
	}
	
	public String getThresholdPolicy() {
		return thresholdPolicy;
	}
	
	public void setThresholdPolicy(String thresholdPolicy) {
		this.thresholdPolicy = thresholdPolicy;
	}
	
	public int getCpuUpperLimit() {
		return cpuUpperLimit;
	}
	
	public void setCpuUpperLimit(int cpuUpperLimit) {
		this.cpuUpperLimit = cpuUpperLimit;
	}
	
	public int getCpuLowerLimit() {
		return cpuLowerLimit;
	}
	
	public void setCpuLowerLimit(int cpuLowerLimit) {
		this.cpuLowerLimit = cpuLowerLimit;
	}

	public int getRamUpperLimit() {
		return ramUpperLimit;
	}

	public void setRamUpperLimit(int ramUpperLimit) {
		this.ramUpperLimit = ramUpperLimit;
	}

	public int getRamLowerLimit() {
		return ramLowerLimit;
	}

	public void setRamLowerLimit(int ramLowerLimit) {
		this.ramLowerLimit = ramLowerLimit;
	}

	public int getMinQuotient() {
		return minQuotient;
	}
	public void setMinQuotient(int minQuotient) {
		this.minQuotient = minQuotient;
	}

	public int getLatencyUpperLimit() {
		return latencyUpperLimit;
	}

	public void setLatencyUpperLimit(int latencyUpperLimit) {
		this.latencyUpperLimit = latencyUpperLimit;
	}

	public int getLatencyLowerLimit() {
		return latencyLowerLimit;
	}

	public void setLatencyLowerLimit(int latencyLowerLimit) {
		this.latencyLowerLimit = latencyLowerLimit;
	}
}
