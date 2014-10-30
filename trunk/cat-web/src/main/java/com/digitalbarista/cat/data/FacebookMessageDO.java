package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: FacebookMessageDO
 *
 */

@Entity
@Table(name="facebook_message")
public class FacebookMessageDO implements Serializable,DataObject {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="facebook_message_id")
	private Integer facebookMessageId;

	@Column(name="facebook_app_id")
	private String facebookAppName;
	
	@Column(name="title")
	private String title;

	@Column(name="body")
	private String body;
	
	@Column(name="facebook_uid")
	private String facebookUID;
	
	@Column(name="create_date")
	private Calendar createDate;
	
	@Column(name="metadata")
	private String metadata;
	
	@Column(name="response")
	private String response;
	
	private static final long serialVersionUID = 1L;

	public FacebookMessageDO() {
		super();
	}

	public Integer getFacebookMessageId() {
		return facebookMessageId;
	}

	public void setFacebookMessageId(Integer facebookMessageId) {
		this.facebookMessageId = facebookMessageId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFacebookUID() {
		return facebookUID;
	}

	public void setFacebookUID(String facebookUID) {
		this.facebookUID = facebookUID;
	}

	public Calendar getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getFacebookAppName() {
		return facebookAppName;
	}

	public void setFacebookAppName(String facebookAppName) {
		this.facebookAppName = facebookAppName;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}   
	
	   
}
