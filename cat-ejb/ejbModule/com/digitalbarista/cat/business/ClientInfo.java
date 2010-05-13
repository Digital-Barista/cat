package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.data.ClientInfoDO;
import com.digitalbarista.cat.data.EntryPointType;

@XmlRootElement
public class ClientInfo implements
		BusinessObject<ClientInfoDO>,Auditable {

	@PrimaryDescriminator
	private Long clientInfoId;
	private Long clientId;
	private EntryPointType entryType;
	private String name;
	private String value;
	

	@Override
	public void copyFrom(ClientInfoDO dataObject) 
	{
		clientInfoId = dataObject.getClientInfoId();
		entryType = dataObject.getEntryType();
		name = dataObject.getName();
		value = dataObject.getValue();
		
		if (dataObject.getClient() != null)
			clientId = dataObject.getClient().getPrimaryKey();
		
	}

	@Override
	public void copyTo(ClientInfoDO dataObject) 
	{
		dataObject.setEntryType(entryType);
		dataObject.setName(name);
		dataObject.setValue(value);
	}
	
	
	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("clientInfoId:" + getClientInfoId());
		ret.append(";entryType:" + getEntryType());
		ret.append(";name:" + getName());
		ret.append(";value:" + getValue());
		ret.append(";clientId:" + getClientId());
		return ret.toString();
	}


	@XmlAttribute(name="id")
	public Long getClientInfoId() {
		return clientInfoId;
	}

	public void setClientInfoId(Long clientInfoId) {
		this.clientInfoId = clientInfoId;
	}

	@XmlTransient
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	@XmlAttribute(name="entryType")
	public EntryPointType getEntryType() {
		return entryType;
	}
	
	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
}
