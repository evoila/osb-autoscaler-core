package de.cf.autoscaler.http.response;

import de.cf.autoscaler.applications.ScalableApp;

/**
 * Simple wrapper class for serializing RAM policies.
 * @author Marius Berger
 *
 */
public class ResponseRam {

	private long upperLimit;
	private long lowerLimit;
	private String thresholdPolicy;
	private boolean ramScalingEnabled;
	
	public ResponseRam(ScalableApp app) {
		upperLimit = app.getRam().getUpperLimit();
		lowerLimit = app.getRam().getLowerLimit();
		thresholdPolicy = app.getRam().getThresholdPolicy();
		ramScalingEnabled = app.getRam().isRamScalingEnabled();
	}

	public long getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(long upperLimit) {
		this.upperLimit = upperLimit;
	}

	public long getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(long lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public String getThresholdPolicy() {
		return thresholdPolicy;
	}

	public void setThresholdPolicy(String thresholdPolicy) {
		this.thresholdPolicy = thresholdPolicy;
	}

	public boolean isRamScalingEnabled() {
		return ramScalingEnabled;
	}

	public void setRamScalingEnabled(boolean ramScalingEnabled) {
		this.ramScalingEnabled = ramScalingEnabled;
	}
}
