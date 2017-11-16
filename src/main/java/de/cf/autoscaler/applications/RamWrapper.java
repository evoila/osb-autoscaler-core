package de.cf.autoscaler.applications;

import java.util.List;

import de.cf.autoscaler.kafka.messages.ApplicationMetric;

/**
 * Wraps settings and methods for RAM handling of an application.
 * @author Marius Berger
 */
public class RamWrapper {

	/**
	 * The upper limit for the RAM load to reach before scaling up.
	 */
	private long upperLimit;
	
	/**
	 * The lower limit for the RAM load to reach before scaling down.
	 */
	private long lowerLimit;
	
	/**
	 * Policy to describe how the RAM load of the application is computed.
	 */
	private String thresholdPolicy;
	
	/**
	 * Boolean value, whether scaling based on RAM load is activated.
	 */
	private boolean ramScalingEnabled;
	
	/**
	 * {@code ScalableApp} to which this object is bound.
	 */
	private final ScalableApp assignedApp;

	/**
	 * Constructor with all fields.
	 */
	
	/**
	 * Constructor with all fields.
	 * @param thresholdPolicy {@linkplain #thresholdPolicy}
	 * @param upperLimit {@linkplain #upperLimit}
	 * @param lowerLimit {@linkplain #lowerLimit}
	 * @param ramScalingEnabled {@linkplain #ramScalingEnabled}
	 * @param assignedApp {@linkplain #assignedApp}
	 */
	public RamWrapper(String thresholdPolicy, long upperLimit, long lowerLimit, boolean ramScalingEnabled, ScalableApp assignedApp) {
		this.thresholdPolicy = thresholdPolicy;
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
		this.ramScalingEnabled = ramScalingEnabled;
		this.assignedApp = assignedApp;
	}

	public String getThresholdPolicy() {
		return thresholdPolicy;
	}

	public void setThresholdPolicy(String thresholdPolicy) {
		this.thresholdPolicy = thresholdPolicy;
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

	public ScalableApp getAssignedApp() {
		return assignedApp;
	}

	public boolean isRamScalingEnabled() {
		return ramScalingEnabled;
	}

	public void setRamScalingEnabled(boolean ramScalingEnabled) {
		this.ramScalingEnabled = ramScalingEnabled;
	}

	/**
	 * Returns the RAM load of the application depending on the chosen {@code thresholdPolicy} based on the {@code ApplicationMetrics}.
	 * @return RAM load of the application or -1 for an invalid {@code thresholdPolicy}
	 */
	public long getValueOfRam() {
		if (thresholdPolicy.equals(ScalableApp.MAX)) 
			return maxOfRam();
		if (thresholdPolicy.equals(ScalableApp.MIN))
			return minOfRam();
		if (thresholdPolicy.equals(ScalableApp.MEAN))
			return meanOfRam();
		
		return -1;
	}
	
	/**
	 * Computes the RAM load by searching for the maximum in the {@code ApplicationMetrics}.
	 * @return the maximum of RAM loads or 0 if no metrics are stored
	 */
	private long maxOfRam() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		long output= 0;
		for (int i = 0; i < metrics.size(); i++) {
				output = Math.max(output, metrics.get(i).getRam());
		}
		return output;
	}
	
	/**
	 * Computes the RAM load by searching for the minimum in the {@code ApplicationMetrics}.
	 * @return the minimum of RAM loads or {@code Integer.Max_Value} if no metrics are stored
	 */
	private long minOfRam() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		long output= Integer.MAX_VALUE;
		for (int i = 0; i < metrics.size(); i++) {
			output = Math.min(output, metrics.get(i).getRam());
		}
		return output;
	}
	
	/**
	 * Computes the RAM load by adding all values and dividing the sum with the count of {@code ApplicationMetrics}.
	 * @return the mean of RAM loads or 0 if no metrics are stored
	 */
	private long meanOfRam() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		long output= 0;
		if (metrics.size() > 0) {
			for (int i = 0; i < metrics.size(); i++) {
				output += metrics.get(i).getRam();
			}
			output /= metrics.size();
		}
		return output;
	}
}
