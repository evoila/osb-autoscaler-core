package de.cf.autoscaler.exception;

import de.cf.autoscaler.applications.ScalableApp;

/**
 * Exception to indicate an invalid working set for a {@linkplain ScalableApp}.
 * @author Marius Berger
 *
 */
public class InvalidWorkingSetException extends Exception {

	private static final long serialVersionUID = -9100975245637900760L;


	public InvalidWorkingSetException() {
		super();
	}
	
	public InvalidWorkingSetException(String message) {
		super(message);
	}
}
