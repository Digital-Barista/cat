package com.digitalbarista.cat.business;

import java.io.Serializable;

import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.KeywordDO;

public class Keyword implements
		BusinessObject<KeywordDO>, Serializable {

	private Long entryPointId;
	private String incomingAddress;
	private EntryPointType incomingAddressType;
	private Long clientId;
	private String clientName;
	private String keyword;
	private Long primaryKey;
	private String campaignUID;
	private static final long serialVersionUID = 1L;
	
	@Override
	public void copyFrom(KeywordDO dataObject) {
		primaryKey = dataObject.getPrimaryKey();
		
		keyword = dataObject.getKeyword();
		clientId = dataObject.getClient().getPrimaryKey();
		clientName = dataObject.getClient().getName();
		entryPointId = dataObject.getEntryPoint().getPrimaryKey();
		incomingAddress = dataObject.getEntryPoint().getValue();
		incomingAddressType = dataObject.getEntryPoint().getType();
	}

	@Override
	public void copyTo(KeywordDO dataObject) {
		dataObject.setKeyword(keyword);
	}

	public Long getEntryPointId() {
		return entryPointId;
	}

	public void setEntryPointId(Long entryPointId) {
		this.entryPointId = entryPointId;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getIncomingAddress() {
		return incomingAddress;
	}

	public EntryPointType getIncomingAddressType() {
		return incomingAddressType;
	}

	public String getClientName() {
		return clientName;
	}

	public String getCampaignUID() {
		return campaignUID;
	}

	public void setCampaignUID(String campaignUID) {
		this.campaignUID = campaignUID;
	}

	public void setIncomingAddress(String incomingAddress) {
		this.incomingAddress = incomingAddress;
	}

	public void setIncomingAddressType(EntryPointType incomingAddressType) {
		this.incomingAddressType = incomingAddressType;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

}
