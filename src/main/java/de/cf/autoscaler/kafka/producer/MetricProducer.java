package de.cf.autoscaler.kafka.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import de.cf.autoscaler.kafka.messages.ContainerMetric;
import de.cf.autoscaler.kafka.messages.HttpMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufContainerMetricWrapper.ProtoContainerMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufHttpMetricWrapper.ProtoHttpMetric;

/**
 * A Producer to publish metrics on Kafka.
 * @author Marius Berger
 *
 */
public class MetricProducer {

	/**
	 * IP or URL of Kafka plus its port.
	 */
	private String host;
	/**
	 * Producer for outgoing messages.
	 */
	private org.apache.kafka.clients.producer.Producer<String,byte[]> producer;
	
	@SuppressWarnings("unused")
	/**
	 * Id of the group to join on Kafka.
	 */
	private String groupId;
	
	/**
	 * Constructor with all necessary fields.
	 * @param groupId {@linkplain #groupId}
	 * @param hostname IP or URL of Kafka
	 * @param port port of Kafka
	 */
	public MetricProducer(String groupId, String hostname, int port) {
        this.groupId = groupId;
        host = hostname+":"+port;
        
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        configProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");

        producer = new KafkaProducer<String, byte[]>(configProperties);
	}
	
	/**
	 * Publish a {@linkplain HttpMetric} on Kafka on the given topic.
	 * @param metric {@linkplain HttpMetric} to publish
	 * @param topic topic to publish on
	 */
	public void produce(HttpMetric metric, String topic) {
		ProtoHttpMetric metricProto = ProtoHttpMetric.newBuilder()
				.setTimestamp(metric.getTimestamp())
				.setMetricName(metric.getMetricName())
				.setAppId(metric.getAppId())
				.setRequests(metric.getRequests())
				.setLatency(metric.getLatency())
				.setDescription(metric.getDescription())
				.build();
		
		byte[] output = metricProto.toByteArray();
		
        ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(topic, output);
        producer.send(rec);
	}
	
	/**
	 * Publish a {@linkplain ContainerMetric} on Kafka on the given topic.
	 * @param metric {@linkplain ContainerMetric} to publish
	 * @param topic topic to publish on
	 */
	public void produce(ContainerMetric metric, String topic) {
		ProtoContainerMetric metricProto = ProtoContainerMetric.newBuilder()
				.setTimestamp(metric.getTimestamp())
				.setMetricName(metric.getMetricName())
				.setAppId(metric.getAppId())
				.setCpu(metric.getCpu())
				.setRam(metric.getRam())
				.setInstanceIndex(metric.getInstanceIndex())
				.setDescription(metric.getDescription())
				.build();
		
		byte[] output = metricProto.toByteArray();
		
        ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(topic, output);
        producer.send(rec);
	}
}
