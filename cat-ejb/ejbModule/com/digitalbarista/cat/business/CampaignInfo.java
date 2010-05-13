package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.EntryPointType;

@XmlRootElement
public class CampaignInfo implements
		BusinessObject<CampaignInfoDO>,Auditable {

	@PrimaryDescriminator
	private Long campaignInfoId;
	private Long campaignId;
	private EntryPointType entryType;
	private String name;
	private String value;
	
	
	@Override
	public void copyFrom(CampaignInfoDO dataObject) {
		campaignInfoId = dataObject.getCampaignInfoId();
		entryType = dataObject.getEntryType();
		name = dataObject.getName();
		value = dataObject.getValue();
		
		if (dataObject.getCampaign() != null)
			campaignId = dataObject.getCampaign().getPrimaryKey();
	}

	@Override
	public void copyTo(CampaignInfoDO dataObject) {
		dataObject.setEntryType(entryType);
		dataObject.setName(name);
		dataObject.setValue(value);
	}
	
	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("campaignInfoId:" + getCampaignInfoId());
		ret.append(";entryType:" + getEntryType());
		ret.append(";name:" + getName());
		ret.append(";value:" + getValue());
		ret.append(";campaignId:" + getCampaignId());
		return ret.toString();
	}

	@XmlAttribute(name="id")
	public Long getCampaignInfoId() {
		return campaignInfoId;
	}

	public void setCampaignInfoId(Long campaignInfoId) {
		this.campaignInfoId = campaignInfoId;
	}

	@XmlTransient
	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
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
