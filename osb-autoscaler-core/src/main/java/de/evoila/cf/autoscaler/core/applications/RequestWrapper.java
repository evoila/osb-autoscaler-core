package de.evoila.cf.autoscaler.core.applications;


import de.evoila.cf.autoscaler.core.scaling.Scaler;
import de.evoila.cf.autoscaler.kafka.messages.ApplicationMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Wraps settings and methods for HTTP request handling of an application.
 * @author Marius Berger
 */
public class RequestWrapper {

	/**
	 * Logger for this class.
	 */
	private static Logger log = LoggerFactory.getLogger(RequestWrapper.class);
	
	/**
	 * Current quotient ( request count divided by the instance count) of the application.
	 */
	private int quotient;
	
	/**
	 * Minimum which the {@code quotient} can start at. 0 by default.
	 */
	private int minQuotient;
	
	/**
	 * Policy to describe how the request count of the application is computed.
	 */
	private String thresholdPolicy;
	
	/**
	 * Boolean value, whether scaling with the help of the quotient is activated.
	 */
	private boolean quotientScalingEnabled;
	
	/**
	 * {@code ScalableApp} to which this object is bound.
	 */
	private final de.evoila.cf.autoscaler.core.applications.ScalableApp assignedApp;
	
	/**
	 * Constructor with all changeable fields.
	 * @param thresholdPolicy {@linkplain #thresholdPolicy}
	 * @param minQuotient {@linkplain #minQuotient}
	 * @param quotientScalingEnabled {@linkplain #quotientScalingEnabled}
	 * @param assignedApp {@linkplain #assignedApp}
	 */
	public RequestWrapper(String thresholdPolicy, int minQuotient, boolean quotientScalingEnabled, de.evoila.cf.autoscaler.core.applications.ScalableApp assignedApp) {
		this.thresholdPolicy = thresholdPolicy;
		this.minQuotient = minQuotient;
		this.quotient = minQuotient;
		this.quotientScalingEnabled = quotientScalingEnabled;
		this.assignedApp = assignedApp;
	}
	
	public String getThresholdPolicy() {
		return thresholdPolicy;
	}

	public void setThresholdPolicy(String thresholdPolicy) {
		this.thresholdPolicy = thresholdPolicy;
	}

	public int getQuotient() {
		return quotient;
	}

	public void setQuotient(int quotient) {
		if (quotient < minQuotient)
			this.quotient = minQuotient;
		else
			this.quotient = quotient;
	}
	
	public int getMinQuotient() {
		return minQuotient;
	}

	/**
	 * Sets the {@code minQuotient} to the given number. Also triggers change of the {@code quotient} if it is lower than the new minQuotient.
	 * @param minQuotient minimum to set
	 */
	public void setMinQuotient(int minQuotient) {
		this.minQuotient = minQuotient;
		if (quotient < minQuotient)
			quotient = minQuotient;
	}
	
	public boolean isQuotientScalingEnabled() {
		return quotientScalingEnabled;
	}

	public void setQuotientScalingEnabled(boolean quotientScalingEnabled) {
		this.quotientScalingEnabled = quotientScalingEnabled;
	}

	public de.evoila.cf.autoscaler.core.applications.ScalableApp getAssignedApp() {
		return assignedApp;
	}
	
	/**
	 * Resets the current quotient to the {@code minQuotient}
	 */
	public void resetQuotient() {
		if (minQuotient > 0) {
			quotient = minQuotient;
		}
		else {
			quotient = 0;
		}
	}
	
	/**
	 * Computes the {@code quotient} and if the new {@code quotient} is bigger than the old one, raises it by half of the difference, . 
	 */
	public void setRequestsPerInstance() {
		int instances = assignedApp.getCurrentInstanceCount();
		
		if (instances == Scaler.NO_METRIC_ERROR_INSTANCE_COUNT
				|| assignedApp.getCpu().getValueOfCpu() > assignedApp.getCpu().getUpperLimit()
				|| assignedApp.getRam().getValueOfRam() > assignedApp.getRam().getUpperLimit()
				|| assignedApp.getLatency().getValueOfLatency() > assignedApp.getLatency().getUpperLimit())
			return;
		
		int requests = getValueOfHttpRequests();
		
		// ---- debug syso ----
		String identifier = assignedApp.getIdentifierStringForLogs();
		log.debug("Quotient --- "+ identifier + " - Requests: " + requests + " , policy: " + thresholdPolicy + ", per Instance: " + requests/instances + " , old: "+quotient);
		// ---- end debug syso ----
		
		
		if (requests / instances > getQuotient()) {
			setQuotient((requests / instances+ getQuotient())/2);
			log.debug("New Threshold: " + getQuotient() + " - for " + assignedApp.getIdentifierStringForLogs());
		}
	}
	
	/**
	 * Returns the request count of the application depending on the chosen {@code thresholdPolicy} based on the {@code ApplicationMetrics}.
	 * @return CPU load of the application or -1 for an invalid {@code thresholdPolicy}
	 */
	public int getValueOfHttpRequests() {
		if (thresholdPolicy.equals(de.evoila.cf.autoscaler.core.applications.ScalableApp.MAX))
			return maxOfHTTPRequests();
		if (thresholdPolicy.equals(de.evoila.cf.autoscaler.core.applications.ScalableApp.MIN))
			return minOfHTTPRequests();
		if (thresholdPolicy.equals(de.evoila.cf.autoscaler.core.applications.ScalableApp.MEAN))
			return meanOfHTTPRequests();
		
		return -1;
	}
	
	/**
	 * Computes the request count by searching for the maximum in the {@code ApplicationMetrics}.
	 * @return the maximum of request counts or 0 if no metrics are stored
	 */
	private int maxOfHTTPRequests() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= 0;
		for (int i = 0; i < metrics.size(); i++) {
			
			output = Math.max(output, metrics.get(i).getRequests());
		}
		return output;
	}
	
	/**
	 * Computes the request count by searching for the minimum in the {@code ApplicationMetrics}.
	 * @return the minimum of request counts or {@code Integer.Max_Value} if no metrics are stored
	 */
	private int minOfHTTPRequests() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= Integer.MAX_VALUE;
		for (int i = 0; i < metrics.size(); i++) {
			output = Math.min(output, metrics.get(i).getRequests());
		}
		return output;
	}
	
	/**
	 * Computes the request count by adding all values and dividing the sum with the count of {@code ApplicationMetrics}.
	 * @return the mean of request counts or 0 if no metrics are stored
	 */
	private int meanOfHTTPRequests() {
		List<ApplicationMetric> metrics = assignedApp.getCopyOfApplicationMetricsList();
		int output= -1;
		if (metrics.size() > 0) {
			for (int i = 0; i < metrics.size(); i++) {
				output += metrics.get(i).getRequests();
			}
			output /= metrics.size();
		}
		return output;
		
	}
}
