package de.evoila.cf.autoscaler.core.kafka.consumer;


import com.google.protobuf.InvalidProtocolBufferException;
import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer;
import de.evoila.cf.autoscaler.kafka.messages.ContainerMetric;
import de.evoila.cf.autoscaler.kafka.protobuf.PbContainerMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Consumer implementing the {@code AutoScalerConsumer} interface, parsing byte to container protobuf from a {@code ByteConsumerThread} 
 * and adding the resulting {@code ContainerMetric} to the dedicated {@code ScalableApp}.
 * @see AutoScalerConsumer
 * @see ContainerMetric
 * @see PbContainerMetric
 * @author Marius Berger
 *
 */
public class ContainerMetricConsumer extends AbstractByteConsumer{
	
	/**
	 * Logger of the class.
	 */
	private Logger log = LoggerFactory.getLogger(ContainerMetricConsumer.class);
	
	/**
	 * Constructor with all necessary fields.
	 * @param topic topic for the {@linkplain #consThread ByteConsumerThread} to subscribe to.
	 * @param groupId id of the group for the {@linkplain #consThread ByteConsumerThread} to join.
	 * @param hostname IP or URL of the Kafka service
	 * @param port port of the Kafka service
	 * @param maxMetricAge {@linkplain #maxMetricAge}
	 * @param appManager {@linkplain #appManager ScalableAppManager}
	 */
	public ContainerMetricConsumer(String topic, String groupId, String hostname, int port, long maxMetricAge, ScalableAppManager appManager) {
		super(topic, groupId, hostname, port, maxMetricAge, appManager);
	}
	
	/**
	 * Consume byte, parse it into {@linkplain ContainerMetric} and add them to the dedicated {@linkplain ScalableApp}.
	 * @see ContainerMetric
	 * @see ScalableApp
	 * @see PbContainerMetric
	 */
	public void consume(byte[] bytes) {
		try {
			ContainerMetric metric = new ContainerMetric(PbContainerMetric.ProtoContainerMetric.parseFrom(bytes));
			ScalableApp app = appManager.getByResourceId(metric.getAppId());
			if (app != null && !metric.isTooOld(maxMetricAge)) {
				try {
					app.acquire();
					app.addMetric(metric);
				} catch (InterruptedException e) {}
				app.release();
			}
		} catch (InvalidProtocolBufferException e) {
			log.error("Could not parse metric: "+e.getMessage());
		}
	}
	
	/**
	 * Returns string representation of the {@linkplain ContainerMetric} type.
	 */
	public String getType() {
		return AutoScalerConsumer.TYPE_METRIC_CONTAINER;
	}
}