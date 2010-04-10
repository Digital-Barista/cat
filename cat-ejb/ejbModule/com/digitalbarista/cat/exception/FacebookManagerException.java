package com.digitalbarista.cat.exception;

public class FacebookManagerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FacebookManagerException(String message)
	{
		super(message, null);
	}
}
