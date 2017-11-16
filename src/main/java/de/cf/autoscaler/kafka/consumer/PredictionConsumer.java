package de.cf.autoscaler.kafka.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.exception.LimitException;
import de.cf.autoscaler.exception.SpecialCharacterException;
import de.cf.autoscaler.exception.TimeException;
import de.cf.autoscaler.kafka.AutoScalerConsumer;
import de.cf.autoscaler.kafka.ByteConsumerThread;
import de.cf.autoscaler.kafka.messages.ContainerMetric;
import de.cf.autoscaler.kafka.protobuf.ProtobufPredictionWrapper;
import de.cf.autoscaler.kafka.protobuf.ProtobufPredictionWrapper.ProtoPrediction;
import de.cf.autoscaler.manager.ScalableAppManager;
import de.cf.autoscaler.scaling.prediction.Prediction;

/**
 * A Consumer implementing the {@code AutoScalerConsumer} interface, parsing byte to prediction protobuf from a {@code ByteConsumerThread} 
 * and adding the resulting {@code Prediction} to the dedicated {@code ScalableApp}.
 * @see AutoScalerConsumer
 * @see ByteConsumerThread
 * @see ContainerMetric 
 * @see ProtobufPredictionWrapper
 * @see Prediction
 * @author Marius Berger
 *
 */
public class PredictionConsumer extends AbstractByteConsumer{
	
	/**
	 * Logger of the class.
	 */
	private Logger log = LoggerFactory.getLogger(PredictionConsumer.class);
	
	/**
	 * Constructor with all necessary fields.
	 * @param topic topic for the {@linkplain #consThread ByteConsumerThread} to subscribe to.
	 * @param groupId id of the group for the {@linkplain #consThread ByteConsumerThread} to join.
	 * @param hostname IP or URL of the Kafka service
	 * @param port port of the Kafka service
	 * @param appManager {@linkplain #appManager ScalableAppManager}
	 */
	public PredictionConsumer(String topic, String groupId, String hostname, int port, ScalableAppManager appManager) {
		super(topic, groupId, hostname,port, -1, appManager);
	}
	
	/**
	 * Consume byte, parse it into {@linkplain Prediction} and add them to the dedicated {@linkplain ScalableApp}.
	 * @see Prediction
	 * @see ScalableApp
	 * @see ProtobufPredictionWrapper
	 * @see ProtoPrediction
	 */
	@Override
	public void consume(byte[] bytes) {
		try {
			Prediction prediction = new Prediction(ProtoPrediction.parseFrom(bytes));
			ScalableApp app = appManager.getByResourceId(prediction.getAppId());

			if (app != null) {
				try {
					app.acquire();
						
					try {
						prediction.isValid();
						app.setPrediction(prediction);
					} catch (TimeException ex) {
						log.debug("TimeException for a prediction for " + app.getIdentifierStringForLogs() + ": " + ex.getMessage());
					} catch (SpecialCharacterException ex) {
						log.debug("SpecialCharacterException for a prediction for " + app.getIdentifierStringForLogs() + ": " + ex.getMessage());
					} catch (LimitException ex) {
						log.debug("LimitException for a prediction for " + app.getIdentifierStringForLogs() + ": " + ex.getMessage());
					}
					
					log.info("Prediction from " + prediction.getPredictorId() + " was invalid and not set for " + app.getIdentifierStringForLogs());
						
					app.setPrediction(prediction);
				} catch (InterruptedException e) {}
				app.release();
			}
		} catch (InvalidProtocolBufferException e) {
			log.error("Could not parse prediction: " + e.getMessage());
		}
	}
	
	@Override
	public String getType() {
		return AutoScalerConsumer.TYPE_PREDICTION;
	}
}