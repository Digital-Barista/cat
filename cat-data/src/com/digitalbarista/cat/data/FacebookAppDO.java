package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: FacebookAppDO
 *
 */

@Entity
@Table(name="facebook_app")
public class FacebookAppDO implements Serializable,DataObject {

	@Id
	@Column(name="facebook_app_id")
	private String facebookAppId;

	@Column(name="api_key")
	private String apiKey;

	@Column(name="secret")
	private String secret;
	
	@Column(name="id")
	private String id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="client_id")
	private ClientDO client;
	
	private static final long serialVersionUID = 1L;

	public FacebookAppDO() {
		super();
	}

	public String getFacebookAppId() {
		return facebookAppId;
	}

	public void setFacebookAppId(String facebookAppId) {
		this.facebookAppId = facebookAppId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}
}
