package de.evoila.cf.autoscaler.core.applications;

import de.evoila.cf.autoscaler.kafka.messages.ApplicationMetric;

import java.util.List;

/**
 * Wraps settings and methods for latency handling of an application.
 * @author Marius Berger
 */
public class LatencyWrapper {

	/**
	 * The upper limit for the latency to reach before scaling up.
	 */
	private int upperLimit;
	
	/**
	 * The lower limit for the CPU to reach before scaling down.
	 */
	private int lowerLimit;
	
	/**
	 * Policy to describe how the latency of the application is computed.
	 */
	private String thresholdPolicy;
	
	/**
	 * Boolean value, whether scaling based on latency is activated.
	 */
	private boolean latencyScalingEnabled;
	
	/**
	 * {@code ScalableApp} to which this object is bound.
	 */
	private final de.evoila.cf.autoscaler.core.applications.ScalableApp assignedApp;
	

	/**
	 * Constructor with all fields.
	 * @param upperLimit {@linkplain #upperLimit}
	 * @param lowerLimit {@linkplain #lowerLimit}
	 * @param thresholdPolicy {@linkplain #thresholdPolicy}
	 * @param latencyScalingEnabled {@linkplain #latencyScalingEnabled}
	 * @param assignedApp {@linkplain #assignedApp}
	 */
	public LatencyWrapper(int upperLimit, int lowerLimit, String thresholdPolicy, boolean latencyScalingEnabled,
			de.evoila.cf.autoscaler.core.applications.ScalableApp assignedApp) {
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
		this.thresholdPolicy = thresholdPolicy;
		this.latencyScalingEnabled = latencyScalingEnabled;
		this.assignedApp = assignedApp;
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

	public de.evoila.cf.autoscaler.core.applications.ScalableApp getAssignedApp() {
		return assignedApp;
	}
	
	/**
	 * Returns the latency of the application depending on the chosen {@code thresholdPolicy} based on the {@code ApplicationMetrics}.
	 * @return latency of the application or -1 for an invalid {@code thresholdPolicy}
	 */
	public int getValueOfLatency() {
		if (thresholdPolicy.equals(de.evoila.cf.autoscaler.core.applications.ScalableApp.MAX))
			return maxOfLatency();
		if (thresholdPolicy.equals(de.evoila.cf.autoscaler.core.applications.ScalableApp.MIN))
			return minOfLatency();
		if (thresholdPolicy.equals(de.evoila.cf.autoscaler.core.applications.ScalableApp.MEAN))
			return meanOfLatency();
		
		return -1;
	}
	
	/**
	 * Computes the latency by searching for the maximum in the {@code ApplicationMetrics}.
	 * @return the maximum of latencies or 0 if no metrics are stored
	 */
	private int maxOfLatency() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= 0;
		for (int i = 0; i < metrics.size(); i++) {
			output = Math.max(output, metrics.get(i).getLatency());
		}
		return output;
	}
	
	/**
	 * Computes the latency by searching for the minimum in the {@code ApplicationMetrics}.
	 * @return the minimum of latencies or {@code Integer.Max_Value} if no metrics are stored
	 */
	private int minOfLatency() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= Integer.MAX_VALUE;
		for (int i = 0; i < metrics.size(); i++) {
			output = Math.min(output, metrics.get(i).getLatency());
		}
		return output;
	}
	
	/**
	 * Computes the latency by adding all values and dividing the sum with the count of {@code ApplicationMetrics}.
	 * @return the mean of latencies or 0 if no metrics are stored
	 */
	private int meanOfLatency() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= 0;
		if (metrics.size() > 0) {
			for (int i = 0; i < metrics.size(); i++) {
				output += metrics.get(i).getLatency();
			}
			output /= metrics.size();
		}
		return output;
	}
}
