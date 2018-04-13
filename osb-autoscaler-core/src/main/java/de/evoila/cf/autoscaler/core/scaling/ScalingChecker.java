package de.evoila.cf.autoscaler.core.scaling;

import de.evoila.cf.autoscaler.core.applications.ScalableApp;
import de.evoila.cf.autoscaler.core.properties.AutoscalerPropertiesBean;
import de.evoila.cf.autoscaler.core.scaling.prediction.Prediction;
import de.evoila.cf.autoscaler.kafka.messages.ScalingLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds methods for computing needed instance counts of an application.
 * @author Marius Berger
 *
 */
public class ScalingChecker {

	/**
	 * Logger of this class.
	 */
	private static Logger log = LoggerFactory.getLogger(ScalingChecker.class);
	
	/**
	 * Number of instances to add or subtract from the instance count when scaling without the quotient.
	 * This is altered by the {@linkplain Scaler#init()} method with the value from the {@linkplain AutoscalerPropertiesBean}.
	 */
	private static int staticScalingSize = 1;


	/**
	 * Private constructor as there is no need for an object of this class.
	 */
	private ScalingChecker() {}
	
	public static int getStaticScalingSize() {
		return staticScalingSize;
	}

	public static void setStaticScalingSize(int staticScalingSize) {
		ScalingChecker.staticScalingSize = staticScalingSize;
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on CPU values.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @return computed ScalingAction or null if scaling for this component is not enabled or null if the instance count could not be computed
	 */
	public static ScalingAction chooseScalingActionForCpu(ScalableApp app) {
		if (!app.getCpu().isCpuScalingEnabled()) 
			return null;
		
		int instances = app.getCurrentInstanceCount();
		if (instances == Scaler.NO_METRIC_ERROR_INSTANCE_COUNT) {
			log.error("Application " + app.getIdentifierStringForLogs() + " had no metrics to compute the instance count.");
			return null;
		}
		
		ScalingAction act = null;
		
		if (app.getRequest().isQuotientScalingEnabled() && app.getRequest().getQuotient() > 0 && !app.isInLearningTime()) {
			act = chooseScalingActionWithQuotient(app, instances, ScalingLog.CONTAINER_CPU_BASED);
		} else {		
			act = chooseScalingActionWithoutQuotient(app, instances, ScalingLog.CONTAINER_CPU_BASED);
		}
		
		act = checkForLimits(app, act);
		return act;
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on RAM values.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @return computed ScalingAction or null if scaling for this component is not enabled or null if the instance count could not be computed
	 */
	public static ScalingAction chooseScalingActionForRam(ScalableApp app) {
		if (!app.getRam().isRamScalingEnabled()) 
			return null;	
		
		int instances = app.getCurrentInstanceCount();
		if (instances == Scaler.NO_METRIC_ERROR_INSTANCE_COUNT) {
			log.error("Application had no metrics to compute the instance count.");
			return null;
		}
		
		ScalingAction act = null;
		
		if (app.getRequest().isQuotientScalingEnabled() && app.getRequest().getQuotient() > 0 && !app.isInLearningTime()) {
			act = chooseScalingActionWithQuotient(app, instances, ScalingLog.CONTAINER_RAM_BASED);
		} else {		
			act = chooseScalingActionWithoutQuotient(app, instances, ScalingLog.CONTAINER_RAM_BASED);
		}
		
		act = checkForLimits(app, act);
		return act;
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on latency values.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @return computed ScalingAction or null if scaling for this component is not enabled or null if the instance count could not be computed
	 */
	public static ScalingAction chooseScalingActionForLatency(ScalableApp app) {
		if (!app.getLatency().isLatencyScalingEnabled()) 
			return null;	
		
		int instances = app.getCurrentInstanceCount();
		if (instances == Scaler.NO_METRIC_ERROR_INSTANCE_COUNT) {
			log.error("Application had no metrics to compute the instance count.");
			return null;
		}
		
		ScalingAction act = null;
		
		if (app.getRequest().isQuotientScalingEnabled() && app.getRequest().getQuotient() > 0 && !app.isInLearningTime()) {
			act = chooseScalingActionWithQuotient(app, instances, ScalingLog.HTTP_LATENCY_BASED);
		} else {		
			act = chooseScalingActionWithoutQuotient(app, instances, ScalingLog.HTTP_LATENCY_BASED);
		}
		
		act = checkForLimits(app, act);
		return act;
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on application limits.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @param act {@linkplain ScalingAction} to control for limit violations
	 * @return computed ScalingAction or null if the instance count could not be computed
	 */
	public static ScalingAction chooseScalingActionForLimits(ScalableApp app, ScalingAction act) {
			int newInstances = -1;
			int oldInstances = app.getCurrentInstanceCount();
			boolean changeNeeded = false;
			String desc = "";
			
			if (oldInstances == Scaler.NO_METRIC_ERROR_INSTANCE_COUNT)
				return null;
			
			//if there is need to scale, the other scaling action already stays in the limit
			if (oldInstances > app.getMaxInstances() && (act == null || !act.isNeedToScale() || act.getNewInstances() == app.getMaxInstances()) ) {
				newInstances = app.getMaxInstances();
				changeNeeded = true;
				desc = "Downscaled - instance count over maximum of "+app.getMaxInstances(); 
			}
			else if (oldInstances < app.getMinInstances() && (act == null || !act.isNeedToScale() || act.getNewInstances() == app.getMinInstances()) ) {
				newInstances = app.getMinInstances();
				changeNeeded = true;
				desc = "Upscaled - instance count below minimum of "+app.getMinInstances(); 
			}
			
			
			if (changeNeeded) {
				ScalingAction newAction = new ScalingAction(app, newInstances, oldInstances, true, ScalingLog.LIMIT_BASED, desc);
				return newAction;
			}
			//return new ScalingAction(app,oldInstances, oldInstances, false, "No application limits were surpassed.");
			return null;
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on the {@linkplain Prediction}.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @return computed ScalingAction or null if scaling for this component is not enabled or null if the instance count could not be computed
	 */
	public static ScalingAction chooseScalingActionForPrediction(ScalableApp app) {
		Prediction pred = app.getPrediction(); 
		long current = System.currentTimeMillis();
		
		if (pred == null || pred.getIntervalStart() > current)
			return null;
		
		if (pred.getIntervalEnd() < current) {
			app.setPrediction(null);
			return null;
		}
		
		int oldInstances = app.getCurrentInstanceCount();
		ScalingAction act = null;
		
		if (oldInstances == pred.getInstanceCount()) {
			act = new ScalingAction(app, oldInstances, oldInstances, false, ScalingLog.PREDICTOR_BASED, "No scaling - current instance count is the same as the predicted one.");
		} else {
			String desc;
			if (oldInstances > pred.getInstanceCount()) {
				desc = "Downscaled - instance count above prediction.";
			} else { 
				desc = "Upscaled - instance count below prediction.";
			}
			act = new ScalingAction(app, pred.getInstanceCount(), oldInstances, true, ScalingLog.PREDICTOR_BASED, desc);
		}
			
		return checkForLimits(app, act);
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on the given component with the help of the quotient.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @param instances current number of instances of the application
	 * @param component index of which component will be used
	 * @return the computed ScalingAction
	 */
	private static ScalingAction chooseScalingActionWithQuotient(ScalableApp app, int instances, int component) {
		long upperLimit = -1;
		long lowerLimit = -1;
		long currentValue = -1;
		String descriptionFiller = "";
		
		switch(component) {
		case ScalingLog.CONTAINER_CPU_BASED: upperLimit = app.getCpu().getUpperLimit();
												lowerLimit = app.getCpu().getLowerLimit();
												currentValue = app.getCpu().getValueOfCpu();
												descriptionFiller = "cpu load";
												break;
		case ScalingLog.CONTAINER_RAM_BASED: upperLimit = app.getRam().getUpperLimit();
												lowerLimit = app.getRam().getLowerLimit();
												currentValue = app.getRam().getValueOfRam();
												descriptionFiller = "ram load";
												break;
		case ScalingLog.HTTP_LATENCY_BASED:	upperLimit = app.getLatency().getUpperLimit();
												lowerLimit = app.getLatency().getLowerLimit();
												currentValue = app.getLatency().getValueOfLatency();
												descriptionFiller = "latency";
												break;
		default:								log.error("Tried to create a ScalingAction based on an unknown component.");
												return null;
		}
		
		ScalingAction act = chooseScalingActionBasedOnQuotient(app, instances, component);
		
		if (currentValue > upperLimit) {
			act.setReasonDescription("Upscaled - " + descriptionFiller + " over " + upperLimit + " - with quotient");
			
			if (act.getNewInstances() <= act.getOldInstances()) {
				act = chooseScalingActionWithoutQuotient(app, instances, component);
				act.setReasonDescription("Upscaled - " + descriptionFiller + " over " + upperLimit + " - despite quotient");
			}
		} else if (currentValue < lowerLimit) {
			act.setReasonDescription("Downscaled - " + descriptionFiller + " below " + lowerLimit + " - with quotient");
			
			if (act.getNewInstances() >= act.getOldInstances()) {
				act = chooseScalingActionWithoutQuotient(app, instances, component);
				act.setReasonDescription("Downscaled - " + descriptionFiller + " below "+ lowerLimit+ " - despite quotient");
			}
		} else if (currentValue <= upperLimit && currentValue >= lowerLimit) {
			String desc = "No need for scaling, because " + descriptionFiller + " is in an allowed state.";
			act = new ScalingAction(app, instances, instances, false, component, desc);
		}
		return act;
	}
	
	/**
	 * Get a {@linkplain ScalingAction} based on the given component without the help of the quotient.
	 * Use the constants of the ScalingAction class for the component index.
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @param instances current number of instances of the application
	 * @param component index of which component will be used
	 * @return the computed ScalingAction
	 */
	private static ScalingAction chooseScalingActionWithoutQuotient(ScalableApp app, int instances, int component) {
		int newInstances = -1;
		long upperLimit = -1;
		long lowerLimit = -1;
		long currentValue = -1;
		String desc;
		String descriptionFiller = "";
		
		switch(component) {
		case ScalingLog.CONTAINER_CPU_BASED: upperLimit = app.getCpu().getUpperLimit();
												lowerLimit = app.getCpu().getLowerLimit();
												currentValue = app.getCpu().getValueOfCpu();
												descriptionFiller = "cpu load";
												break;
		case ScalingLog.CONTAINER_RAM_BASED: upperLimit = app.getRam().getUpperLimit();
												lowerLimit = app.getRam().getLowerLimit();
												currentValue = app.getRam().getValueOfRam();
												descriptionFiller = "ram load";
												break;
		case ScalingLog.HTTP_LATENCY_BASED:	upperLimit = app.getLatency().getUpperLimit();
												lowerLimit = app.getLatency().getLowerLimit();
												currentValue = app.getLatency().getValueOfLatency();
												descriptionFiller = "latency";
												break;
		default:								log.error("Tried to create a ScalingAction based on an unknown component.");
												return null;
		}
		
		ScalingAction act = null;
		
		if (currentValue > upperLimit) {
			newInstances = instances + staticScalingSize;
			desc = "Upscaled - " + descriptionFiller + " over " + upperLimit + " - without quotient ";
			act = new ScalingAction(app, newInstances, instances, true, component, desc);	
		} else if (currentValue < lowerLimit) {
			newInstances = instances - staticScalingSize;
			desc = "Downscaled - " + descriptionFiller + " below "+ lowerLimit + " - without quotient ";
			act = new ScalingAction(app, newInstances, instances, true, component, desc);
		} else if (currentValue <= upperLimit && currentValue >= lowerLimit) {
			desc = "No need for scaling, because " + descriptionFiller + " is in an allowed state.";
			act = new ScalingAction(app, instances, instances, false, component, desc);
		}
		
		return act;
	}
	
	/**
	 * Creates a {@linkplain ScalingAction} with an instance count computed by the quotient. 
	 * @param app {@linkplain ScalableApp} to get the ScalingAction for
	 * @param instances current number of instances of the application
	 * @param basedOnComponent index of which component will be used
	 * @return created ScalingAction
	 */
	private static ScalingAction chooseScalingActionBasedOnQuotient(ScalableApp app, int instances, int basedOnComponent) {
			int quotient = app.getRequest().getQuotient();
			int requests = app.getRequest().getValueOfHttpRequests();
			int newInstances = Math.abs(requests / quotient);
				
			if (requests > newInstances * quotient) 
				newInstances++;

			if (newInstances < app.getMinInstances())
				newInstances = app.getMinInstances();
			
			if (newInstances > app.getMaxInstances())
				newInstances = app.getMaxInstances();
			
			if (newInstances == instances)
				return new ScalingAction(app, instances, instances, false, basedOnComponent, "");
			return new ScalingAction(app, newInstances, instances, true, basedOnComponent, "");
	}

	/**
	 * Checks whether a given {@linkplain ScalingAction} is out of the application's limits and alters the {@code ScalingAction} accordingly.
	 * @param app {@linkplain ScalableApp} to get limits from
	 * @param act {@linkplain ScalingAction} to check for limit violations
	 * @return the altered {@linkplain ScalingAction}
	 */
	private static ScalingAction checkForLimits(ScalableApp app, ScalingAction act) {
		if (act != null) {
			if (act.getNewInstances() > app.getMaxInstances()) {
				act.setNewInstances(app.getMaxInstances());
				act.setNeedToScale(true);
			}
				
			if (act.getNewInstances() < app.getMinInstances()) {
				act.setNewInstances(app.getMinInstances());
				act.setNeedToScale(true);
			}
				
			
			if (act.getNewInstances()  == act.getOldInstances() && act.isNeedToScale()) {
				String desc = "Not allowed to scale, because it would break an instance count limit.";
				act = new ScalingAction(app, act.getOldInstances(), act.getOldInstances(), false, act.getReason(), desc);
			}
		}
		
		return act;
	}
}
