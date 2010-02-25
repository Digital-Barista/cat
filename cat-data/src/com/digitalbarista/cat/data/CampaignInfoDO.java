package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CampaignInfoDO
 *
 */

@Entity
@Table(name="campaign_info")
public class CampaignInfoDO implements Serializable,DataObject {

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="campaign_info_id")
	private Long campaignInfoId;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="campaign_id")
	private CampaignDO campaign;
	

	@Column(name="entry_type")
	@Enumerated(EnumType.STRING)
	private EntryPointType entryType;
	
	@Column(name="name")
	private String name;

	@Column(name="value")
	private String value;
	
	private static final long serialVersionUID = 1L;

	public CampaignInfoDO() {
		super();
	}

	public Long getCampaignInfoId() {
		return campaignInfoId;
	}

	public void setCampaignInfoId(Long campaignInfoId) {
		this.campaignInfoId = campaignInfoId;
	}

	public CampaignDO getCampaign() {
		return campaign;
	}

	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}

	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

   
	
}
