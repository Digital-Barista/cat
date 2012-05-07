package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: LayoutInfoDO
 *
 */

@Entity
@Table(name="layout_info")
@NamedQueries({
	@NamedQuery(name="layout.by.uuid",query="select li from LayoutInfoDO li where li.UID in(:uids) and li.campaign.currentVersion=li.version"),
	@NamedQuery(name="layouts.by.campaign.version",query="select li from LayoutInfoDO li where li.campaign.id=:campaignPK and li.version=:version")
})
public class LayoutInfoDO implements Serializable,DataObject {

	private Long primaryKey;
	private Integer xLoc;
	private Integer yLoc;
	private Integer version;
	private String UID;
	private CampaignDO campaign;
	private static final long serialVersionUID = 1L;

	public LayoutInfoDO() {
		super();
	}   
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="layout_info_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}   
	
	@Column(name="x_loc")
	public Integer getXLoc() {
		return this.xLoc;
	}

	public void setXLoc(Integer xLoc) {
		this.xLoc = xLoc;
	}   
	
	@Column(name="y_loc")
	public Integer getYLoc() {
		return this.yLoc;
	}

	public void setYLoc(Integer yLoc) {
		this.yLoc = yLoc;
	}   
	
	@Column(name="version")
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}   
	
	@Column(name="uid")
	public String getUID() {
		return this.UID;
	}

	public void setUID(String UID) {
		this.UID = UID;
	}   
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="campaign_id")
	public CampaignDO getCampaign() {
		return this.campaign;
	}

	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}
   
}
