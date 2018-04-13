package de.evoila.cf.autoscaler.core.http.response;

import de.evoila.cf.autoscaler.core.applications.ScalableApp;

/**
 * Simple wrapper class for serializing CPU policies.
 * @author Marius Berger
 *
 */
public class ResponseCpu {

	private int upperLimit;
	private int lowerLimit;
	private String thresholdPolicy;
	private boolean cpuScalingEnabled;
	
	public ResponseCpu(ScalableApp app) {
		upperLimit = app.getCpu().getUpperLimit();
		lowerLimit = app.getCpu().getLowerLimit();
		thresholdPolicy = app.getCpu().getThresholdPolicy();
		cpuScalingEnabled = app.getCpu().isCpuScalingEnabled();
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

	public boolean isCpuScalingEnabled() {
		return cpuScalingEnabled;
	}

	public void setCpuScalingEnabled(boolean cpuScalingEnabled) {
		this.cpuScalingEnabled = cpuScalingEnabled;
	}
	
	
}
