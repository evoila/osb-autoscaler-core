package de.evoila.cf.autoscaler.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * A bean for storing default values.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Service
@ConfigurationProperties(prefix = "default")
public class DefaultValueBean {

	/**
	 * Default option whether scaling is enabled
	 */
	private boolean scalingEnabled;
	
	/**
	 * Default option whether CPU scaling is enabled
	 */
	private boolean cpuScalingEnabled;
	
	/**
	 * Default option whether RAM scaling is enabled
	 */
	private boolean ramScalingEnabled;
	
	/**
	 * Default option whether latency scaling is enabled
	 */
	private boolean latencyScalingEnabled;
	
	/**
	 * Default option whether latency scaling is enabled
	 */
	private boolean quotientScalingEnabled;
	
	/**
	 * Default option whether prediction scaling is enabled
	 */
	private boolean predictionScalingEnabled;
	
	/**
	 * Default value for the scaling interval multiplier
	 */
	private int scalingIntervalMultiplier;
		
	/**
	 * Default value for the minimum of instances an application is allowed to have.
	 */
	private int minInstances;
	
	/**
	 * Default value for the maximum of instances an application is allowed to have
	 */
	private int maxInstances;
	
	/**
	 * Default value for the cool down time.
	 */
	private int cooldownTime;
	
	/**
	 * Default option whether learning is enabled.
	 */
	private boolean learningEnabled;
	
	/**
	 * Default option whether billing interval is considered´.
	 */
	private boolean billingIntervalConsidered;
	
	/**
	 * Default value for the learning time multiplier.
	 */
	private int learningTimeMultiplier;
	
	/**
	 * Default value for threshold policies
	 */
	private String thresholdPolicy;
	
	/**
	 * Default value for the upper limit of CPU
	 */
	private int cpuUpperLimit;
	
	/**
	 * Default value for the lower limit of CPU
	 */
	private int cpuLowerLimit;
	
	/**
	 * Default value for the upper limit of RAM
	 */
	private int ramUpperLimit;
	
	/**
	 * Default value for the lower limit of RAM
	 */
	private int ramLowerLimit;
	
	/**
	 * Default value for the upper limit of latency
	 */
	private int latencyUpperLimit;
	
	/**
	 * Default value for the lower limit of latency
	 */
	private int latencyLowerLimit;
	
	/**
	 * Default value for the minimum quotient
	 */
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
