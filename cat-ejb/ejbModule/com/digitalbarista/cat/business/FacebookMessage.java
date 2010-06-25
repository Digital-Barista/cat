package com.digitalbarista.cat.business;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.FacebookMessageDO;

@XmlRootElement(name="message")
public class FacebookMessage implements
		BusinessObject<FacebookMessageDO> {

	private Integer facebookMessageId;
	private String facebookAppId;
	private String title;
	private String body;
	private Calendar createDate;
	private String formattedCreateDate;
	private String metadata;
	private String response;

	@Override
	public void copyFrom(FacebookMessageDO dataObject) {
		facebookMessageId = dataObject.getFacebookMessageId();
		facebookAppId = dataObject.getFacebookAppId();
		title = dataObject.getTitle();
		body = dataObject.getBody();
		createDate = dataObject.getCreateDate();
		metadata = dataObject.getMetadata();
		response = dataObject.getResponse();
		
	}
	@Override
	public void copyTo(FacebookMessageDO dataObject) {
		// TODO Auto-generated method stub
		
	}

	@XmlElement
	public Integer getFacebookMessageId() {
		return facebookMessageId;
	}
	public void setFacebookMessageId(Integer facebookMessageId) {
		this.facebookMessageId = facebookMessageId;
	}

	@XmlElement
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	@XmlElement
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	@XmlElement
	public Calendar getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}

	@XmlElement
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	@XmlElement
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}

	@XmlElement
	public String getFacebookAppId() {
		return facebookAppId;
	}
	public void setFacebookAppId(String facebookAppId) {
		this.facebookAppId = facebookAppId;
	}
	
	@XmlElement
	public String getFormattedCreateDate()
	{
		if (formattedCreateDate == null)
		{
			SimpleDateFormat df = new SimpleDateFormat("MMMMM d 'at' hh:mm");
			SimpleDateFormat ampm = new SimpleDateFormat("aaa");
			formattedCreateDate = df.format(createDate.getTime()) + ampm.format(createDate.getTime()).toLowerCase();
		}
		return formattedCreateDate;
	}	
	public void setFormattedCreateDate(String createDate)
	{
	}
	
}
