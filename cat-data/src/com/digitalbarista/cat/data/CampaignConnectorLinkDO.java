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

/**
 * Entity implementation class for Entity: CampaignNodeLinkDO
 *
 */

@Entity
@Table(name="campaign_connector_link")
public class CampaignConnectorLinkDO implements Serializable,DataObject {

	private Long primaryKey;
	private CampaignDO campaign;
	private ConnectorDO connector;
	private Integer version;
	private static final long serialVersionUID = 1L;

	public CampaignConnectorLinkDO() {
		super();
	}   

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ccl_id")
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
	@JoinColumn(name="connector_id")
	public ConnectorDO getConnector() {
		return this.connector;
	}

	public void setConnector(ConnectorDO connector) {
		this.connector = connector;
	}
	
	@Column(name="version")
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
   
}
