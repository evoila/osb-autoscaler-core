package de.cf.autoscaler.scaling;

/**
 * A Thread dedicated to run the main scaling loop after the construction of the scaler.
 * @author Marius Berger
 *
 */
public class ScalingThread extends Thread {

	/**
	 * Scaler service to execute the loop.
	 */
	private Scaler sc;
	
	/**
	 * Constructor to create a ScalingThread with a {@linkplain Scaler} object.
	 * @param sc Scaler service to execute the loop.
	 */
	public ScalingThread(Scaler sc) {
		this.sc = sc;
	}
	
	/**
	 * Enters the scaling loop of the bound {@linkplain Scaler} service.
	 */
	@Override
	public void run() {
		sc.checkScalingLoop();
	}
}
