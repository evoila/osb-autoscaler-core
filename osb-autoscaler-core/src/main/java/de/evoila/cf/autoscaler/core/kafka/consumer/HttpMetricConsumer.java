package de.evoila.cf.autoscaler.core.kafka.consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import de.evoila.cf.autoscaler.core.model.ScalableApp;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer;
import de.evoila.cf.autoscaler.kafka.messages.HttpMetric;
import de.evoila.cf.autoscaler.kafka.protobuf.PbHttpMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Consumer implementing the {@code AutoScalerConsumer} interface, parsing byte to http protobuf from a {@code ByteConsumerThread}
 * and adding the resulting {@code HttpMetric} to the dedicated {@code ScalableApp}.
 * @see AutoScalerConsumer
 * @author Marius Berger
 *
 */
public class HttpMetricConsumer extends AbstractByteConsumer{
	
	/**
	 * Logger of the class.
	 */
	private Logger log = LoggerFactory.getLogger(HttpMetricConsumer.class);
	
	/**
	 * Constructor with all necessary fields.
	 * @param topic topic for the {@linkplain #consThread ByteConsumerThread} to subscribe to.
	 * @param groupId id of the group for the {@linkplain #consThread ByteConsumerThread} to join.
	 * @param hostname IP or URL of the Kafka service
	 * @param port port of the Kafka service
	 * @param maxMetricAge {@linkplain #maxMetricAge}
	 * @param appManager {@linkplain #appManager ScalableAppManager}
	 */
	public HttpMetricConsumer(String topic, String groupId, String hostname, int port, long maxMetricAge, ScalableAppManager appManager) {
		super(topic, groupId, hostname, port, maxMetricAge, appManager);
	}
	
	/**
	 * Consume byte, parse it into {@linkplain PbHttpMetric} and add them to the dedicated {@linkplain ScalableApp}.
	 * @see ScalableApp
	 */
	public void consume(byte[] bytes) {
		try {
			HttpMetric metric = new HttpMetric(PbHttpMetric.ProtoHttpMetric.parseFrom(bytes));
			ScalableApp app = appManager.getByResourceId(metric.getAppId());
			
			if (app != null && !metric.isTooOld(maxMetricAge)) {
				try {
					app.acquire();
					app.addMetric(metric);
				} catch (InterruptedException ex) {}
				app.release();
			}
		} catch (InvalidProtocolBufferException e) {
			log.error("Could not parse metric: "+e.getMessage());
		}
	}
	
	/**
	 * Returns string representation of the {@linkplain PbHttpMetric} type.
	 */
	public String getType() {
		return AutoScalerConsumer.TYPE_METRIC_HTTP;
	}
}
