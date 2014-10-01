package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.AddInMessageType;
import com.digitalbarista.cat.data.EntryPointType;

@XmlRootElement
public class AddInMessage implements
		BusinessObject<AddInMessageDO>,Auditable {

	@PrimaryDescriminator
	private Long addInMessageId;
	private Long clientId;
	private Long campaignId;
	private EntryPointType entryType;
	private AddInMessageType type;
	private String message;
	
	
	@Override
	public void copyFrom(AddInMessageDO dataObject) {
		addInMessageId = dataObject.getAddInMessageId();
		entryType = dataObject.getEntryType();
		type = dataObject.getType();
		message = dataObject.getMessage();
		
		if (dataObject.getClient() != null)
			clientId = dataObject.getClient().getPrimaryKey();
		
		if (dataObject.getCampaign() != null)
			campaignId = dataObject.getCampaign().getPrimaryKey();
	}

	@Override
	public void copyTo(AddInMessageDO dataObject) {
		dataObject.setEntryType(entryType);
		dataObject.setMessage(message);
		dataObject.setType(type);
	}
	
	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("addInMessageId:" + getAddInMessageId());
		ret.append(";entryType:" + getEntryType());
		ret.append(";clientId:" + getClientId());
		ret.append(";campaignId:" + getCampaignId());
		ret.append(";message:" + getMessage());
		return ret.toString();
	}
	
	@XmlAttribute(name="id")
	public Long getAddInMessageId() {
		return addInMessageId;
	}

	public void setAddInMessageId(Long addInMessageId) {
		this.addInMessageId = addInMessageId;
	}

	@XmlTransient
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	@XmlTransient
	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	@XmlAttribute
	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	@XmlAttribute
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlAttribute
	public AddInMessageType getType() {
		return type;
	}

	public void setType(AddInMessageType type) {
		this.type = type;
	}
}
