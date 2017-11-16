package de.cf.autoscaler.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A bean for storing properties dedicated to the Autoscaler.
 * Spring fills the fields at the start of the Autoscaler with values out of the properties file.
 * @author Marius Berger
 *
 */
@Service
public class AutoscalerPropertiesBean {

	/**
	 * Maximum count a List storing Metrics is allowed the have.
	 */
	@Value("${scaler.maxMetricListSize}")
	private int maxMetricListSize;
	
	/**
	 * Maximum age a Metric is allowed the have.
	 */
	@Value("${scaler.maxMetricAge}")
	private long maxMetricAge;
	
	/**
	 * Constructor for Spring to inject the bean.
	 */
	public AutoscalerPropertiesBean() { }

	public int getMaxMetricListSize() {
		return maxMetricListSize;
	}

	public void setMaxMetricListSize(int maxMetricListSize) {
		this.maxMetricListSize = maxMetricListSize;
	}

	public long getMaxMetricAge() {
		return maxMetricAge;
	}

	public void setMaxMetricAge(int maxMetricAge) {
		this.maxMetricAge = maxMetricAge;
	}
}
