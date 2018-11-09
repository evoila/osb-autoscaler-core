package de.evoila.cf.autoscaler.core.kafka.consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.exception.LimitException;
import de.evoila.cf.autoscaler.core.exception.SpecialCharacterException;
import de.evoila.cf.autoscaler.core.exception.TimeException;
import de.evoila.cf.autoscaler.core.manager.ScalableAppManager;
import de.evoila.cf.autoscaler.core.scaling.prediction.Prediction;
import de.evoila.cf.autoscaler.kafka.AutoScalerConsumer;
import de.evoila.cf.autoscaler.kafka.protobuf.PbPrediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Consumer implementing the {@code AutoScalerConsumer} interface, parsing byte to prediction protobuf from a {@code ByteConsumerThread} 
 * and adding the resulting {@code Prediction} to the dedicated {@code ScalableApp}.
 * @see AutoScalerConsumer
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
	 */
	@Override
	public void consume(byte[] bytes) {
		try {
			Prediction prediction = new Prediction(PbPrediction.ProtoPrediction.parseFrom(bytes));
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