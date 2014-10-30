package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: SubscriberDO
 *
 */
@Entity
@Table(name="subscribers")
@NamedQueries({
	@NamedQuery(name="subscriber.by.address",query="select s from SubscriberDO s where s.address=:endpoint and s.type=:type"),
	@NamedQuery(name="all.subscribers.on.node",query="select s from CampaignSubscriberLinkDO l join l.subscriber s join l.lastHitNode n where n.UID=:nodeUID")
})
public class SubscriberDO implements Serializable,DataObject {

	private Long primaryKey;
	private EntryPointType type;
	private String address;
	private Map<CampaignDO,CampaignSubscriberLinkDO> subscriptions = new HashMap<CampaignDO,CampaignSubscriberLinkDO>();

	public SubscriberDO() {
		super();
	}   

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="subscriber_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}   

	@Column(name="subscriber_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getType() {
		return this.type;
	}

	public void setType(EntryPointType type) {
		this.type = type;
	}   
	
	@Column(name="address")
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@OneToMany(mappedBy="subscriber",targetEntity=CampaignSubscriberLinkDO.class)
	@MapKey(name="campaign")
	public Map<CampaignDO,CampaignSubscriberLinkDO> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Map<CampaignDO,CampaignSubscriberLinkDO> subscriptions) {
		this.subscriptions = subscriptions;
	}
}
