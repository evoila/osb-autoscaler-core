package de.cf.autoscaler.scaling;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.cf.autoscaler.applications.ScalableApp;
import de.cf.autoscaler.applications.ScalableAppService;
import de.cf.autoscaler.http.HTTPWrapper;
import de.cf.autoscaler.kafka.producer.ProtobufProducer;
import de.cf.autoscaler.manager.ScalableAppManager;

/**
 * Core class with the check scaling loop.
 * @author Marius Berger
 *
 */
@Service
public class Scaler {
	
	/**
	 * Specific number to signal that the instance count could not be computed.
	 */
	public static final int NO_METRIC_ERROR_INSTANCE_COUNT = -666;
	
	/**
	 * Logger of this class.
	 */
	private Logger log = LoggerFactory.getLogger(Scaler.class);

	/**
	 * Manager for {@linkplain ScalableAppManager} to get applications from.
	 */
	@Autowired
	private ScalableAppManager appManager;
	
	/**
	 * Producer service to publish on the message broker.
	 */
	@Autowired
	private ProtobufProducer producer; 
	
	/**
	 * Wrapper for HTTP request.
	 */
	@Autowired
	private HTTPWrapper httpWrapper;
	
	/**
	 * Mutex to get the scaling checks triggered based on time by a {@linkplain TimerThread}
	 */
	private Semaphore checkScalingMutex;
	
	/**
	 * Triggers the scaling checks of the scaler.
	 */
	private TimerThread timer;
	
	/**
	 * A Thread dedicated to run the {@linkplain #checkScalingLoop()}.
	 */
	private ScalingThread scalingThread;
	
	/**
	 * Constructor for Spring to set up and inject the scaler.
	 */
	public Scaler() {
		checkScalingMutex = new Semaphore(0, true);
	}
	
	/**
	 * Gets called after construction of the service by spring to create and start the TimerThread.
	 */
	@PostConstruct
	public void init() {
		timer = new TimerThread(this);
		timer.start();
		scalingThread = new ScalingThread(this);
		scalingThread.start();
	}
	
	/**
	 *  Loops the check scaling mechanism of the scaler.
	 *  Waits until the checkScalingMutex is available to check all ScalableApps.
	 *  !This is an endless loop!
	 */
	public void checkScalingLoop() {
		try {
			ScalableApp currentApp = null;
			while(true){
				checkScalingMutex.acquire();
				List<ScalableApp> l = appManager.getFlatCopyOfApps();
				for (int i = 0; i < l.size(); i++) {
					currentApp = l.get(i);
					currentApp.acquire();
					log.debug("--- Application " + currentApp.getIdentifierStringForLogs() + " --- ");
					if (currentApp.isScalingEnabled()) {
						ScalableAppService.aggregateInstanceMetrics(currentApp, producer);
						boolean timeToCheck = currentApp.timeToCheck();
						if (currentApp.isInCooldown()) {
							log.info("Application "+ currentApp.getIdentifierStringForLogs() + " is still waiting for cooldown.");
							currentApp.resetApplicationMetricLists();
						} else if (timeToCheck) {
							log.info("Time to check for "+ currentApp.getIdentifierStringForLogs());
							checkScaling(currentApp);
						} else {
							log.debug("Not yet time for " + currentApp.getIdentifierStringForLogs());
						}
					} else {
						log.debug("InstanceMetrics: count=" + currentApp.getCopyOfInstanceContainerMetricsList().size() + " - " + currentApp.getCopyOfInstanceContainerMetricsList());
						log.debug("No scaling enabled for " + currentApp.getIdentifierStringForLogs());
						currentApp.resetContainerMetricsList();
						currentApp.resetHttpMetricList();
					}

					currentApp.release();
				}
			}
		} catch (InterruptedException e) { }
		log.error("Scaling loop stopped because of an InterruptedException.");
	}

	/**
	 * Checks whether it is necessary to scale for a ScalableApp.
	 * @param app ScalalbeApp to check scaling for
	 */
	public void checkScaling(ScalableApp app) {
		if (app == null) 
			return;
		
		int instances = app.getCurrentInstanceCount();
		if (instances == NO_METRIC_ERROR_INSTANCE_COUNT) {
			log.error("Application " + app.getIdentifierStringForLogs() + " had no metrics to compute the instance count.");
			return;
		}
		
		List<ScalingAction> actions = new LinkedList<>();
		
		actions.add(ScalingChecker.chooseScalingActionForCpu(app));
		actions.add(ScalingChecker.chooseScalingActionForRam(app));
		actions.add(ScalingChecker.chooseScalingActionForLatency(app));
		
		log.debug("Hardware actions: " + actions);
		ScalingAction action = decideAction(actions);
		log.debug("Decided action: " + action);
		
		ScalingAction tmpAction = ScalingChecker.chooseScalingActionForLimits(app, action);
		if (tmpAction != null) {
			log.debug("Limit action created: " + tmpAction);
			action = tmpAction;
		}
		
		ScalingAction predictedAction = ScalingChecker.chooseScalingActionForPrediction(app);
		if ( predictedAction != null) {
			log.debug("Predicted action: " + predictedAction);
			action = predictedAction;
		}
		
		log.debug("Final action: " + action);
		
		if (action != null) {
			if (action.isNeedToScale() && action.getNewInstances() != action.getOldInstances()) {
				log.info("Scaling needed for " + app.getIdentifierStringForLogs() + ": " + action.getReasonDescription());
				action.executeAction(httpWrapper, producer);
			} else if (app.isLearningEnabled()){
				// no scaling ? => set Requests Per Instance
				log.info("No scaling needed for " + app.getIdentifierStringForLogs() + ".");
				app.getRequest().setRequestsPerInstance();
				
			}
		} else {
			log.info("No final scaling action found for " + app.getIdentifierStringForLogs() + ", this may be caused by not enabling scaling for any metric.");
		}
		app.resetApplicationMetricLists();
		appManager.updateInDatabase(app);
	}
	
	/**
	 * Method for the {@linkplain #timer TimerThread} to access the {@linkplain Scaler#checkScalingMutex Mutex}.
	 */
	public void releaseMutex() {
		checkScalingMutex.release();
	}
	
	/**
	 * Looks for the {@linkplain ScalingAction} with the highest priority.
	 * @param actions List of actions to work with
	 * @return {@linkplain ScalingAction} with the highest priority
	 */
	private ScalingAction decideAction(List<ScalingAction> actions) {
		if (actions.size() > 0) {
			ScalingAction action = null;
			ScalingAction hlp;
			for (int i = 0; i < actions.size(); i++) {
				hlp = actions.get(i);
				action = higherPriority(action, hlp);
			}
			return action;
		}
		return null;
	}
	
	/**
	 * Compares two {@linkplain ScalingAction} objects and return the one with the higher priority.
	 * Generally returns the ScalingAction with the higher instance count.
	 * Existing ScalingActions have a higher priority than null pointers.
	 * @param older first ScalingAction for comparison
	 * @param newer second ScalingAction for comparison
	 * @return higher priority ScalingAction
	 */
	private ScalingAction higherPriority(ScalingAction older, ScalingAction newer) {
		if ( newer == null || !newer.isValid() )
			return older;
		
		if (older == null)
			return newer;
		
		if (older.isUpscale()) {
			
			if (newer.isUpscale()) {
				return higherInstanceCount(older, newer);
			} else {
				return older;
			}
		
		} else if (!older.isNeedToScale()) {
			
			if (newer.isUpscale()) {
				return newer;
			} else {
				return older;
			}
			
		} else if(older.isDownscale()) {
			
			if (newer.isUpscale()) {
				return newer;
			}  else if (!newer.isNeedToScale()){
				return newer;
			} else if(newer.isDownscale()) {
				return higherInstanceCount(older, newer);
			}
			
		}
		
		return null;
	}
	
	/**
	 * Returns the ScalingAction with the higher instance count out of two ScalingActions.
	 * @param older first ScalingAction for comparison
	 * @param newer second ScalingAction for comparison
	 * @return ScalingAction with the higher instance count. If both have the same instance count, the first ScalingAction will be returned
	 */
	private ScalingAction higherInstanceCount(ScalingAction older, ScalingAction newer) {
		if (newer.getNewInstances() > older.getNewInstances())
			return newer;
		return older;
	}
}
