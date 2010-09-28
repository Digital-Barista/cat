package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.MapKey;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Entity implementation class for Entity: ConnectorDO
 *
 */

@Entity
@Table(name="connectors")
@NamedQuery(name="connector.by.uuid", query="select c from ConnectorDO c where c.UID=:uuid")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
public class ConnectorDO implements Serializable,DataObject {

	private Set<NodeConnectorLinkDO> connections = new HashSet<NodeConnectorLinkDO>();
	private Map<Integer,CampaignConnectorLinkDO> versionedConnectors = new HashMap<Integer,CampaignConnectorLinkDO>();
	private Set<ConnectorInfoDO> connectorInfo = new HashSet<ConnectorInfoDO>();
	private Long primaryKey;
	private String name;
	private String UID;
	private ConnectorType type;
	private CampaignDO campaign;
	private static final long serialVersionUID = 1L;

	public ConnectorDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="connector_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="uid")
	public String getUID() {
		return this.UID;
	}

	public void setUID(String UID) {
		this.UID = UID;
	}
	
	@Column(name="type")
	@Enumerated(EnumType.ORDINAL)
	public ConnectorType getType() {
		return this.type;
	}

	public void setType(ConnectorType type) {
		this.type = type;
	}

	@OneToMany(mappedBy="connector",targetEntity=NodeConnectorLinkDO.class)
	@BatchSize(size=100)
	@Fetch(FetchMode.SELECT)
	@OrderBy("version DESC")
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	public Set<NodeConnectorLinkDO> getConnections() {
		return connections;
	}

	public void setConnections(Set<NodeConnectorLinkDO> connections) {
		this.connections = connections;
	}

	@OneToMany(mappedBy="connector",targetEntity=CampaignConnectorLinkDO.class)
	@MapKey(name="version")
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	public Map<Integer, CampaignConnectorLinkDO> getVersionedConnectors() {
		return versionedConnectors;
	}

	public void setVersionedConnectors(
			Map<Integer, CampaignConnectorLinkDO> versionedConnectors) {
		this.versionedConnectors = versionedConnectors;
	}

	@OneToMany(mappedBy="connector",targetEntity=ConnectorInfoDO.class,cascade=CascadeType.ALL)
	@BatchSize(size=100)
	@Fetch(FetchMode.SELECT)
	@org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	@OrderBy("version DESC")
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL)
	public Set<ConnectorInfoDO> getConnectorInfo() {
		return connectorInfo;
	}

	public void setConnectorInfo(Set<ConnectorInfoDO> connectorInfoLinks) {
		this.connectorInfo = connectorInfoLinks;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="campaign_id")
	public CampaignDO getCampaign() {
		return campaign;
	}

	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}
   
}
