package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CampaignDO
 *
 */

@Entity
@Table(name="campaigns")
@NamedQueries({
	@NamedQuery(name = "campaign.by.uuid", query = "select c from CampaignDO c where c.UID=:uuid"),
	@NamedQuery(name = "campaign.by.status", query ="select c from CampaignDO c where c.status=:status"),
	@NamedQuery(name = "campaign.by.status.with.security", query ="select c from CampaignDO c where c.status=:status and c.client.id in (:clientIds)")
})
public class CampaignDO implements Serializable,DataObject {
	
	private Long primaryKey;
	private String name;
	private int currentVersion = 1;
	private static final long serialVersionUID = 1L;
	private Set<CampaignVersionDO> versions = new HashSet<CampaignVersionDO>();
	private Set<CampaignNodeLinkDO> nodes = new HashSet<CampaignNodeLinkDO>();
	private Set<CampaignConnectorLinkDO> connectors = new HashSet<CampaignConnectorLinkDO>();
	private Set<AddInMessageDO> addInMessages = new HashSet<AddInMessageDO>();
	private Set<CampaignInfoDO> campaignInfos = new HashSet<CampaignInfoDO>();
	private String UID;
	private ClientDO client;
	private CampaignStatus status=CampaignStatus.Active;
	private Set<CampaignSubscriberLinkDO> subscribers = new HashSet<CampaignSubscriberLinkDO>();
	private Set<CampaignEntryPointDO> entryPoints = new HashSet<CampaignEntryPointDO>();
	private CampaignMode mode;
	
	public CampaignDO() {
		super();
	}   

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="campaign_id")
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

	@Column(name="current_version")
	public int getCurrentVersion() {
		return this.currentVersion;
	}

	public void setCurrentVersion(int currentVersion) {
		this.currentVersion = currentVersion;
	}

	@OneToMany(targetEntity=CampaignVersionDO.class, fetch=FetchType.LAZY)
	public Set<CampaignVersionDO> getVersions() {
		return versions;
	}

	public void setVersions(Set<CampaignVersionDO> versions) {
		this.versions = versions;
	}

	@OneToMany(fetch=FetchType.LAZY, targetEntity=CampaignNodeLinkDO.class, mappedBy="campaign")
	@JoinColumn(insertable=false,updatable=false,name="campaign_id")
	public Set<CampaignNodeLinkDO> getNodes() {
		return nodes;
	}

	public void setNodes(Set<CampaignNodeLinkDO> nodes) {
		this.nodes = nodes;
	}


	@OneToMany(fetch=FetchType.LAZY, targetEntity=AddInMessageDO.class, mappedBy="campaign")
	@JoinColumn(insertable=false,updatable=false,name="campaign_id")
	public Set<AddInMessageDO> getAddInMessages() {
		return addInMessages;
	}

	public void setAddInMessages(Set<AddInMessageDO> addInMessages) {
		this.addInMessages = addInMessages;
	}

	

	@OneToMany(fetch=FetchType.LAZY, targetEntity=CampaignInfoDO.class, mappedBy="campaign")
	@JoinColumn(insertable=false,updatable=false,name="campaign_id")
	public Set<CampaignInfoDO> getCampaignInfos() {
		return campaignInfos;
	}

	public void setCampaignInfos(Set<CampaignInfoDO> campaignInfos) {
		this.campaignInfos = campaignInfos;
	}

	@Column(name="uid")
	public String getUID() {
		return UID;
	}

	public void setUID(String uid) {
		UID = uid;
	}

	@OneToMany(fetch=FetchType.LAZY, targetEntity=CampaignConnectorLinkDO.class, mappedBy="campaign")
	@JoinColumn(insertable=false,updatable=false,name="campaign_id")
	public Set<CampaignConnectorLinkDO> getConnectors() {
		return connectors;
	}

	public void setConnectors(Set<CampaignConnectorLinkDO> connectors) {
		this.connectors = connectors;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="client_id")
	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}

	@Column(name="status")
	@Enumerated(EnumType.STRING)
	public CampaignStatus getStatus() {
		return status;
	}

	public void setStatus(CampaignStatus status) {
		this.status = status;
	}

	@OneToMany(mappedBy="campaign",targetEntity=CampaignSubscriberLinkDO.class)
	public Set<CampaignSubscriberLinkDO> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(Set<CampaignSubscriberLinkDO> subscribers) {
		this.subscribers = subscribers;
	}

	@OneToMany(mappedBy="campaign",targetEntity=CampaignEntryPointDO.class)
	public Set<CampaignEntryPointDO> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(Set<CampaignEntryPointDO> entryPoints) {
		this.entryPoints = entryPoints;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof CampaignDO))
			return false;
		if(UID!=null)
			return !UID.equals(((CampaignDO)obj).UID);
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		if(UID==null) return 0;
		return UID.hashCode();
	}

	@Column(name="mode")
	@Enumerated(EnumType.STRING)
	public CampaignMode getMode() {
		return mode;
	}

	public void setMode(CampaignMode mode) {
		this.mode = mode;
	}
}
