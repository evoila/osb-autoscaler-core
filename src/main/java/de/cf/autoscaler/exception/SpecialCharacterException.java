package de.cf.autoscaler.exception;

import de.cf.autoscaler.applications.ScalableApp;

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
