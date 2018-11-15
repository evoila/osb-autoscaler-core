package de.evoila.cf.autoscaler.core.kafka.producer;

import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.scaling.ScalingAction;
import de.evoila.cf.autoscaler.kafka.KafkaPropertiesBean;
import de.evoila.cf.autoscaler.kafka.messages.ContainerMetric;
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog;
import de.evoila.cf.autoscaler.kafka.protobuf.PbApplicationMetric;
import de.evoila.cf.autoscaler.kafka.protobuf.PbContainerMetric;
import de.evoila.cf.autoscaler.kafka.protobuf.PbScalingLog;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * A Producer to publish {@linkplain ContainerMetric}, {@linkplain ScalingLog} as protobuf on Kafka.
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
		host = kafkaProps.getHost() + ":" + kafkaProps.getPort();
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

        producer = new KafkaProducer<>(configProperties);
	}

	/**
	 * Publish a {@linkplain ScalingLog} on Kafka.
	 * @param sc {@linkplain ScalingAction} to get fields from
	 * @param timestamp time stamp for the {@linkplain ScalingLog}
	 */
	public void produceScalingLog(ScalingAction sc, long timestamp) {
		ScalableApp app = sc.getApp();
		
		PbScalingLog.ProtoScalingLog scalingLogProto = PbScalingLog.ProtoScalingLog.newBuilder()
				.setTimestamp(timestamp)
				.setAppId(app.getBinding().getResourceId())
				.setResourceName(app.getBinding().getResourceName())
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
		
		ScalingLog scalingLog = new ScalingLog(scalingLogProto);
		log.debug("ScalingLog: " + scalingLog.toString());
		
		byte[] output = scalingLogProto.toByteArray();
		
        ProducerRecord<String, byte[]> rec = new ProducerRecord<String, byte[]>(scalingTopic, output);
        producer.send(rec);
	}

	/**
	 * Publish a {@linkplain ContainerMetric} on Kafka.
	 * @param containerMetric {@linkplain ContainerMetric} to publish
	 */
	public void produceContainerMetric(ContainerMetric containerMetric) {
		PbContainerMetric.ProtoContainerMetric metricProto = PbContainerMetric.ProtoContainerMetric.newBuilder()
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
	 * Publish a {@linkplain PbApplicationMetric} on Kafka.
	 * @param applicationMetric protobuf application metric to produce
	 */
	public void produceApplicationMetric(PbApplicationMetric.ProtoApplicationMetric applicationMetric) {
		byte[] output = applicationMetric.toByteArray();
		
		ProducerRecord<String, byte[]> rec = new ProducerRecord<>(applicationMetricsTopic, output);
		producer.send(rec);
	}
}
