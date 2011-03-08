package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.data.FacebookAppDO;

@XmlRootElement
public class FacebookApp implements
		BusinessObject<FacebookAppDO>,Auditable {

	@PrimaryDescriminator
	private String appName;
	private String apiKey;
	private String secret;
	private String id;
	private Long clientId;
	private String clientName;
	
	@Override
	public void copyFrom(FacebookAppDO dataObject) {
		appName = dataObject.getAppName();
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
		dataObject.setAppName(appName);
		dataObject.setApiKey(apiKey);
		dataObject.setSecret(secret);
		dataObject.setId(id);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("appName:" + getAppName());
		ret.append(";apiKey:"+getApiKey());
		ret.append(";id:" + getId());
		ret.append(";clientId:" + getClientId());
		return ret.toString();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
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
