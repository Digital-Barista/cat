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
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: CampaignNodeLinkDO
 *
 */

@Entity
@Table(name="campaign_node_link")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/CampaignNodeLink")
public class CampaignNodeLinkDO implements Serializable,DataObject {

	private Long primaryKey;
	private CampaignDO campaign;
	private NodeDO node;
	private Integer version;
	private static final long serialVersionUID = 1L;

	public CampaignNodeLinkDO() {
		super();
	}   

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="cnl_id")
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
	
	@ManyToOne(fetch=FetchType.LAZY)
	@BatchSize(size=100)
	@JoinColumn(name="node_id")
	public NodeDO getNode() {
		return this.node;
	}

	public void setNode(NodeDO node) {
		this.node = node;
	}
	
	@Column(name="version")
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
   
}
