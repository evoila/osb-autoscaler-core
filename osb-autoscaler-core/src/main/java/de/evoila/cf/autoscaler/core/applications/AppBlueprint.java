package de.evoila.cf.autoscaler.core.applications;

import de.evoila.cf.autoscaler.api.binding.Binding;
import org.springframework.data.annotation.Id;

/**
 * A blueprint for storing or restoring a {@code ScalableApp} in or from the database.
 * @author Marius Berger
 *
 */
public class AppBlueprint {
	
	private int scalingIntervalMultiplier;
	private int currentIntervalState;
	private int minInstances;
	private int maxInstances;
	private int cooldownTime;
	private int learningTimeMultiplier;
	private int cpuUpperLimit;
	private int cpuLowerLimit;
	private int minQuotient;
	private int quotient;
	private int latencyUpperLimit;
	private int latencyLowerLimit;
	
	
	private boolean scalingEnabled;
	private boolean cpuScalingEnabled;
	private boolean ramScalingEnabled;
	private boolean latencyScalingEnabled;
	private boolean quotientBasedScalingEnabled;
	private boolean predictionScalingEnabled;
	private boolean learningEnabled;
	private boolean billingIntervalConsidered;
	
	private long ramUpperLimit;
	private long ramLowerLimit;
	private long lastScalingTime;
	private long learningStartTime;
	
	@Id
	//used for identifying an individual app since bindingId is hidden in the binding object
	private String id;
	private String cpuThresholdPolicy;
	private String requestThresholdPolicy;
	private String ramThresholdPolicy;
	private String latencyThresholdPolicy;
	
	private Binding binding;

	/**
	 * Constructor for Spring Data. Should not be used otherwise, because it could lead to an object with invalid settings.
	 */
	public AppBlueprint() { }

	/**
	 * Constructor for creating a blueprint out of an existing {@code ScalableApp}.
	 * @param app {@linkplain ScalableApp} to get fields from
	 */
	public AppBlueprint(ScalableApp app) {
		id = app.getBinding().getId();
		binding = new Binding(app.getBinding());
		
		scalingIntervalMultiplier = app.getScalingIntervalMultiplier();
		currentIntervalState = app.getCurrentIntervalState();
		minInstances = app.getMinInstances();
		maxInstances = app.getMaxInstances();
		cooldownTime = app.getCooldownTime();
		learningTimeMultiplier = app.getLearningTimeMultiplier();
		cpuUpperLimit = app.getCpu().getUpperLimit();
		cpuLowerLimit = app.getCpu().getLowerLimit();
		minQuotient = app.getRequest().getMinQuotient();
		quotient = app.getRequest().getQuotient();
		ramUpperLimit = app.getRam().getUpperLimit();
		ramLowerLimit = app.getRam().getLowerLimit();
		latencyUpperLimit = app.getLatency().getUpperLimit();
		latencyLowerLimit = app.getLatency().getLowerLimit();
		
		scalingEnabled = app.isScalingEnabled();
		cpuScalingEnabled = app.getCpu().isCpuScalingEnabled();
		ramScalingEnabled = app.getRam().isRamScalingEnabled();
		latencyScalingEnabled = app.getLatency().isLatencyScalingEnabled();
		quotientBasedScalingEnabled = app.getRequest().isQuotientScalingEnabled();
		predictionScalingEnabled = app.isPredictionScalingEnabled();
		learningEnabled = app.isLearningEnabled();
		billingIntervalConsidered =app.isBillingIntervalConsidered() ;
		
		lastScalingTime = app.getLastScalingTime();
		learningStartTime = app.getLearningStartTime();
		
		cpuThresholdPolicy = app.getCpu().getThresholdPolicy();
		requestThresholdPolicy = app.getRequest().getThresholdPolicy();
		ramThresholdPolicy = app.getRam().getThresholdPolicy();
		latencyThresholdPolicy = app.getLatency().getThresholdPolicy();
	}
	
	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

	public int getScalingIntervalMultiplier() {
		return scalingIntervalMultiplier;
	}

	public void setScalingIntervalMultiplier(int scalingIntervalMultiplier) {
		this.scalingIntervalMultiplier = scalingIntervalMultiplier;
	}

	public int getCurrentIntervalState() {
		return currentIntervalState;
	}

	public void setCurrentIntervalState(int currentIntervalState) {
		this.currentIntervalState = currentIntervalState;
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

	public int getLearningTimeMultiplier() {
		return learningTimeMultiplier;
	}

	public void setLearningTimeMultiplier(int learningTimeMultiplier) {
		this.learningTimeMultiplier = learningTimeMultiplier;
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

	public int getMinQuotient() {
		return minQuotient;
	}

	public void setMinQuotient(int minQuotient) {
		this.minQuotient = minQuotient;
	}

	public int getQuotient() {
		return quotient;
	}

	public void setQuotient(int quotient) {
		this.quotient = quotient;
	}

	public long getRamUpperLimit() {
		return ramUpperLimit;
	}

	public void setRamUpperLimit(long ramUpperLimit) {
		this.ramUpperLimit = ramUpperLimit;
	}

	public long getRamLowerLimit() {
		return ramLowerLimit;
	}

	public void setRamLowerLimit(long ramLowerLimit) {
		this.ramLowerLimit = ramLowerLimit;
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

	public boolean isQuotientBasedScalingEnabled() {
		return quotientBasedScalingEnabled;
	}

	public void setQuotientBasedScalingEnabled(boolean quotientBasedScalingEnabled) {
		this.quotientBasedScalingEnabled = quotientBasedScalingEnabled;
	}

	public boolean isPredictionScalingEnabled() {
		return predictionScalingEnabled;
	}

	public void setPredictionScalingEnabled(boolean predictionScalingEnabled) {
		this.predictionScalingEnabled = predictionScalingEnabled;
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

	public long getLastScalingTime() {
		return lastScalingTime;
	}

	public void setLastScalingTime(long lastScalingTime) {
		this.lastScalingTime = lastScalingTime;
	}

	public long getLearningStartTime() {
		return learningStartTime;
	}

	public void setLearningStartTime(long learningStartTime) {
		this.learningStartTime = learningStartTime;
	}

	public String getCpuThresholdPolicy() {
		return cpuThresholdPolicy;
	}

	public void setCpuThresholdPolicy(String cpuThresholdPolicy) {
		this.cpuThresholdPolicy = cpuThresholdPolicy;
	}

	public String getRequestThresholdPolicy() {
		return requestThresholdPolicy;
	}

	public void setRequestThresholdPolicy(String requestThresholdPolicy) {
		this.requestThresholdPolicy = requestThresholdPolicy;
	}

	public String getRamThresholdPolicy() {
		return ramThresholdPolicy;
	}

	public void setRamThresholdPolicy(String ramThresholdPolicy) {
		this.ramThresholdPolicy = ramThresholdPolicy;
	}

	public String getLatencyThresholdPolicy() {
		return latencyThresholdPolicy;
	}

	public void setLatencyThresholdPolicy(String latencyThresholdPolicy) {
		this.latencyThresholdPolicy = latencyThresholdPolicy;
	}

	@Override
	public String toString() {
		return "AppBlueprint [scalingIntervalMultiplier=" + scalingIntervalMultiplier + ", currentIntervalState="
				+ currentIntervalState + ", minInstances=" + minInstances + ", maxInstances=" + maxInstances
				+ ", cooldownTime=" + cooldownTime + ", learningTimeMultiplier=" + learningTimeMultiplier
				+ ", cpuUpperLimit=" + cpuUpperLimit + ", cpuLowerLimit=" + cpuLowerLimit + ", minQuotient="
				+ minQuotient + ", quotient=" + quotient + ", latencyUpperLimit=" + latencyUpperLimit
				+ ", latencyLowerLimit=" + latencyLowerLimit + ", scalingEnabled=" + scalingEnabled
				+ ", cpuScalingEnabled=" + cpuScalingEnabled + ", ramScalingEnabled=" + ramScalingEnabled
				+ ", latencyScalingEnabled=" + latencyScalingEnabled + ", quotientBasedScalingEnabled="
				+ quotientBasedScalingEnabled + ", predictionScalingEnabled=" + predictionScalingEnabled
				+ ", learningEnabled=" + learningEnabled + ", billingIntervalConsidered=" + billingIntervalConsidered
				+ ", ramUpperLimit=" + ramUpperLimit + ", ramLowerLimit=" + ramLowerLimit + ", lastScalingTime="
				+ lastScalingTime + ", learningStartTime=" + learningStartTime + ", cpuThresholdPolicy="
				+ cpuThresholdPolicy + ", requestThresholdPolicy=" + requestThresholdPolicy + ", ramThresholdPolicy="
				+ ramThresholdPolicy + ", latencyThresholdPolicy=" + latencyThresholdPolicy + ", binding=" + binding
				+ "]";
	}
}
