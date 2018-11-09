package de.evoila.cf.autoscaler.core.http.response;

import java.util.Date;

import de.evoila.cf.autoscaler.core.applications.ScalableApp;

/**
 * Simple wrapper class for serializing learning policies.
 * @author Marius Berger
 *
 */
public class ResponseLearning {

	private boolean learningEnabled;
	private int learningTimeMultiplier;
	private long learningStartTime;
	private String learningStartTimeAsString;
	
	public ResponseLearning(ScalableApp app) {
		learningEnabled = app.isLearningEnabled();
		learningTimeMultiplier = app.getLearningTimeMultiplier();
		learningStartTime = app.getLearningStartTime();
		learningStartTimeAsString = new Date(learningStartTime).toString();
	}

	public boolean isLearningEnabled() {
		return learningEnabled;
	}

	public void setLearningEnabled(boolean learningEnabled) {
		this.learningEnabled = learningEnabled;
	}

	public int getLearningTimeMultiplier() {
		return learningTimeMultiplier;
	}

	public void setLearningTimeMultiplier(int learningTimeMultiplier) {
		this.learningTimeMultiplier = learningTimeMultiplier;
	}

	public long getLearningStartTime() {
		return learningStartTime;
	}

	public void setLearningStartTime(long learningStartTime) {
		this.learningStartTime = learningStartTime;
	}

	public String getLearningStartTimeAsString() {
		return learningStartTimeAsString;
	}

	public void setLearningStartTimeAsString(String learningStartTimeAsString) {
		this.learningStartTimeAsString = learningStartTimeAsString;
	}
}
