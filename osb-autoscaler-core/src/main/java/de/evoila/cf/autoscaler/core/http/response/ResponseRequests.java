package de.evoila.cf.autoscaler.core.http.response;

import de.evoila.cf.autoscaler.core.applications.ScalableApp;

/**
 * Simple wrapper class for serializing request policies.
 * @author Marius Berger
 *
 */
public class ResponseRequests {

	private int quotient;
	private int minQuotient;
	private String thresholdPolicy;
	private boolean quotientScalingEnabled;
	
	public ResponseRequests(ScalableApp app) {
		quotient = app.getRequest().getQuotient();
		minQuotient = app.getRequest().getMinQuotient();
		thresholdPolicy = app.getRequest().getThresholdPolicy();
		quotientScalingEnabled = app.getRequest().isQuotientScalingEnabled();
	}

	public int getQuotient() {
		return quotient;
	}

	public void setQuotient(int quotient) {
		this.quotient = quotient;
	}

	public int getMinQuotient() {
		return minQuotient;
	}

	public void setMinQuotient(int minQuotient) {
		this.minQuotient = minQuotient;
	}

	public String getThresholdPolicy() {
		return thresholdPolicy;
	}

	public void setThresholdPolicy(String thresholdPolicy) {
		this.thresholdPolicy = thresholdPolicy;
	}

	public boolean isQuotientScalingEnabled() {
		return quotientScalingEnabled;
	}

	public void setQuotientScalingEnabled(boolean quotientScalingEnabled) {
		this.quotientScalingEnabled = quotientScalingEnabled;
	}
}
