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
 * Entity implementation class for Entity: NodeDO
 *
 */

@Entity
@Table(name="nodes")
@NamedQuery(name="node.by.uuid", query="select n from NodeDO n where n.UID=:uuid")
public class NodeDO implements Serializable,DataObject {
	
	private Long primaryKey;
	private CampaignDO campaign;
	private NodeType type;
	private String name;
	private String UID;
	private Map<Integer,CampaignNodeLinkDO> versionedNodes = new HashMap<Integer,CampaignNodeLinkDO>();
	private Set<NodeConnectorLinkDO> connections = new HashSet<NodeConnectorLinkDO>();
	private Set<NodeInfoDO> nodeInfo = new HashSet<NodeInfoDO>();
	private Set<CampaignSubscriberLinkDO> currentSubscribers = new HashSet<CampaignSubscriberLinkDO>();
	
	
	private static final long serialVersionUID = 1L;

	public NodeDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="node_id")
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
	
	@Column(name="type")
	@Enumerated(EnumType.ORDINAL)
	public NodeType getType() {
		return this.type;
	}

	public void setType(NodeType type) {
		this.type = type;
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

	@OneToMany(mappedBy="node",targetEntity=CampaignNodeLinkDO.class)
	@MapKey(name="version")
	public Map<Integer, CampaignNodeLinkDO> getVersionedNodes() {
		return versionedNodes;
	}

	public void setVersionedNodes(Map<Integer, CampaignNodeLinkDO> versionedNodes) {
		this.versionedNodes = versionedNodes;
	}

	@OneToMany(mappedBy="node",targetEntity=NodeConnectorLinkDO.class,fetch=FetchType.EAGER)
	@BatchSize(size=100)
	@OrderBy("version DESC")
	public Set<NodeConnectorLinkDO> getConnections() {
		return connections;
	}

	public void setConnections(Set<NodeConnectorLinkDO> connections) {
		this.connections = connections;
	}

	@OneToMany(mappedBy="node",targetEntity=NodeInfoDO.class,cascade=CascadeType.ALL,fetch=FetchType.EAGER)
	@BatchSize(size=100)
	@org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
	@OrderBy("version DESC")
	public Set<NodeInfoDO> getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(Set<NodeInfoDO> nodeInfoLinks) {
		this.nodeInfo = nodeInfoLinks;
	}

	@OneToMany(mappedBy="lastHitNode",targetEntity=CampaignSubscriberLinkDO.class)
	public Set<CampaignSubscriberLinkDO> getCurrentSubscribers() {
		return currentSubscribers;
	}

	public void setCurrentSubscribers(Set<CampaignSubscriberLinkDO> currentSubscribers) {
		this.currentSubscribers = currentSubscribers;
	}
   
}
