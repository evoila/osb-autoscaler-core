package de.evoila.cf.autoscaler.core.exception;

import de.evoila.cf.autoscaler.core.model.ScalableApp;

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
