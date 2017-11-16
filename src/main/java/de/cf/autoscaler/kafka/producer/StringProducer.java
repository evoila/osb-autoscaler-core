package de.cf.autoscaler.kafka.producer;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.cf.autoscaler.kafka.KafkaPropertiesBean;


/**
 * A Producer to publish events about bindings on Kafka.
 * @author Marius Berger
 *
 */
@Service
public class StringProducer {

	/**
	 * Constant for symbolizing creation as action.
	 */
	public static final String CREATING = "creating";
	/**
	 * Constant for symbolizing deletion as action.
	 */
	public static final String DELETING = "deleting";
	
	/**
	 * Constant for symbolizing loading from the database as action.
	 */
	public static final String LOADING = "loading";
	
	/**
	 * Property Bean for Kafka Settings.
	 */
	@Autowired
	private KafkaPropertiesBean kafkaProps;
	
	/**
	 * IP or URL of Kafka and its port.
	 */
	private String host;
	/**
	 * Topic to publish binding Strings on.
	 */
	private String bindingTopic;
	
	/**
	 * Underlying Kafka producer.
	 */
	private org.apache.kafka.clients.producer.Producer<String,String> producer;
	
	/** 
	 * Default constructor for Spring to inject this service.
	 */
	public StringProducer() { }
	
	/**
	 * Set up the fields of the producer after construction by Spring.
	 */
	@PostConstruct
	private void init() {
		host = kafkaProps.getHost()+":"+kafkaProps.getPort();
		bindingTopic = kafkaProps.getBindingTopic();
		
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        configProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(configProperties);
	}

	/**
	 * Publish a binding String on Kafka.
	 * @param deleting boolean if the message is deletion event
	 * @param appId id of the application
	 */
	/**
	 * Publish a binding action String on Kafka.
	 * @param action type of action what was performed
	 * @param bindingId id of the binding
	 * @param resourceId id of the resource
	 * @param scalerId id of the scaler
	 */
	public void produceBinding(String action, String bindingId, String resourceId, String scalerId) {
		String output = "binding: "+ bindingId + ", resourceId: " + resourceId + ", scalerId: " + scalerId + ", action: " + action;
        ProducerRecord<String, String> rec = new ProducerRecord<String, String>(bindingTopic, output);
        producer.send(rec);
	}
}
