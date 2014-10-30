package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Date;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: CampaignVersionDO
 *
 */

@Entity
@Table(name="campaign_versions")
public class CampaignVersionDO implements Serializable,DataObject {

	private long primaryKey;
	private CampaignDO campaign;
	private Integer version;
	private CampaignVersionStatus status;
	private Date publishedDate;
	private static final long serialVersionUID = 1L;

	public CampaignVersionDO() {
		super();
	}   
	
	@Id    
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="campaign_version_id")
	public long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(long primaryKey) {
		this.primaryKey = primaryKey;
	}   
	
	@JoinColumn(name="campaign_id")
	@ManyToOne(fetch=FetchType.LAZY)
	public CampaignDO getCampaign() {
		return this.campaign;
	}

	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}   

	@Column(name="version",insertable=false,updatable=false)
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}   
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	public CampaignVersionStatus getStatus() {
		return this.status;
	}

	public void setStatus(CampaignVersionStatus status) {
		this.status = status;
	}   
	
	@Column(name="published_date")
	public Date getPublishedDate() {
		return this.publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}
}
