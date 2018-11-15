package de.evoila.cf.autoscaler.core.controller.response;

import de.evoila.cf.autoscaler.core.model.ScalableApp;

/**
 * Simple wrapper class for serializing latency policies.
 * @author Marius Berger
 *
 */
public class ResponseLatency {

	private int upperLimit;
	private int lowerLimit;
	private String thresholdPolicy;
	private boolean latencyScalingEnabled;
	
	public ResponseLatency(ScalableApp app) {
		upperLimit = app.getLatency().getUpperLimit();
		lowerLimit = app.getLatency().getLowerLimit();
		thresholdPolicy = app.getLatency().getThresholdPolicy();
		latencyScalingEnabled = app.getLatency().isLatencyScalingEnabled();
	}

	public int getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(int upperLimit) {
		this.upperLimit = upperLimit;
	}

	public int getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(int lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public String getThresholdPolicy() {
		return thresholdPolicy;
	}

	public void setThresholdPolicy(String thresholdPolicy) {
		this.thresholdPolicy = thresholdPolicy;
	}

	public boolean isLatencyScalingEnabled() {
		return latencyScalingEnabled;
	}

	public void setLatencyScalingEnabled(boolean latencyScalingEnabled) {
		this.latencyScalingEnabled = latencyScalingEnabled;
	}
}
