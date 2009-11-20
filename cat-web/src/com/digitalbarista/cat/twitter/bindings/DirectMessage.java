package com.digitalbarista.cat.twitter.bindings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="direct_message")
public class DirectMessage {
	private static SimpleDateFormat df;
	static
	{
		df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	private long id;
	private long senderId;
	private String text;
	private long recipientId;
	private Date createdAt;
	private String senderScreenName;
	private String recipientScreenName;
	private Tweeter sender;
	private Tweeter recipient;
	
	@XmlElement(name="id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@XmlElement(name="sender_id")
	public long getSenderId() {
		return senderId;
	}
	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}
	
	@XmlElement(name="text")
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	@XmlElement(name="recipient_id")
	public long getRecipientId() {
		return recipientId;
	}
	public void setRecipientId(long recipientId) {
		this.recipientId = recipientId;
	}
	
	@XmlElement(name="created_at")
	public String getCreatedAt() {
		return df.format(createdAt);
	}
	public void setCreatedAt(String createdAt) {
		try
		{
			this.createdAt = df.parse(createdAt);
		}catch(Exception e){}
	}
	
	public Date getCreatedAtDate()
	{
		return createdAt;
	}
	public void setCreatedAtDate(Date ca)
	{
		createdAt = ca;
	}
	
	@XmlElement(name="sender_screen_name")
	public String getSenderScreenName() {
		return senderScreenName;
	}
	public void setSenderScreenName(String senderScreenName) {
		this.senderScreenName = senderScreenName;
	}

	@XmlElement(name="recipient_screen_name")
	public String getRecipientScreenName() {
		return recipientScreenName;
	}
	public void setRecipientScreenName(String recipientScreenName) {
		this.recipientScreenName = recipientScreenName;
	}
	
	@XmlElement(name="sender")
	public Tweeter getSender() {
		return sender;
	}
	public void setSender(Tweeter sender) {
		this.sender = sender;
	}

	@XmlElement(name="recipient")
	public Tweeter getRecipient() {
		return recipient;
	}
	public void setRecipient(Tweeter recipient) {
		this.recipient = recipient;
	}
}
