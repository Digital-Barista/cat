package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Calendar;
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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity implementation class for Contact: ContactDO
 *
 */
@Entity
@Table(name="contact")

public class ContactDO implements DataObject,Serializable {

	
	private Long contactId;
	private String address;
	private Calendar createDate;
	private ClientDO client;
	private EntryPointType type;
	private String alternateId;
	private Set<ContactTagLinkDO> contactTags;
//	private SubscriberBlacklistDO subscriberBlacklist;
	
	public ContactDO() {
		super();
	}
	
	@Id
	@Column(name="contact_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getContactId() {
		return contactId;
	}

	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}


	@OneToMany(mappedBy="contact", targetEntity=ContactTagLinkDO.class)
	@JoinColumn(updatable=false,insertable=false,name="contact_id")
	public Set<ContactTagLinkDO> getContactTags() {
		return contactTags;
	}

	public void setContactTags(Set<ContactTagLinkDO> contactTags) {
		this.contactTags = contactTags;
	}

	public ContactTagLinkDO findLink(ContactTagDO tag)
	{
		for(ContactTagLinkDO link : getContactTags())
		{
			if(link.getTag()==tag)
				return link;
		}
		return null;
	}
	
	@Column(name="address")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name="create_date")
	public Calendar getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="client_id")
	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getType() {
		return this.type;
	}

	public void setType(EntryPointType type) {
		this.type = type;
	}

	@Column(name="alternate_id")
	public String getAlternateId() {
		return alternateId;
	}

	public void setAlternateId(String alternateId) {
		this.alternateId = alternateId;
	}

	// This isn't right because th "incoming_address" of the blacklist isn't the address that's blacklisted
//	@ManyToOne(fetch=FetchType.EAGER)
//	@JoinColumns({
//        @JoinColumn(name="address", referencedColumnName="incoming_address", insertable=false, updatable=false),
//        @JoinColumn(name="type", referencedColumnName="incoming_type", insertable=false, updatable=false)
//    })
//	public SubscriberBlacklistDO getSubscriberBlacklist() {
//		return subscriberBlacklist;
//	}
//
//	public void setSubscriberBlacklist(SubscriberBlacklistDO subscriberBlacklist) {
//		this.subscriberBlacklist = subscriberBlacklist;
//	}

	
	
	
}
