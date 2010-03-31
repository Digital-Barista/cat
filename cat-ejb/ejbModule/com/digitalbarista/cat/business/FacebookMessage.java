package com.digitalbarista.cat.business;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.FacebookMessageDO;

@XmlRootElement(name="message")
public class FacebookMessage implements
		BusinessObject<FacebookMessageDO> {

	private Integer facebookMessageId;
	private String title;
	private String body;
	private Calendar createDate;
	private String formattedCreateDate;
	private String metadata;
	private String response;

	@Override
	public void copyFrom(FacebookMessageDO dataObject) {
		facebookMessageId = dataObject.getFacebookMessageId();
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

	@XmlAttribute
	public Integer getFacebookMessageId() {
		return facebookMessageId;
	}
	public void setFacebookMessageId(Integer facebookMessageId) {
		this.facebookMessageId = facebookMessageId;
	}

	@XmlAttribute
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	@XmlAttribute
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	@XmlAttribute
	public Calendar getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}

	@XmlAttribute
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	@XmlAttribute
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	
	@XmlAttribute
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
	
}
