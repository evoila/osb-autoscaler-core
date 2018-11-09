package de.evoila.cf.autoscaler.core.applications;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.api.binding.InvalidBindingException;
import de.evoila.cf.autoscaler.api.update.UpdateRequest;
import de.evoila.cf.autoscaler.core.exception.*;
import de.evoila.cf.autoscaler.core.kafka.producer.ProtobufProducer;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import de.evoila.cf.autoscaler.core.properties.DefaultValueBean;
import de.evoila.cf.autoscaler.core.scaling.Scaler;
import de.evoila.cf.autoscaler.core.scaling.prediction.Prediction;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.evoila.cf.autoscaler.kafka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Representation of an application of the platform. Holds all metrics and a Semaphore for synchronization.
 * @author Marius Berger
 */
public class ScalableApp {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LoggerFactory.getLogger(ScalableApp.class);
	
	/**
	 * Minimum for the {@code cooldownTime}.
	 */
	public static final int COOLDOWN_MIN = 0;
	/**
	 * Minimum for the {@code learningTimeMultiplier}.
	 */
	public static final int LEARNING_MULTIPLIER_MIN = 1;
	/**
	 * Minimum for the  {@code scalingIntervalMultiplier}.
	 */
	public static final int SCALING_INTERVAL_MULTIPLIER_MIN = 1;
	/**
	 * Default for the {@code learningTimeMultiplier}.
	 */
	public static final int LEARNING_STANDARD_TIME = 60 * 1000;

	/**
	 * Code representation for the "maximum" policy.
	 */
	public static final String MAX = "max";
	/**
	 * Code representation for the "minimum" policy.
	 */
	public static final String MIN = "min";
	/**
	 * Code representation for the "mean" policy.
	 */
	public static final String MEAN = "mean";
	
	/**
	 * Maximum of list size for the lists for {@code ContainerMetrics}, {@code ApplicationMetrics} and {@code HttpMetrics}.
	 */
	private final int maxListSize;
	
	/**
	 * Maximum of age a metric is allowed to have.
	 */
	private final long maxMetricAge;
	
	/**
	 * Boolean value, whether scaling as a whole is activated.
	 */
	private boolean scalingEnabled;
	/**
	 * Boolean value, whether predictions are considered or ignored.
	 */
	private boolean predictionScalingEnabled;
	/**
	 * Boolean value, whether the learning process is activated.
	 */
	private boolean learningEnabled;
	/**
	 * Boolean value, whether the billing interval consideration is activated.
	 */
	private boolean billingIntervalConsidered;
	
	/**
	 * Multiplier for the time between two checks.
	 */
	private int scalingIntervalMultiplier;
	/**
	 * Representation for passed time between the last and the next check.
	 */
	private int currentIntervalState;
	/**
	 * Minimum for the instance count in regards to scaling decisions.
	 */
	private int minInstances;
	/**
	 * Maximum for the instance count in regards to scaling decisions.
	 */
	private int maxInstances;
	/**
	 * Time after a scaling event, where scaling is disabled and metrics will be ignored.
	 */
	private int cooldownTime;
	/**
	 * Multiplier for the time where quotient supported scaling is disabled, but static scaling and learning is conducted.
	 */
	private int learningTimeMultiplier;
		
	/**
	 * Epoch time stamp of the last scaling event.
	 */
	private long lastScalingTime;
	/**
	 * Epoch time stamp of the start of the learning process.
	 */
	private long learningStartTime;
	
	/**
	 * Contains the ID of the resource, ID of the scaler, the creation time and the binding context.
	 */
	private Binding binding;
	
	/**
	 * Handles CPU concerned settings and methods.
	 * @see CpuWrapper
	 */
	private final CpuWrapper cpu;
	/**
	 * Handles request concerned settings and methods.
	 * @see RequestWrapper
	 */
	private final RequestWrapper request;
	/**
	 * Handles RAM concerned settings and methods.
	 * @see RamWrapper
	 */
	private final RamWrapper ram;
	/**
	 * Handles latency concerned settings and methods.
	 * @see LatencyWrapper
	 */
	private final LatencyWrapper latency;
	
	/**
	 * Stores incoming {@code HttpMetrics} before aggregation.
	 */
	private List<HttpMetric> httpMetrics;
	/**
	 * Stores incoming {@code ContainerMetrics} before aggregation.
	 */
	private List<ContainerMetric> instanceMetrics;
	/**
	 * Stores created {@code ApplicationMetrics} for later scaling purposes.
	 */
	private List<ApplicationMetric> applicationMetrics;
	
	/**
	 * Mutex to manage synchronization for the components accessing this {@code ScalableApp}.
	 */
	private Semaphore accessMutex;
	/**
	 * Current prediction for this application.
	 */
	private Prediction prediction;
	
	
	/**
	 * Constructor to create a {@code ScalableApp} object with a given {@code AppBlueprint}.
	 * @param bp {@code AppBlueprint} to get fields from
	 * @param kafkaProps {@code KafkaPropertiesBean} to get settings from for Kafka
	 * @param autoscalerProps {@code AutoscalerPropertiesBean} to get general settings from
	 * @param producer {@code ProtobufProducer} to publish on the message broker
	 */
	public ScalableApp(AppBlueprint bp, KafkaPropertiesBean kafkaProps, AutoscalerPropertiesBean autoscalerProps, ProtobufProducer producer) {
		binding = bp.getBinding();
		
		scalingEnabled = bp.isScalingEnabled();
		predictionScalingEnabled = bp.isPredictionScalingEnabled();
		learningEnabled = bp.isLearningEnabled();
		billingIntervalConsidered = bp.isBillingIntervalConsidered();
		
		scalingIntervalMultiplier = bp.getScalingIntervalMultiplier();
		currentIntervalState = bp.getCurrentIntervalState();
		minInstances = bp.getMinInstances();
		maxInstances = bp.getMaxInstances();
		cooldownTime = bp.getCooldownTime();
		learningTimeMultiplier = bp.getLearningTimeMultiplier();
			
		lastScalingTime = bp.getLastScalingTime();
		learningStartTime = bp.getLearningStartTime();
		
		cpu = new CpuWrapper(bp.getCpuThresholdPolicy(), bp.getCpuUpperLimit(), bp.getCpuLowerLimit(), bp.isCpuScalingEnabled(), this);
		request = new RequestWrapper(bp.getRequestThresholdPolicy(), bp.getMinQuotient(), bp.isQuotientBasedScalingEnabled(), this);
		request.setQuotient(bp.getQuotient());
		ram = new RamWrapper(bp.getRamThresholdPolicy(), bp.getRamUpperLimit(), bp.getRamLowerLimit(), bp.isRamScalingEnabled(), this);
		latency = new LatencyWrapper(bp.getLatencyUpperLimit(), bp.getLatencyLowerLimit(),bp.getLatencyThresholdPolicy(), bp.isLatencyScalingEnabled(), this);
		
		maxMetricAge = autoscalerProps.getMaxMetricAge();
		maxListSize = autoscalerProps.getMaxMetricListSize();
		initOtherInternalElements();
	}
	
	/**
	 * Constructor for a default {@code ScalableApp} with a given {@code appId}.
	 * @param binding binding information for the new {@code ScalableApp}
	 * @param kafkaProps {@code KafkaPropertiesBean} to get settings for Kafka
	 * @param defaults {@code DefaultValueBean} to get the default values
	 * @param autoscalerProps {@code AutoscalerPropertiesBean} to get general settings
	 * @param producer {@code ProtobufProducer} to publish on the message broker
	 */
	public ScalableApp(Binding binding, KafkaPropertiesBean kafkaProps, DefaultValueBean defaults
			, AutoscalerPropertiesBean autoscalerProps, ProtobufProducer producer) {
		
		this.binding = new Binding(binding);
		if (this.binding.getCreationTime() == 0)
			this.binding.setCreationTime(System.currentTimeMillis());
		
		cpu = new CpuWrapper("", -1, -1, false, this);
		ram = new RamWrapper("", -1, -1, false, this);
		request = new RequestWrapper("", -1, false, this);
		latency = new LatencyWrapper(-1, -1 , "", false, this);
		
		currentIntervalState = 0;
		lastScalingTime = this.binding.getCreationTime();
		learningStartTime = this.binding.getCreationTime();
		
		maxMetricAge = autoscalerProps.getMaxMetricAge();
		maxListSize = autoscalerProps.getMaxMetricListSize();
		initOtherInternalElements();
		initialiseDefaults(defaults);
	}
	
	/**
	 * Sets fields with the given default values.
	 * @param defaults {@code DefaultValueBean} to get the default values to set
	 */
	private void initialiseDefaults(DefaultValueBean defaults) {
		scalingEnabled = defaults.isScalingEnabled();
		predictionScalingEnabled = defaults.isPredictionScalingEnabled();
		scalingIntervalMultiplier = defaults.getScalingIntervalMultiplier();
		minInstances = defaults.getMinInstances();
		maxInstances = defaults.getMaxInstances();
		cooldownTime = defaults.getCooldownTime();
		learningEnabled = defaults.isLearningEnabled();
		billingIntervalConsidered = defaults.isBillingIntervalConsidered();
		learningTimeMultiplier = defaults .getLearningTimeMultiplier();
		
		cpu.setThresholdPolicy(defaults.getThresholdPolicy());
		cpu.setUpperLimit(defaults.getCpuUpperLimit());
		cpu.setLowerLimit(defaults.getCpuLowerLimit());
		cpu.setCpuScalingEnabled(defaults.isCpuScalingEnabled());
		
		ram.setThresholdPolicy(defaults.getThresholdPolicy());
		ram.setUpperLimit(defaults.getRamUpperLimit());
		ram.setLowerLimit(defaults.getRamLowerLimit());
		ram.setRamScalingEnabled(defaults.isRamScalingEnabled());
		
		latency.setThresholdPolicy(defaults.getThresholdPolicy());
		latency.setUpperLimit(defaults.getLatencyUpperLimit());
		latency.setLowerLimit(defaults.getLatencyLowerLimit());
		latency.setLatencyScalingEnabled(defaults.isLatencyScalingEnabled());
		
		request.setThresholdPolicy(defaults.getThresholdPolicy());
		request.setMinQuotient(defaults.getMinQuotient());
		request.setQuotientScalingEnabled(defaults.isQuotientScalingEnabled());
	}
	
	/**
	 * Sets working set fields.
	 */
	private void initOtherInternalElements() {
		httpMetrics = new LinkedList<HttpMetric>();
		instanceMetrics = new LinkedList<ContainerMetric>();
		applicationMetrics = new LinkedList<ApplicationMetric>();
		accessMutex = new Semaphore(1);
		prediction = null;
	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}
	
	public String getIdentifierStringForLogs() {
		return 	binding.getIdentifierStringForLogs();
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

	public int getScalingIntervalMultiplier() {
		return scalingIntervalMultiplier;
	}

	public void setScalingIntervalMultiplier(int scalingIntervalMultiplier) {
		this.scalingIntervalMultiplier = scalingIntervalMultiplier;
		if (currentIntervalState >= scalingIntervalMultiplier)
			currentIntervalState = scalingIntervalMultiplier - 1;
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

	public boolean isLearningEnabled() {
		return learningEnabled;
	}

	/**
	 * Sets the new value of {@link #learningEnabled} and of {@link #learningStartTime} if needed.
	 * @param learningEnabled boolean to set
	 */
	public void setLearningEnabled(boolean learningEnabled) {
		if (learningEnabled && !this.learningEnabled) {
			log.info(binding.getIdentifierStringForLogs() + " starts learning now.");
			setLearningStartTime(System.currentTimeMillis());
		} else if (!learningEnabled && this.learningEnabled) {
			log.info(binding.getIdentifierStringForLogs() + " stops learning now.");
		}
		this.learningEnabled = learningEnabled;
	}

	public boolean isBillingIntervalConsidered() {
		return billingIntervalConsidered;
	}

	public void setBillingIntervalConsidered(boolean billingIntervalConsidered) {
		this.billingIntervalConsidered = billingIntervalConsidered;
	}

	public long getLearningStartTime() {
		return learningStartTime;
	}

	public void setLearningStartTime(long learningStartTime) {
		this.learningStartTime = learningStartTime;
	}

	public CpuWrapper getCpu() {
		return cpu;
	}

	public RequestWrapper getRequest() {
		return request;
	}

	public RamWrapper getRam() {
		return ram;
	}

	public LatencyWrapper getLatency() {
		return latency;
	}

	public int getLearningTimeMultiplier() {
		return learningTimeMultiplier;
	}

	public void setLearningTimeMultiplier(int learningTimeMultiplier) {
		this.learningTimeMultiplier = learningTimeMultiplier;
	}
	
	public long getLastScalingTime() {
		return lastScalingTime;
	}

	public void setLastScalingTime(long lastScalingTime) {
		this.lastScalingTime = lastScalingTime;
	}
	
	public int getCurrentIntervalState() {
		return currentIntervalState;
	}

	public void setCurrentIntervalState(int currentIntervalState) {
		this.currentIntervalState = currentIntervalState;
	}
	
	public Prediction getPrediction() {
		return prediction;
	}

	public void setPrediction(Prediction prediction) {
		this.prediction = prediction;
		log.info("New prediction for: " + binding.getIdentifierStringForLogs());
	}
	
	public int getMaxListSize() {
		return maxListSize;
	}
	
	public long getMaxMetricAge() {
		return maxMetricAge;
	}
	//----- end getter and setter -----

	/**
	 * Triggers an update for the fields of this object, if the given {@code UpdateRequest} holds valid fields.
	 * @param updateRequest {@code UpdateRequest} to get fields from
	 * @return String Representation of the updated object.
	 * @throws LimitException for invalid limits and numbers
	 * @throws InvalidPolicyException for invalid policies
	 * @throws SpecialCharacterException for invalid names and IDs
	 * @throws TimeException for invalid time stamps and number concerning time.
	 * @throws InvalidWorkingSetException for an invalid working set
	 * @throws InvalidBindingException for an invalid binding
	 */
	public String update(UpdateRequest updateRequest) throws LimitException, InvalidPolicyException, SpecialCharacterException, TimeException, InvalidWorkingSetException, InvalidBindingException {
		AppBlueprint bp = this.getCopyOfBlueprint();
		Set<Integer> set = updateRequest.getAllSetElements();
		
		//create blueprint with the updated values
		
		if (set.contains(UpdateRequest.REQUEST_THRESHOLD_POLICY_IS_SET))
			bp.setRequestThresholdPolicy(updateRequest.getRequests().getThresholdPolicy());
		
		if (set.contains(UpdateRequest.CPU_THRESHOLD_POLICY_IS_SET))
			bp.setCpuThresholdPolicy(updateRequest.getCpu().getThresholdPolicy());
			
		if (set.contains(UpdateRequest.RAM_THRESHOLD_POLICY_IS_SET))
			bp.setRamThresholdPolicy(updateRequest.getRam().getThresholdPolicy());
		
		if (set.contains(UpdateRequest.LATENCY_THRESHOLD_POLICY_IS_SET))
			bp.setLatencyThresholdPolicy(updateRequest.getLatency().getThresholdPolicy());
			
		if (set.contains(UpdateRequest.SCALING_ENABLED_IS_SET)) 
			bp.setScalingEnabled(updateRequest.getScaling().isScalingEnabled());
		
		if (set.contains(UpdateRequest.CPU_SCALING_ENABLED_IS_SET))
			bp.setCpuScalingEnabled(updateRequest.getCpu().isCpuScalingEnabled());
		
		if (set.contains(UpdateRequest.RAM_SCALING_ENABLED_IS_SET))
			bp.setRamScalingEnabled(updateRequest.getRam().isRamScalingEnabled());
		
		if (set.contains(UpdateRequest.LATENCY_SCALING_ENABLED_IS_SET))
			bp.setLatencyScalingEnabled(updateRequest.getLatency().isLatencyScalingEnabled());
		
		if (set.contains(UpdateRequest.QUOTIENT_SCALING_ENABLED_IS_SET))
			bp.setQuotientBasedScalingEnabled(updateRequest.getRequests().isQuotientScalingEnabled());
		
		if (set.contains(UpdateRequest.PREDICTION_SCALING_ENABLED_IS_SET))
			bp.setPredictionScalingEnabled(updateRequest.getScaling().isPredictionScalingEnabled());
		
		if (set.contains(UpdateRequest.LEARNING_ENABLED_IS_SET)) 
			bp.setLearningEnabled(updateRequest.getLearning().isLearningEnabled());

		if (set.contains(UpdateRequest.BILLING_INTERVAL_CONSIDERED_IS_SET))
			bp.setBillingIntervalConsidered(updateRequest.getScaling().isBillingIntervalConsidered());
		
		if (set.contains(UpdateRequest.MIN_REQUEST_QUOTIENT_IS_SET))
			bp.setMinQuotient(updateRequest.getRequests().getMinRequestQuotient());
		
		if (set.contains(UpdateRequest.CPU_UPPER_LIMIT_IS_SET)) 
			bp.setCpuUpperLimit(updateRequest.getCpu().getUpperLimit());
		
		if (set.contains(UpdateRequest.CPU_LOWER_LIMIT_IS_SET))
			bp.setCpuLowerLimit(updateRequest.getCpu().getLowerLimit());
		
		if (set.contains(UpdateRequest.RAM_UPPER_LIMIT_IS_SET))
			bp.setRamUpperLimit(updateRequest.getRam().getUpperLimit());
		
		if (set.contains(UpdateRequest.RAM_LOWER_LIMIT_IS_SET))
			bp.setRamLowerLimit(updateRequest.getRam().getLowerLimit());
		
		if (set.contains(UpdateRequest.LATENCY_UPPER_LIMIT_IS_SET))
			bp.setLatencyUpperLimit(updateRequest.getLatency().getUpperLimit());
		
		if (set.contains(UpdateRequest.LATENCY_LOWER_LIMIT_IS_SET))
			bp.setLatencyLowerLimit(updateRequest.getLatency().getLowerLimit());
		
		if (set.contains(UpdateRequest.MIN_INSTANCES_IS_SET)) 
			bp.setMinInstances(updateRequest.getScaling().getMinInstances());
		
		if (set.contains(UpdateRequest.MAX_INSTANCES_IS_SET))
			bp.setMaxInstances(updateRequest.getScaling().getMaxInstances());
		
		if (set.contains(UpdateRequest.COOLDOWN_TIME_IS_SET))
			bp.setCooldownTime(updateRequest.getScaling().getCooldownTime());
		
		if (set.contains(UpdateRequest.LEARNING_TIME_MULTIPLIER_IS_SET))
			bp.setLearningTimeMultiplier(updateRequest.getLearning().getLearningTimeMultiplier());
		
		if (set.contains(UpdateRequest.SCALING_INTERVAL_MULTIPLIER_IS_SET))
			bp.setScalingIntervalMultiplier(updateRequest.getScaling().getScalingIntervalMultiplier());
		
		//check if created blueprint is valid
		de.evoila.cf.autoscaler.core.applications.ScalableAppService.isValid(bp);
		
		//update policies based on the blueprint
		updatePolicies(bp, updateRequest.getSetElements());
		
		if (set.size() == 1) {
			log.info("A policy of " + binding.getIdentifierStringForLogs() + " was updated.");
			log.debug("The update set contained following number: " + set.toString());
		} else if (set.size() > 1) {
			log.info(set.size() + " policies of " + binding.getIdentifierStringForLogs() + " were updated.");
			log.debug("The update set contained following numbers: " + set.toString());
		}
		return toString();
	}
	
	/**
	 * Updates the fields of this {@code ScalableApp} with the fields of the {@code AppBlueprint}, if it is declared in the {@code Set} to be changed.
	 * @param bp {@code AppBlueprint} to get fields from
	 * @param setElements {@code Set} containing information about which fields have to be updated
	 */
	private void updatePolicies(AppBlueprint bp, Set<Integer> setElements) {
		if (setElements != null) {
			if (setElements.contains(UpdateRequest.REQUEST_THRESHOLD_POLICY_IS_SET))
				request.setThresholdPolicy(bp.getRequestThresholdPolicy());
			
			if (setElements.contains(UpdateRequest.CPU_THRESHOLD_POLICY_IS_SET))
				cpu.setThresholdPolicy(bp.getCpuThresholdPolicy());
				
			if (setElements.contains(UpdateRequest.RAM_THRESHOLD_POLICY_IS_SET))
				ram.setThresholdPolicy(bp.getRamThresholdPolicy());
			
			if (setElements.contains(UpdateRequest.LATENCY_THRESHOLD_POLICY_IS_SET))
				latency.setThresholdPolicy(bp.getLatencyThresholdPolicy());
				
			if (setElements.contains(UpdateRequest.SCALING_ENABLED_IS_SET)) 
				setScalingEnabled(bp.isScalingEnabled());
			
			if (setElements.contains(UpdateRequest.CPU_SCALING_ENABLED_IS_SET)) 
				cpu.setCpuScalingEnabled(bp.isCpuScalingEnabled());
			
			if (setElements.contains(UpdateRequest.RAM_SCALING_ENABLED_IS_SET)) 
				ram.setRamScalingEnabled(bp.isRamScalingEnabled());
			
			if (setElements.contains(UpdateRequest.LATENCY_SCALING_ENABLED_IS_SET)) 
				latency.setLatencyScalingEnabled(bp.isLatencyScalingEnabled());
			
			if (setElements.contains(UpdateRequest.QUOTIENT_SCALING_ENABLED_IS_SET))
				request.setQuotientScalingEnabled(bp.isQuotientBasedScalingEnabled());
			
			if (setElements.contains(UpdateRequest.PREDICTION_SCALING_ENABLED_IS_SET))
				setPredictionScalingEnabled(bp.isPredictionScalingEnabled());
			
			if (setElements.contains(UpdateRequest.LEARNING_ENABLED_IS_SET)) 
				setLearningEnabled(bp.isLearningEnabled());

			if (setElements.contains(UpdateRequest.BILLING_INTERVAL_CONSIDERED_IS_SET))
				setBillingIntervalConsidered(bp.isBillingIntervalConsidered());
			
			if (setElements.contains(UpdateRequest.MIN_REQUEST_QUOTIENT_IS_SET))
				request.setMinQuotient(bp.getMinQuotient());
			
			if (setElements.contains(UpdateRequest.CPU_UPPER_LIMIT_IS_SET)) 
				cpu.setUpperLimit(bp.getCpuUpperLimit());
			
			if (setElements.contains(UpdateRequest.CPU_LOWER_LIMIT_IS_SET))
				cpu.setLowerLimit(bp.getCpuLowerLimit());
			
			if (setElements.contains(UpdateRequest.RAM_UPPER_LIMIT_IS_SET))
				ram.setUpperLimit(bp.getRamUpperLimit());
			
			if (setElements.contains(UpdateRequest.RAM_LOWER_LIMIT_IS_SET))
				ram.setLowerLimit(bp.getRamLowerLimit());
			
			if (setElements.contains(UpdateRequest.LATENCY_UPPER_LIMIT_IS_SET))
				latency.setUpperLimit(bp.getLatencyUpperLimit());
			
			if (setElements.contains(UpdateRequest.LATENCY_LOWER_LIMIT_IS_SET))
				latency.setLowerLimit(bp.getLatencyLowerLimit());
			
			if (setElements.contains(UpdateRequest.MIN_INSTANCES_IS_SET)) 
				setMinInstances(bp.getMinInstances());
			
			if (setElements.contains(UpdateRequest.MAX_INSTANCES_IS_SET))
				setMaxInstances(bp.getMaxInstances());
			
			if (setElements.contains(UpdateRequest.COOLDOWN_TIME_IS_SET))
				setCooldownTime(bp.getCooldownTime());
			
			if (setElements.contains(UpdateRequest.LEARNING_TIME_MULTIPLIER_IS_SET))
				setLearningTimeMultiplier(bp.getLearningTimeMultiplier());
			
			if (setElements.contains(UpdateRequest.SCALING_INTERVAL_MULTIPLIER_IS_SET))
				setScalingIntervalMultiplier(bp.getScalingIntervalMultiplier());
		}
	}
	
	/**
	 * Creates and returns a {@code AppBlueprint} of this {@code ScalableApp}.
	 * @return {@code AppBlueprint} of this {@code ScalableApp}.
	 */
	public AppBlueprint getCopyOfBlueprint() {
		return new AppBlueprint(this);
	}
	
	/**
	 * Empties the list for {@code ApplicationMetrics}. {@link #applicationMetrics}
	 */
	public void resetApplicationMetricLists() {
		applicationMetrics = new LinkedList<ApplicationMetric>();
	}

	/**
	 * Empties the list for {@code HttpMetrics}. {@link #httpMetrics}
	 */
	public void resetHttpMetricList() {
		httpMetrics = new LinkedList<HttpMetric>();
	}
	
	/**
	 * Empties the list for {@code ContainerMetrics}. {@link #instanceMetrics}
	 */
	public void resetContainerMetricsList() {
		instanceMetrics = new LinkedList<ContainerMetric>();
	}
	
	/**
	 * Adds a {@code Metric} to the related list.
	 * @param metric {@code Metric} to add
	 */
	public void addMetric(AutoscalerMetric metric) {
		if (metric == null) {
			return;
		}
		
		if (metric.getType() == AutoscalerMetric.TYPE_HTTP) {
			try {
				addHttpMetric(metric.getHttpMetric());
			} catch (InvalidMetricTypeException ex) {
				log.error(ex.getMessage());
			}
			
		} else if( metric.getType() == AutoscalerMetric.TYPE_CONTAINER) {
			try {
				addInstanceContainerMetric(metric.getContainerMetric());
			} catch (InvalidMetricTypeException ex) {
				log.error(ex.getMessage());
			}
		} else if ( metric.getType() == AutoscalerMetric.TYPE_APPLICATION) {
			try {
				addApplicationMetric(metric.getApplicationMetric());
			} catch (InvalidMetricTypeException ex) {
					log.error(ex.getMessage());
			}
		}
	}
	
	/**
	 * Adds a {@code HttpMetric} to the {@link #httpMetrics} and deletes latest {@code HttpMetric}, if {@link #maxListSize} is surpassed.
	 * @param metric {@code HttpMetric} to add
	 */
	private void addHttpMetric(HttpMetric metric) {
		httpMetrics.add(metric);
		while (httpMetrics.size() > maxListSize) {
			httpMetrics.remove(0);
		}
	}
	
	/**
	 * Adds a {@code ContainerMetric} to the {@link #instanceMetrics} and deletes latest {@code ContainerMetric}, if {@link #maxListSize} is surpassed.
	 * @param metric {@code ContainerMetric} to add
	 */
	private void addInstanceContainerMetric(ContainerMetric metric) {
		instanceMetrics.add(metric);
		while (instanceMetrics.size() > maxListSize) {
			instanceMetrics.remove(0);
		}
	}
	
	/**
	 * Adds a {@code ApplicationMetric} to the {@link #instanceMetrics} and deletes latest {@code ApplicationMetric}, if {@link #maxListSize} is surpassed.
	 * @param metric {@code ApplicationMetric} to add
	 */
	private void addApplicationMetric(ApplicationMetric metric) {
		if (!isInCooldown()) {
			applicationMetrics.add(metric);
			while (applicationMetrics.size() > maxListSize) {
				applicationMetrics.remove(0);
			}
		}
	}
	
	/**
	 * Creates and returns a deep copy of {@link #httpMetrics}.
	 * @return deep copy of {@link #httpMetrics}
	 */
	public List<HttpMetric> getCopyOfHttpMetricsList() {
		List<HttpMetric> output = new LinkedList<HttpMetric>();
		for (int i = 0; i < httpMetrics.size(); i++) {
			output.add(new HttpMetric(httpMetrics.get(i)));
		}
		return output;
	}
	
	/**
	 * Creates and returns a deep copy of {@link #instanceMetrics}.
	 * @return deep copy of {@link #instanceMetrics}
	 */
	public List<ContainerMetric> getCopyOfInstanceContainerMetricsList() {
		List<ContainerMetric> output = new LinkedList<ContainerMetric>(); 
		for (int i = 0; i < instanceMetrics.size(); i++) {
			output.add(new ContainerMetric(instanceMetrics.get(i)));
		}
		return output;
	}
	
	/**
	 * Creates and returns a deep copy of {@link #applicationMetrics}.
	 * @return deep copy of {@link #applicationMetrics}
	 */
	public List<ApplicationMetric> getCopyOfApplicationMetricsList() {
		List<ApplicationMetric> output = new LinkedList<ApplicationMetric>(); 
		for (int i = 0; i < applicationMetrics.size(); i++) {
			output.add(new ApplicationMetric(applicationMetrics.get(i)));
		}
		return output;
	}
	/**
	 * Computes the current instance count of the application based on {@link #instanceMetrics} or {@link #applicationMetrics}.
	 * If {@link #instanceMetrics} has no {@code ContainerMetrics}, {@link #applicationMetrics} will be used.
	 * @return instance count of the application or a number representing an error from {@link Scaler}
	 */
	public int getCurrentInstanceCount() {
		int output = Scaler.NO_METRIC_ERROR_INSTANCE_COUNT;
		if (instanceMetrics.size() > 0) {
			for (int i = 0; i < instanceMetrics.size(); i++) {
				output = Math.max(output, instanceMetrics.get(i).getInstanceIndex());
			}
			output++; // add one because instanceIndex starts at 0
		} else if (applicationMetrics.size() > 0 ) {
			output = applicationMetrics.get(applicationMetrics.size()-1).getInstanceCount();
		}
		return output;
	}
	
	/**
	 * Finishes the current interval and checks whether it is time to enter the scaling routine for this application.
	 * @return Boolean value whether it is time to enter the scaling routine for this application after finishing an interval
	 */
	public boolean timeToCheck() {
		currentIntervalState = (currentIntervalState+1) % scalingIntervalMultiplier;
		return isTimeToCheckScaling();
	}
	
	/**
	 * Checks whether it is time to enter the scaling routine for this application.
	 * @return Boolean value whether it is time to enter the scaling routine for this application
	 */
	public boolean isTimeToCheckScaling() {
		return currentIntervalState == 0;
	}
	
	/**
	 * Checks whether this application is in its cool down time.
	 * @return Boolean value whether it is in cool down time
	 */
	public boolean isInCooldown() {
		return (System.currentTimeMillis() - getLastScalingTime() < getCooldownTime());
	}
	
	/**
	 * Checks whether this application is in its learning time.
	 * @return Boolean value whether it is in learning time
	 */
	public boolean isInLearningTime() {
		return System.currentTimeMillis() - getLearningStartTime() <  getLearningTimeMultiplier() * ScalableApp.LEARNING_STANDARD_TIME;
	}
	
	/**
	 * Calls the acquire() method of the underlying {@link #accessMutex}.
	 * @throws InterruptedException - if the current thread is interrupted
	 */
	public void acquire() throws InterruptedException {
		accessMutex.acquire();
	}
	
	/**
	 * Calls the release() method of the underlying {@link #accessMutex}.
	 */
	public void release() {
		if (accessMutex.availablePermits() <= 1)
			accessMutex.release();
	}
}
