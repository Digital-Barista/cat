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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: CampaignSubscriberLinkDO
 *
 */
@Entity
@Table(name="campaign_subscriber_link")
@NamedQueries({
	@NamedQuery(name="subscription.count.for.node", query="select count(csl.primaryKey) from CampaignSubscriberLinkDO csl where csl.lastHitNode.UID=:nodeUID")
})
public class CampaignSubscriberLinkDO implements Serializable {

	private Long primaryKey;
	private CampaignDO campaign;
	private SubscriberDO subscriber;
	private NodeDO lastHitNode;
	private String lastHitEntryPoint;
	private EntryPointType lastHitEntryType;
	private boolean active=true;
	private static final long serialVersionUID = 1L;

	public CampaignSubscriberLinkDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="campaign_subscriber_link_id")
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
	@JoinColumn(name="subscriber_id")
	public SubscriberDO getSubscriber() {
		return this.subscriber;
	}

	public void setSubscriber(SubscriberDO subscriber) {
		this.subscriber = subscriber;
	}   

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="last_completed_node_id")
	public NodeDO getLastHitNode() {
		return this.lastHitNode;
	}

	public void setLastHitNode(NodeDO lastHitNode) {
		this.lastHitNode = lastHitNode;
	}

	@Column(name="last_used_entry_point")
	public String getLastHitEntryPoint() {
		return lastHitEntryPoint;
	}

	public void setLastHitEntryPoint(String lastHitEntryPoint) {
		this.lastHitEntryPoint = lastHitEntryPoint;
	}

	@Column(name="last_used_entry_point_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getLastHitEntryType() {
		return lastHitEntryType;
	}

	public void setLastHitEntryType(EntryPointType lastHitEntryType) {
		this.lastHitEntryType = lastHitEntryType;
	}

	@Column(name="active")
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
   
}
