package com.digitalbarista.cat.mail;

import java.io.Serializable;
import java.util.Date;

import javax.mail.Address;

public class MessageSummary implements Serializable{
	private static final long serialVersionUID = 1L;
	private Address[] recipients;
	private Object content;
	private String contentType;
	private Address[] from;
	private Date receivedDate;
	private Date sentDate;
	private Address[] replyTo;
	private int size;
	private String subject;

	public Address[] getRecipients() {
		return recipients;
	}
	public void setRecipients(Address[] recipients) {
		this.recipients = recipients;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Address[] getFrom() {
		return from;
	}
	public void setFrom(Address[] from) {
		this.from = from;
	}
	public Date getReceivedDate() {
		return receivedDate;
	}
	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}
	public Address[] getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(Address[] replyTo) {
		this.replyTo = replyTo;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Date getSentDate() {
		return sentDate;
	}
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}
}
