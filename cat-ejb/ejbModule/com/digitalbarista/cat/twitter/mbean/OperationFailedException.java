package com.digitalbarista.cat.twitter.mbean;

public class OperationFailedException extends Exception {

	public OperationFailedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public OperationFailedException(String arg0) {
		super(arg0);
	}

	public OperationFailedException(Throwable arg0) {
		super(arg0);
	}
	

}
