package de.cf.autoscaler.exception;

import de.cf.autoscaler.applications.ScalableApp;

/**
 * Exception to indicate an invalid policy.
 * @author Marius Berger
 * @see ScalableApp
 * @see Exception
 */
public class InvalidPolicyException extends Exception{

	private static final long serialVersionUID = -5926963366159925829L;

	public InvalidPolicyException() {
		super();
	}
	
	public InvalidPolicyException(String message) {
		super(message);
	}
}
