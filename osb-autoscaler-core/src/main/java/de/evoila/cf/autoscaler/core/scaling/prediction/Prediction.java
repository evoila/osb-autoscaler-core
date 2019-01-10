package de.evoila.cf.autoscaler.core.scaling.prediction;

import de.evoila.cf.autoscaler.core.exception.LimitException;
import de.evoila.cf.autoscaler.core.exception.SpecialCharacterException;
import de.evoila.cf.autoscaler.core.exception.TimeException;

/**
 * Entity for handling a prediction.
 * @author Marius Berger
 *
 */
public class Prediction {

	/**
	 * predicted instance count
	 */
	private int instanceCount;
	
	/**
	 * time stamp of the prediction
	 */
	private long timestamp;
	/**
	 * point of start of the interval
	 */
	private long intervalStart;
	/**
	 * point of end of the interval
	 */
	private long intervalEnd;
	
	/**
	 * ID of the predictor
	 */
	private String predictorId;
	/**
	 * ID of the application
	 */
	private String appId;
	/**
	 * description of the prediction
	 */
	private String desc;
	
	public Prediction(int instanceCount, long timestamp, long intervalStart, long intervalEnd, String predictorId,
			String appId, String desc) {
		this.instanceCount = instanceCount;
		this.timestamp = timestamp;
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
		this.predictorId = predictorId;
		this.appId = appId;
		this.desc = desc;
	}
	
	public int getInstanceCount() {
		return instanceCount;
	}
	
	public void setInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getIntervalStart() {
		return intervalStart;
	}
	
	public void setIntervalStart(long intervalStart) {
		this.intervalStart = intervalStart;
	}
	
	public long getIntervalEnd() {
		return intervalEnd;
	}
	
	public void setIntervalEnd(long intervalEnd) {
		this.intervalEnd = intervalEnd;
	}
	
	public String getPredictorId() {
		return predictorId;
	}
	
	public void setPredictorId(String predictorId) {
		this.predictorId = predictorId;
	}
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Checks if the prediction is valid and can be added to an application.
	 * @return true if the prediction is valid
	 * @throws SpecialCharacterException if the ID of the app or of the predictor is invalid
	 * @throws TimeException if the interval time information are invalid
	 * @throws LimitException if the limits are invalid
	 */
	public boolean isValid() throws SpecialCharacterException, TimeException, LimitException {
		if (!appId.matches("\\w*")) {
			for (int i = 0; i < appId.length(); i++) {
				if (String.valueOf(appId.charAt(i)).matches("\\W*") && appId.charAt(i)!= '-' ) {
					throw new SpecialCharacterException("AppId contains special characters.");
				}
			}
		}	
		
		if (!predictorId.matches("\\w*")) {
			for (int i = 0; i < predictorId.length(); i++) {
				if (String.valueOf(predictorId.charAt(i)).matches("\\W*") && predictorId.charAt(i)!= '-' ) {
					throw new SpecialCharacterException("PredictorId contains special characters.");
				}
			}
		}	
		
		if (instanceCount <= 0)
			throw new LimitException("InstanceCount is smaller than 1.");
		if (timestamp < 0)
			throw new TimeException("Timestamp is smaller than 0.");
		if (intervalStart < 0)
			throw new TimeException("IntervalStart is smaller than 0.");
		if (intervalEnd < 0)
			throw new TimeException("IntervalEnd is smaller than 0.");
		if (intervalStart >= intervalEnd)
			throw new TimeException("IntervalStart is greater than or equals intervalEnd.");
		
		return true;
	}
	
	public boolean equals(Prediction other) {
		return (instanceCount == other.getInstanceCount()
				&& timestamp == other.getTimestamp()
				&& intervalStart == other.getIntervalStart()
				&& intervalEnd == other.getIntervalEnd()
				&& appId.equals(other.getAppId())
				&& predictorId.equals(other.getPredictorId()));
	}
	
	public String toString() {
		return  "{"
				+ "\"timestamp\" : \"" + timestamp + "\""
				+ ", "
				+ "\"predictorId\" : \"" + predictorId + "\""
				+ ", "
				+ "\"appId\" : \"" + appId + "\""
				+ ", "
				+ "\"intervalStart\" : \"" + intervalStart + "\""
				+ ", "
				+ "\"intervalEnd\" : \"" + intervalEnd + "\""
				+ ", "
				+ "\"instanceCount\" : \"" + instanceCount + "\""
				+ ", "
				+ "\"description\" : \"" + desc + "\""
				+ "}";
	}
}
