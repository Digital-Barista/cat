package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
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
	@NamedQuery(name="subscriber.by.email",query="select s from SubscriberDO s where s.email=:endpoint"),
	@NamedQuery(name="subscriber.by.phone",query="select s from SubscriberDO s where s.phoneNumber=:endpoint"),
	@NamedQuery(name="subscriber.by.twitter",query="select s from SubscriberDO s where s.twitterUsername=:endpoint"),
	@NamedQuery(name="all.subscribers.on.node",query="select s from CampaignSubscriberLinkDO l join l.subscriber s join l.lastHitNode n where n.UID=:nodeUID")
})
public class SubscriberDO implements Serializable,DataObject {

	private Long primaryKey;
	private String email;
	private String phoneNumber;
	private String twitterUsername;
	private static final long serialVersionUID = 1L;
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

	@Column(name="email")
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}   
	
	@Column(name="phone_number")
	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@OneToMany(mappedBy="subscriber",targetEntity=CampaignSubscriberLinkDO.class)
	@MapKey(name="campaign")
	public Map<CampaignDO,CampaignSubscriberLinkDO> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Map<CampaignDO,CampaignSubscriberLinkDO> subscriptions) {
		this.subscriptions = subscriptions;
	}

	@Column(name="twitter_name")
	public String getTwitterUsername() {
		return twitterUsername;
	}

	public void setTwitterUsername(String twitterUsername) {
		this.twitterUsername = twitterUsername;
	}
 
}
