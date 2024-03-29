package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * Entity implementation class for Contact: ContactDO
 *
 */
@Entity
@Table(name="contact")
@NamedQuery(name="contact.by.address.and.client",query="from ContactDO c where c.address=:address and c.type=:type and c.client.id=:clientId")
public class ContactDO implements DataObject,Serializable {

	
	private Long contactId;
	private String UID;
	private String address;
	private Calendar createDate;
	private ClientDO client;
	private EntryPointType type;
	private String alternateId;
	private Set<ContactTagLinkDO> contactTags = new HashSet<ContactTagLinkDO>();
	private Set<ContactInfoDO> contactInfos = new HashSet<ContactInfoDO>();
	private BlacklistDO blacklist;
	private static final long serialVersionUID = 1L;
	
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


	@OneToMany(mappedBy="contact", targetEntity=ContactTagLinkDO.class, cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY)
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

	@OneToMany(fetch=FetchType.LAZY, targetEntity=ContactInfoDO.class, mappedBy="contact")
	public Set<ContactInfoDO> getContactInfos() {
		return contactInfos;
	}

	public void setContactInfos(Set<ContactInfoDO> contactInfos) {
		this.contactInfos = contactInfos;
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


	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
        @JoinColumn(name="address", referencedColumnName="address", insertable=false, updatable=false),
        @JoinColumn(name="type", referencedColumnName="entry_type", insertable=false, updatable=false)
    })
	public BlacklistDO getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(BlacklistDO blacklist) {
		this.blacklist = blacklist;
	}

	@Column(name="contact_uid")
	public String getUID() {
		if(UID==null)
			UID = UUID.randomUUID().toString();
		return UID;
	}

	public void setUID(String uID) {
		UID = uID;
	}
}
