package com.digitalbarista.cat.util;

public class CodedMessage {
	private int code;
	private String message;
	private String detailedMessage;
	
	public CodedMessage(int code, String message, String detailedMessage)
	{
		this.code=code;
		this.message=message;
		this.detailedMessage=detailedMessage;
	}
	
	public CodedMessage(int code, String message)
	{
		this(code,message,message);
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDetailedMessage() {
		return detailedMessage;
	}
	public void setDetailedMessage(String detailedMessage) {
		this.detailedMessage = detailedMessage;
	}
}
