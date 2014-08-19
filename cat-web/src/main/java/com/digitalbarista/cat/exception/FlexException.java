package com.digitalbarista.cat.exception;

public class FlexException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FlexException(String message)
	{
		super(message, null);
	}
}
