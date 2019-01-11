package de.evoila.cf.autoscaler.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A bean for storing default values.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Configuration
@ConfigurationProperties(prefix = "default")
public class DefaultValueBean {

	private boolean scalingEnabled;

	private boolean cpuScalingEnabled;

	private boolean ramScalingEnabled;

	private boolean latencyScalingEnabled;

	private boolean quotientScalingEnabled;

	private boolean predictionScalingEnabled;

	private int scalingIntervalMultiplier;

	private int minInstances;

	private int maxInstances;

	private int cooldownTime;

	private boolean learningEnabled;

	private boolean billingIntervalConsidered;

	private int learningTimeMultiplier;

	private String thresholdPolicy;

	private int cpuUpperLimit;

	private int cpuLowerLimit;

	private int ramUpperLimit;

	private int ramLowerLimit;

	private int latencyUpperLimit;

	private int latencyLowerLimit;

	private int minQuotient;
	

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
