package com.digitalbarista.cat.twitter.bindings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="hash")
public class ErrorMessage {
	private String request;
	private String errorMessage;
	
	@XmlElement(name="request")
	public String getRequest() {
		return request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}
	
	@XmlElement(name="error")
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
