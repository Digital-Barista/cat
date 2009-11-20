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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CampaignEntryPointDO
 *
 */

@Entity
@Table(name="campaign_entry_points")
@NamedQueries({
	@NamedQuery(name="campaign.entry.points",query="select cep from CampaignEntryPointDO cep where cep.type=:type and cep.entryPoint=:entryPoint"),
	@NamedQuery(name="find.matching.entry.point",query="select cep from CampaignEntryPointDO cep where cep.type=:type and cep.entryPoint=:entryPoint and cep.keyword=:keyword"),
	@NamedQuery(name="delete.campaign.entry.points",query="delete CampaignEntryPointDO cep where cep.campaign.id=:campaignID")
})
public class CampaignEntryPointDO implements Serializable {

	private Long primaryKey;
	private CampaignDO campaign;
	private String entryPoint;
	private String keyword;
	private EntryPointType type;
	private Integer quantity = 0;
	private boolean published;
	private static final long serialVersionUID = 1L;

	public CampaignEntryPointDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="campaign_entry_point_id")	
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="campaign_id")
	public CampaignDO getCampaign() {
		return this.campaign;
	}

	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}
	
	@Column(name="entry_point")
	public String getEntryPoint() {
		return this.entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}
	
	@Column(name="keyword")
	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	@Column(name="entry_point_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getType() {
		return type;
	}
	public void setType(EntryPointType type) {
		this.type = type;
	}

	@Column(name="entry_point_qty")
	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	 
	@Column(name="published")
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	} 
}
