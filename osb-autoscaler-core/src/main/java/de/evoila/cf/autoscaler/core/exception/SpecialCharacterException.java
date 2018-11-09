package de.evoila.cf.autoscaler.core.exception;

import de.evoila.cf.autoscaler.core.applications.ScalableApp;

/**
 * Exception to indicate String with an invalid character.
 * @author Marius Berger
 * @see ScalableApp
 * @see Exception
 */
public class SpecialCharacterException extends Exception{

	private static final long serialVersionUID = -3438003824593983723L;

	public SpecialCharacterException() {
		super();
	}
	
	public SpecialCharacterException(String message) {
		super(message);
	}
}
