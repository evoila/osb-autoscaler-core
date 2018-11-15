package de.evoila.cf.autoscaler.core.manager;

import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.kafka.consumer.ContainerMetricConsumer;
import de.evoila.cf.autoscaler.core.kafka.consumer.HttpMetricConsumer;
import de.evoila.cf.autoscaler.core.kafka.consumer.PredictionConsumer;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * Manager for starting and managing consumer for Kafka.
 * @author Marius Berger
 *
 */
@Service
public class ConsumerManager {
	
	/**
	 * Logger of this class.
	 */
	private Logger log = LoggerFactory.getLogger(ConsumerManager.class);
	
	/**
	 * Manager for {@linkplain ScalableApp}
	 */
	@Autowired
	ScalableAppManager appManager;
	
	/**
	 * Property Bean for Kafka Settings.
	 */
	@Autowired
    KafkaPropertiesBean kafkaProps;
	
	/**
	 * Property Bean for Settings of the Autoscaler.
	 */
	@Autowired
	AutoscalerPropertiesBean scalerProperties;
	
	/**
	 * Id of the group for {@linkplain ContainerMetricConsumer}.
	 */
	private String containerConsumerGroupId;
	/**
	 * Id of the group for {@linkplain HttpMetricConsumer}.
	 */
	private String httpConsumerGroupId;
	/**
	 * Id of the group for {@linkplain PredictionConsumer}.
	 */
	private String predictionConsumerGroupId;
	
	/**
	 * List of managed {@linkplain ContainerMetricConsumer}.
	 */
	private List<ContainerMetricConsumer> containerConsumers;
	/**
	 * List of managed {@linkplain HttpMetricConsumer}.
	 */
	private List<HttpMetricConsumer> requestConsumers;
	/**
	 * List of managed {@linkplain PredictionConsumer}.
	 */
	private PredictionConsumer predictionConsumer;

	/**
	 * Constructor to set up the manager.
	 */
	public ConsumerManager() { 
		containerConsumers = new LinkedList<ContainerMetricConsumer>();
		requestConsumers = new LinkedList<HttpMetricConsumer>();
	}
	
	/**
	 * Adds a {@linkplain ContainerMetricConsumer} and starts it.
	 */
	public void addContainerConsumer() {
		ContainerMetricConsumer newConsumer = new ContainerMetricConsumer(kafkaProps.getMetricContainerTopic()
				, containerConsumerGroupId, kafkaProps.getHost(), kafkaProps.getPort(), scalerProperties.getMaxMetricAge(), appManager);
		containerConsumers.add(newConsumer);
		newConsumer.startConsumer();
		log.info("New container consumer #"+containerConsumers.size()+" started.");
	}
	
	/**
	 * Adds a {@linkplain HttpMetricConsumer} and starts it.
	 */
	public void addRequestConsumer() {
		HttpMetricConsumer newConsumer = new HttpMetricConsumer(kafkaProps.getMetricHttpTopic()
				, httpConsumerGroupId, kafkaProps.getHost(), kafkaProps.getPort(), scalerProperties.getMaxMetricAge(), appManager);
		requestConsumers.add(newConsumer);
		newConsumer.startConsumer();
		log.info("New request consumer #"+requestConsumers.size()+" started.");
	}
	
	/**
	 * Sets up the {@linkplain PredictionConsumer} and starts it.
	 */
	public void addPredictionConsumer() {
		predictionConsumer = new PredictionConsumer(kafkaProps.getPredicTopic(), predictionConsumerGroupId
				, kafkaProps.getHost(), kafkaProps.getPort(), appManager);
		predictionConsumer.startConsumer();
		log.info("New prediction consumer started.");
	}
	
	/**
	 * Adds and starts the in the {@linkplain KafkaPropertiesBean} defined amount of consumers.
	 * @see KafkaPropertiesBean
	 */
	@PostConstruct
	public void initConsumers() {
		containerConsumerGroupId = kafkaProps.getContainerConsumerGroupId();
		httpConsumerGroupId = kafkaProps.getHttpConsumerGroupId();
		predictionConsumerGroupId = kafkaProps.getPredictionConsumerGroupId();
		
		for (int i = 0; i < kafkaProps.getContainerConsumerCount(); i++) {
			addContainerConsumer();
		}
		for (int i = 0; i < kafkaProps.getRequestConsumerCount(); i++) {
			addRequestConsumer();
		}
		addPredictionConsumer();
	}
	
	/**
	 * Stops all consumers.
	 */
	public void shutdown() {
		for (int i = 0; i < containerConsumers.size(); i++) {
			containerConsumers.get(i).stopConsumer();
		}
		for (int i = 0; i < requestConsumers.size(); i++) {
			requestConsumers.get(i).stopConsumer();
		}
		predictionConsumer.stopConsumer();
	}
}
