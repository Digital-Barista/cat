package com.digitalbarista.cat.util;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="CouponRedemption")
@XmlType(name="CouponRedemption")
public class CodedMessage {
	private int code;
	private String message;
	private String detailedMessage;
	private String offerCode;
	
	public CodedMessage(){}
	
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
	
	@XmlAttribute(name="statusCode")
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	
	@XmlAttribute(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@XmlAttribute(name="detailedMessage")
	public String getDetailedMessage() {
		return detailedMessage;
	}
	public void setDetailedMessage(String detailedMessage) {
		this.detailedMessage = detailedMessage;
	}

	@XmlAttribute(name="offerCode")
	public String getOfferCode() {
		return offerCode;
	}
	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}
}
