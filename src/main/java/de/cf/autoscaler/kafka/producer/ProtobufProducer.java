package de.cf.autoscaler.kafka.producer;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.cf.autoscaler.kafka.messages.ApplicationMetric;
import de.cf.autoscaler.kafka.messages.ContainerMetric;
import de.cf.autoscaler.kafka.messages.HttpMetric;
import de.cf.autoscaler.kafka.messages.ScalingLog;
import de.cf.autoscaler.kafka.protobuf.ProtobufApplicationMetricWrapper.ProtoApplicationMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufContainerMetricWrapper.ProtoContainerMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufScalingWrapper.ProtoScaling;
import de.cf.autoscaler.scaling.ScalingAction;

/**
 * A Producer to publish {@linkplain ContainerMetric}, {@linkplain HttpMetric} and {@linkplain ScalingLog} as protobuf on Kafka.
 * @author Marius Berger
 *
 */
@Service
public class ProtobufProducer {

	/**
	 * Logger of this class.
	 */
	private Logger log = LoggerFactory.getLogger(ProtobufProducer.class);
	
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
	 * Topic to publish {@linkplain ScalingLog} on.
	 */
	private String scalingTopic;
	/**
	 * Topic to publish {@linkplain ApplicationMetric} on.
	 */
	private String applicationMetricsTopic;
	/**
	 * Id of the group to join.
	 */
	private String groupId;
	
	/**
	 * Underlying Kafka producer.
	 */
	private org.apache.kafka.clients.producer.Producer<String,byte[]> producer;
	
	/** 
	 * Default constructor for Spring to inject this service.
	 */
	public ProtobufProducer() { }
	
	/**
	 * Set up the fields of the producer after construction by Spring.
	 */
	@PostConstruct
	private void init() {
		host = kafkaProps.getHost()+":"+kafkaProps.getPort();
		scalingTopic = kafkaProps.getScalingTopic();
		applicationMetricsTopic = kafkaProps.getMetricApplicationTopic();
		groupId = kafkaProps.getProducerGroupId();
		
		
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        configProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        producer = new KafkaProducer<String, byte[]>(configProperties);
	}

	/**
	 * Publish a {@linkplain ScalingLog} on Kafka.
	 * @param sc {@linkplain ScalingAction} to get fields from
	 * @param timestamp time stamp for the {@linkplain ScalingLog}
	 */
	public void produceScalingLog(ScalingAction sc, long timestamp) {
		ScalableApp app = sc.getApp();
		
		ProtoScaling scalingProto = ProtoScaling.newBuilder()
				.setTimestamp(timestamp)
				.setAppId(app.getBinding().getId())
				.setComponent(sc.getReason())
				.setOldInstances(sc.getOldInstances())
				.setNewInstances(sc.getNewInstances())
				.setCurrentMaxInstanceLimit(app.getMaxInstances())
				.setCurrentMinInstanceLimit(app.getMinInstances())
				.setCurrentCpuLoad(app.getCpu().getValueOfCpu())
				.setCurrentCpuUpperLimit(app.getCpu().getUpperLimit())
				.setCurrentCpuLowerLimit(app.getCpu().getLowerLimit())
				.setCurrentRamLoad(app.getRam().getValueOfRam())
				.setCurrentRamUpperLimit(app.getRam().getUpperLimit())
				.setCurrentRamLowerLimit(app.getRam().getLowerLimit())
				.setCurrentRequestCount(app.getRequest().getValueOfHttpRequests())
				.setCurrentLatencyValue(app.getLatency().getValueOfLatency())
				.setCurrentLatencyUpperLimit(app.getLatency().getUpperLimit())
				.setCurrentLatencyLowerLimit(app.getLatency().getLowerLimit())
				.setCurrentQuotientValue(app.getRequest().getQuotient())
				.setDescription(sc.getReasonDescription())
				.build();
		
		ScalingLog scalingLog = new ScalingLog(scalingProto);
		log.debug("ScalingLog: " + scalingLog.toString());
		
		byte[] output = scalingProto.toByteArray();
		
        ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(scalingTopic, output);
        producer.send(rec);
	}

	/**
	 * Publish a {@linkplain ContainerMetric} on Kafka.
	 * @param containerMetric {@linkplain ContainerMetric} to publish
	 */
	public void produceContainerMetric(ContainerMetric containerMetric) {
		ProtoContainerMetric metricProto = ProtoContainerMetric.newBuilder()
				.setTimestamp(containerMetric.getTimestamp())
				.setMetricName(containerMetric.getMetricName())
				.setAppId(containerMetric.getAppId())
				.setCpu(containerMetric.getCpu())
				.setRam(containerMetric.getRam())
				.setInstanceIndex(containerMetric.getInstanceIndex())
				.setDescription(containerMetric.getDescription())
				.build();
		
		byte[] output = metricProto.toByteArray();
		
        ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(applicationMetricsTopic, output);
        producer.send(rec);
	}
	
	/**
	 * Publish a {@linkplain ApplicationMetric} on Kafka.
	 * @param applicationMetric protobuf application metric to produce
	 */
	public void produceApplicationMetric(ProtoApplicationMetric applicationMetric) {
		byte[] output = applicationMetric.toByteArray();
		
		ProducerRecord<String, byte[]> rec = new ProducerRecord<>(applicationMetricsTopic, output);
		producer.send(rec);
	}
}
