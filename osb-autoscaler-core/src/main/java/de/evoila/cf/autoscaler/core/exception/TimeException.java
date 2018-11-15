package de.evoila.cf.autoscaler.core.exception;

import de.evoila.cf.autoscaler.core.model.ScalableApp;

/**
 * Exception to indicate an invalid time stamb or number for a field regarding time.
 * @author Marius Berger
 * @see ScalableApp
 * @see Exception
 */
public class TimeException extends Exception{

	private static final long serialVersionUID = -6982941550362270401L;

	public TimeException() {
		super();
	}
	
	public TimeException(String message) {
		super(message);
	}
}
