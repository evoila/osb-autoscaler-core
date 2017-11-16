package de.cf.autoscaler.http.response;

import de.cf.autoscaler.applications.ScalableApp;

/**
 * Simple wrapper class for serializing policies concerning general scaling.
 * @author Marius Berger
 *
 */
public class ResponseScaling {

	
	private boolean scalingEnabled;
	private boolean predictionScalingEnabled;
	private boolean billingIntervalEnabled;
	private int scalingIntervalMultiplier;
	private int currentIntervalState;
	private int minInstances;
	private int maxInstances;
	private int cooldownTime;
	
	public ResponseScaling(ScalableApp app) {
		scalingEnabled = app.isScalingEnabled();
		predictionScalingEnabled = app.isPredictionScalingEnabled();
		billingIntervalEnabled = app.isBillingIntervalConsidered();
		scalingIntervalMultiplier = app.getScalingIntervalMultiplier();
		currentIntervalState = app.getCurrentIntervalState();
		minInstances = app.getMinInstances();
		maxInstances = app.getMaxInstances();
		cooldownTime = app.getCooldownTime();
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
	
	public boolean isScalingEnabled() {
		return scalingEnabled;
	}
	
	public void setScalingEnabled(boolean scalingEnabled) {
		this.scalingEnabled = scalingEnabled;
	}
	
	public boolean isPredictionScalingEnabled() {
		return predictionScalingEnabled;
	}
	
	public void setPredictionScalingEnabled(boolean predictionScalingEnabled) {
		this.predictionScalingEnabled = predictionScalingEnabled;
	}
	
	public boolean isBillingIntervalEnabled() {
		return billingIntervalEnabled;
	}
	
	public void setBillingIntervalEnabled(boolean billingIntervalEnabled) {
		this.billingIntervalEnabled = billingIntervalEnabled;
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
}
