package de.cf.autoscaler.scaling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timer for releasing the {@linkplain Scaler Scaler's} mutex in a regular interval.
 * Triggers the checkScaling() method of the dedicated Scaler.
 * @author Marius Berger
 *
 */
public class TimerThread extends Thread {

	/**
	 * Time to wait before releasing the mutex.
	 */
	private static final int MIN_INTERVAL = 30000;
	
	/**
	 * Logger of this class.
	 */
	private Logger log = LoggerFactory.getLogger(TimerThread.class);
	
	/**
	 * {@linkplain Scaler} to trigger
	 */
	private Scaler scaler;
 
	/**
	 * Boolean value, whether the TimerThread is actively running.
	 */
	private boolean running;
	
	/**
	 * index of the current interval
	 */
	private long intervalCount;
	
	/**
	 * Constructor for setting up TimerThread with a given {@linkplain Scaler}.
	 * @param scaler scaler object to trigger
	 */
	public TimerThread(Scaler scaler) {
		super("TimerThread");
		running = true;
		this.scaler = scaler;
		intervalCount = 0;
	}
	
	/**
	 * Waits for the time defined in the {@linkplain #MIN_INTERVAL} and then releases the mutex of the {@linkplain Scaler} in a loop.
	 */
	public void run() {
		try {
			while (running) {
				sleep(MIN_INTERVAL);
				increaseInterval();
				log.info("Scaling interval "+intervalCount+" ended.");
				scaler.releaseMutex();
			}
		} catch (InterruptedException ex) {}
		log.info("Timer thread stopped.");
		scaler.releaseMutex();
	}
	
	/**
	 * Stops the TimerThread instantly via an interrupt.
	 */
	public void stopThread() {
		this.interrupt();
	}
	
	/**
	 * Stops the TimerThread after the next interval.
	 */
	public void stopThreadAfterNextInterval() {
		running = false;
	}
	
	/**
	 * Increases the interval number and in case of an overflow sets it back to 1.
	 */
	private void increaseInterval() {
		intervalCount++;
		if (intervalCount < 1) 
			intervalCount = 1;
	}
}
