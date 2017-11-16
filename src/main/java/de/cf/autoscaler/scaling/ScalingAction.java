package de.cf.autoscaler.scaling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;

import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.http.HTTPWrapper;
import de.cf.autoscaler.kafka.messages.ScalingLog;
import de.cf.autoscaler.kafka.producer.ProtobufProducer;
import de.cf.autoscaler.properties.ScalingEnginePropertiesBean;

/**
 * Defines and manages a scaling order.
 * @author Marius Berger
 *
 */
public class ScalingAction {
	
	/**
	 * Logger of this class.
	 */
	private static Logger log = LoggerFactory.getLogger(ScalingAction.class);

	
	/**
	 * Application the action is dedicated to.
	 */
	private ScalableApp app;
	/**
	 * Number of instances to scale to.
	 */
	private int newInstances;
	/**
	 * Old number of instances.
	 */
	private int oldInstances;
	/**
	 * Component the action is based on.
	 * Use the x_based constants of this class to give this field meaning. 
	 */
	private int reason;
	/**
	 * Boolean value whether there is need to scale
	 */
	private boolean needToScale;
	/**
	 * Description of the reseason, which will not be used for computation.
	 */
	private String reasonDescription;
	
	/**
	 * Executes the action and sends an order to the Scaling Engine.
	 * @param engineProperties property bean for settings of the Scaling Engine
	 * @param producer Producer to publish on the message broker
	 */
	public void executeAction(ScalingEnginePropertiesBean engineProperties, ProtobufProducer producer) {
		if (needToScale && isValid()) {
			long scalingTime = System.currentTimeMillis();
			app.setLastScalingTime(scalingTime);
			try {
				HttpResponse<String> response 
					= HTTPWrapper.scale(engineProperties.getHost()
							, engineProperties.getPort()
							, engineProperties.getScalingEndpoint()
							, app.getBinding().getResourceId()
							, app.getBinding().getContext()
							, newInstances
							, engineProperties.getSecret());

				if (newInstances > oldInstances)
					log.info("Upscaled app "+app.getIdentifierStringForLogs() + ": Statuscode "
							+response.getStatus()+ ", reason: " + getReasonDescription());
				if (newInstances < oldInstances)
					log.info("Downscaled app "+app.getIdentifierStringForLogs()+": Statuscode "
							+response.getStatus()+ ", reason: " + getReasonDescription());
				
				producer.produceScalingLog(this, scalingTime);
			} catch (com.mashape.unirest.http.exceptions.UnirestException e) {
				log.error("Connection error to the scaling engine: " + e.getMessage());
			}
		} else if (!isValid()) {
			log.info("A ScalingAction for " + app.getIdentifierStringForLogs() + " is not valid.");
		}
	}
	
	/**
	 * Checks if the action's fields are valid.
	 * @return true if it is valid
	 */
	public boolean isValid() {
		return (app != null && newInstances <= app.getMaxInstances() && newInstances >= app.getMinInstances()
				&& reason >= ScalingLog.UNDEFINED_BASED && reason <= ScalingLog.LIMIT_BASED && reasonDescription != null);
	}
	
	/**
	 * Check if this action is an upscale.
	 * @return true if this action scales up
	 */
	public boolean isUpscale() {
		return oldInstances < newInstances;
	}
	
	/**
	 * Check if this action is an downscale.
	 * @return true if this action scales down
	 */
	public boolean isDownscale() {
		return oldInstances > newInstances;
	}
	
	/**
	 * Constructor the set up the ScalingAction with all necessary fields.
	 * 
	 */
	/**
	 * Constructor the set up the ScalingAction with all necessary fields.
	 * @param app {@linkplain #app}
	 * @param newInstances {@linkplain #newInstances}
	 * @param oldInstances {@linkplain #oldInstances}
	 * @param needToScale {@linkplain #needToScale}
	 * @param reason {@linkplain #reason}
	 * @param reasonDescription {@linkplain #reasonDescription}
	 */
	public ScalingAction(ScalableApp app, int newInstances, int oldInstances, boolean needToScale, int reason,
			String reasonDescription) {
		this.app = app;
		this.newInstances = newInstances;
		this.oldInstances = oldInstances;
		this.needToScale = needToScale;
		this.reason = reason;
		this.reasonDescription = reasonDescription;
	}

	public ScalableApp getApp() {
		return app;
	}

	public void setAppId(ScalableApp app) {
		this.app = app;
	}

	public int getNewInstances() {
		return newInstances;
	}

	public void setNewInstances(int newInstances) {
		this.newInstances = newInstances;
	}

	public int getOldInstances() {
		return oldInstances;
	}

	public void setOldInstances(int oldInstances) {
		this.oldInstances = oldInstances;
	}

	public boolean isNeedToScale() {
		return needToScale;
	}

	public void setNeedToScale(boolean needToScale) {
		this.needToScale = needToScale;
	}

	public int getReason() {
		return reason;
	}

	public void setReason(int reason) {
		this.reason = reason;
	}

	public String getReasonDescription() {
		return reasonDescription;
	}

	public void setReasonDescription(String reasonDescription) {
		this.reasonDescription = reasonDescription;
	}

	public void setApp(ScalableApp app) {
		this.app = app;
	}

	@Override
	public String toString() {
		return "ScalingAction [app=" + app.getIdentifierStringForLogs() + ", newInstances=" + newInstances + ", oldInstances=" + oldInstances
				+ ", reason=" + reason + ", needToScale=" + needToScale + ", reasonDescription=" + reasonDescription
				+ "]";
	}
}
