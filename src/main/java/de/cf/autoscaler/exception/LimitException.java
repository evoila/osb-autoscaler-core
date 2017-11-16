package de.cf.autoscaler.exception;

import de.cf.autoscaler.applications.ScalableApp;

/**
 * Exception to indicate an invalid limit or number.
 * @author Marius Berger
 * @see ScalableApp
 * @see Exception
 */
public class LimitException extends Exception{

	private static final long serialVersionUID = 3663549552950918030L;

	public LimitException() {
		super();
	}
	
	public LimitException(String message) {
		super(message);
	}
}
