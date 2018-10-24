package de.evoila.cf.autoscaler.core.kafka.producer;

import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;


/**
 * A Producer to publish events about bindings on Kafka.
 * @author Marius Berger
 *
 */
@Service
public class StringProducer {

	/**
	 * Property Bean for Kafka Settings.
	 */
	private KafkaPropertiesBean kafkaProps;
	
	/**
	 * IP or URL of Kafka and its port.
	 */
	private String host;

	/**
	 * Underlying Kafka producer.
	 */
	private org.apache.kafka.clients.producer.Producer<String,String> producer;
	
	/** 
	 * Default constructor for Spring to inject this service.
	 */
	public StringProducer(KafkaPropertiesBean kafkaProps) {this.kafkaProps = kafkaProps; }
	
	/**
	 * Set up the fields of the producer after construction by Spring.
	 */
	@PostConstruct
	private void init() {
		host = kafkaProps.getHost()+":"+kafkaProps.getPort();

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
	 * Publishs a message in the String format on the given topic.
	 * @param topic Kafka topic to publish on
	 * @param message message to publish
	 */
	public void produceString(String topic, String message) {
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, message);
		producer.send(record);
	}
}
