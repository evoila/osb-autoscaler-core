package de.cf.autoscaler.applications;

import java.util.List;

import de.cf.autoscaler.kafka.messages.ApplicationMetric;

/**
 * Wraps settings and methods for CPU handling of an application.
 * @author Marius Berger
 */
public class CpuWrapper {

	/**
	 * The upper limit for the CPU load to reach before scaling up.
	 */
	private int upperLimit;
	
	/**
	 * The lower limit for the CPU load to reach before scaling down.
	 */
	private int lowerLimit;
	
	/**
	 * Policy to describe how the CPU load of the application is computed.
	 */
	private String thresholdPolicy;
	
	/**
	 * Boolean value, whether scaling based on CPU load is activated.
	 */
	private boolean cpuScalingEnabled;
	
	/**
	 * {@code ScalableApp} to which this object is bound.
	 */
	private final ScalableApp assignedApp;

	/**
	 * Constructor with all fields.
	 * @param thresholdPolicy {@linkplain #thresholdPolicy}
	 * @param upperLimit upper {@linkplain #upperLimit}
	 * @param lowerLimit {@linkplain #lowerLimit}
	 * @param cpuScalingEnabled {@linkplain #cpuScalingEnabled}
	 * @param assignedApp {@linkplain #assignedApp}
	 */
	public CpuWrapper(String thresholdPolicy, int upperLimit, int lowerLimit, boolean cpuScalingEnabled, ScalableApp assignedApp) {
		this.thresholdPolicy = thresholdPolicy;
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
		this.cpuScalingEnabled = cpuScalingEnabled;
		this.assignedApp = assignedApp;
	}

	public String getThresholdPolicy() {
		return thresholdPolicy;
	}

	public void setThresholdPolicy(String cpuThresholdPolicy) {
		this.thresholdPolicy = cpuThresholdPolicy;
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
	
	public boolean isCpuScalingEnabled() {
		return cpuScalingEnabled;
	}

	public void setCpuScalingEnabled(boolean cpuScalingEnabled) {
		this.cpuScalingEnabled = cpuScalingEnabled;
	}

	public ScalableApp getAssignedApp() {
		return assignedApp;
	}
	
	/**
	 * Returns the CPU load of the application depending on the chosen {@code thresholdPolicy} based on the {@code ApplicationMetrics}.
	 * @return CPU load of the application or -1 for an invalid {@code thresholdPolicy}
	 */
	public int getValueOfCpu() {
		if (thresholdPolicy.equals(ScalableApp.MAX)) 
			return maxOfCpu();
		if (thresholdPolicy.equals(ScalableApp.MIN))
			return minOfCpu();
		if (thresholdPolicy.equals(ScalableApp.MEAN))
			return meanOfCpu();
		
		return -1;
	}
	
	/**
	 * Computes the CPU load by searching for the maximum in the {@code ApplicationMetrics}.
	 * @return the maximum of CPU loads or 0 if no metrics are stored
	 */
	private int maxOfCpu() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= 0;
		for (int i = 0; i < metrics.size(); i++) {
			output = Math.max(output, metrics.get(i).getCpu());
		}
		return output;
	}
	
	/**
	 * Computes the CPU load by searching for the minimum in the {@code ApplicationMetrics}.
	 * @return the minimum of CPU loads or {@code Integer.Max_Value} if no metrics are stored
	 */
	private int minOfCpu() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= Integer.MAX_VALUE;
		for (int i = 0; i < metrics.size(); i++) {
			output = Math.min(output, metrics.get(i).getCpu());
		}
		return output;
	}
	
	/**
	 * Computes the CPU load by adding all values and dividing the sum with the count of {@code ApplicationMetrics}.
	 * @return the mean of CPU loads or 0 if no metrics are stored
	 */
	private int meanOfCpu() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= 0;
		if (metrics.size() > 0) {
			for (int i = 0; i < metrics.size(); i++) {
				output += metrics.get(i).getCpu();
			}
			output /= metrics.size();
		}
		return output;
	}
}
