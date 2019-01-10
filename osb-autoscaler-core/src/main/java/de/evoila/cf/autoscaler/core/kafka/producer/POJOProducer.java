package de.evoila.cf.autoscaler.core.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.scaling.ScalingAction;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.evoila.cf.autoscaler.kafka.messages.ApplicationMetric;
import de.evoila.cf.autoscaler.kafka.messages.ContainerMetric;
import de.evoila.cf.autoscaler.kafka.messages.HttpMetric;
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog;
import de.evoila.cf.autoscaler.kafka.model.BindingInformation;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * A Producer to publish with jackson serialized objects on Kafka.
 * For some classes a dedicated method exists to pick a predetermined topic.
 * @author Marius Berger
 *
 */
@Service
public class POJOProducer {

	/**
	 * Logger of this class.
	 */
	private Logger log = LoggerFactory.getLogger(POJOProducer.class);
	
	/**
	 * Property Bean for Kafka Settings.
	 */
	private KafkaPropertiesBean kafkaProps;
	
	/**
	 * IP or URL of Kafka and its port.
	 */
	private String host;
	/**
	 * Id of the group to join.
	 */
	private String groupId;

	/**
	 * Jackson object mapper to get byte from POJOs.
	 */
	private ObjectMapper objectMapper;
	
	/**
	 * Underlying Kafka producer.
	 */
	private org.apache.kafka.clients.producer.Producer<String,byte[]> producer;
	
	/** 
	 * Constructor for Spring to inject this service.
	 */
	public POJOProducer(KafkaPropertiesBean kafkaProps) {this.kafkaProps = kafkaProps;}
	
	/**
	 * Set up the fields of the producer after construction by Spring.
	 */
	@PostConstruct
	private void init() {
		host = kafkaProps.getHost() + ":" + kafkaProps.getPort();
		groupId = kafkaProps.getProducerGroupId();
		objectMapper = new ObjectMapper();

        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        configProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        producer = new KafkaProducer<>(configProperties);
	}

	/**
	 * Publish a {@linkplain ScalingLog} on Kafka.
	 * @param sc {@linkplain ScalingAction} to get fields from
	 * @param timestamp time stamp for the {@linkplain ScalingLog}
	 */
	public void produceScalingLog(ScalingAction sc, long timestamp) {
		ScalableApp app = sc.getApp();

		ScalingLog scalingLog = new ScalingLog(
				timestamp,
				app.getBinding().getResourceId(),
				app.getBinding().getResourceName(),
				sc.getReason(),
				sc.getOldInstances(),
				sc.getNewInstances(),
				app.getMaxInstances(),
				app.getMinInstances(),
				app.getCpu().getValueOfCpu(),
				app.getCpu().getUpperLimit(),
				app.getCpu().getLowerLimit(),
				app.getRam().getValueOfRam(),
				app.getRam().getUpperLimit(),
				app.getRam().getLowerLimit(),
				app.getRequest().getValueOfHttpRequests(),
				app.getLatency().getValueOfLatency(),
				app.getLatency().getUpperLimit(),
				app.getLatency().getLowerLimit(),
				app.getRequest().getQuotient(),
				sc.getReasonDescription()
		);
		log.debug("ScalingLog: " + scalingLog.toString());
		produce(kafkaProps.getScalingTopic(), scalingLog);
	}

	/**
	 * Publish a {@linkplain HttpMetric} on Kafka on the given topic.
	 * @param metric {@linkplain HttpMetric} to publish
	 */
	public void produceHttpMetric(HttpMetric metric) {
		produce(kafkaProps.getMetricHttpTopic(), metric);
	}

	/**
	 * Publish a {@linkplain ContainerMetric} on Kafka.
	 * @param containerMetric {@linkplain ContainerMetric} to publish
	 */
	public void produceContainerMetric(ContainerMetric containerMetric) {
		produce(kafkaProps.getMetricContainerTopic(), containerMetric);
	}
	
	/**
	 * Publish a {@linkplain ApplicationMetric} on Kafka.
	 * @param applicationMetric {@linkplain ApplicationMetric} to publish}
	 */
	public void produceApplicationMetric(ApplicationMetric applicationMetric) {
		produce(kafkaProps.getMetricApplicationTopic(), applicationMetric);
	}

	public void produceBindingInformation(BindingInformation bindingInformation) {
		produce(kafkaProps.getBindingTopic(), bindingInformation);
	}

	public void produce(String topic, Object jacksonSerializableObject) {
		try {
			byte[] output = objectMapper.writeValueAsBytes(jacksonSerializableObject);
			ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(topic, output);
			producer.send(rec);
		} catch (JsonProcessingException ex) {
			log.error("Failed to generate byte array from "+jacksonSerializableObject.getClass().getSimpleName()+" object.", ex);
		}
	}
}
