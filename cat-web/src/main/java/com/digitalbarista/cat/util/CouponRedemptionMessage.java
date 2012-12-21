package com.digitalbarista.cat.util;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="CouponRedemption")
@XmlType(name="CouponRedemption")
public class CouponRedemptionMessage {
	private int code;
	private String message;
	private String detailedMessage;
	private String offerCode;
	private String contactUID;
	
	public CouponRedemptionMessage(){}
	
	public CouponRedemptionMessage(int code, String message, String detailedMessage, String contactUID)
	{
		this.code=code;
		this.message=message;
		this.detailedMessage=detailedMessage;
		this.contactUID = contactUID;
	}
	
	public CouponRedemptionMessage(int code, String message, String detailedMessage, String offerCode, String contactUID)
	{
		this.code=code;
		this.message=message;
		this.detailedMessage=detailedMessage;
		this.offerCode=offerCode;
		this.contactUID=contactUID;
	}
	
	public CouponRedemptionMessage(int code, String message)
	{
		this(code,message,message,null);
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

	@XmlAttribute(name="contactUID")
	public String getContactUID() {
		return contactUID;
	}
	public void setContactUID(String contactUID) {
		this.contactUID = contactUID;
	}
}
