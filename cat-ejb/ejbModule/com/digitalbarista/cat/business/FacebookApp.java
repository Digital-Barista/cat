package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.data.FacebookAppDO;

@XmlRootElement
public class FacebookApp implements
		BusinessObject<FacebookAppDO>,Auditable {

	@PrimaryDescriminator
	private String facebookAppId;
	private String apiKey;
	private String secret;
	private String id;
	private Long clientId;
	private String clientName;
	
	@Override
	public void copyFrom(FacebookAppDO dataObject) {
		facebookAppId = dataObject.getFacebookAppId();
		apiKey = dataObject.getApiKey();
		secret = dataObject.getSecret();
		id = dataObject.getId();

		if (dataObject.getClient() != null)
		{
			clientName = dataObject.getClient().getName();
			clientId = dataObject.getClient().getPrimaryKey();
		}
	}

	@Override
	public void copyTo(FacebookAppDO dataObject) {
		dataObject.setFacebookAppId(facebookAppId);
		dataObject.setApiKey(apiKey);
		dataObject.setSecret(secret);
		dataObject.setId(id);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("facebookAppId:" + getFacebookAppId());
		ret.append(";apiKey:"+getApiKey());
		ret.append(";id:" + getId());
		ret.append(";clientId:" + getClientId());
		return ret.toString();
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

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	
	
}
