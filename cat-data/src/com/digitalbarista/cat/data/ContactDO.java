package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKeyManyToMany;

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
	private Map<ContactTagDO,Date> contactTags;
	
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


	@CollectionOfElements(targetElement=Date.class)
	@MapKeyManyToMany(targetEntity=ContactTagDO.class,joinColumns=@JoinColumn(name="contact_tag_id"))
	@JoinTable(
		name="contact_tag_link",
		joinColumns=@JoinColumn(name="contact_id")
	)
	@JoinColumn(name="initial_tag_date")
	public Map<ContactTagDO,Date> getContactTags() {
		return contactTags;
	}

	public void setContactTags(Map<ContactTagDO,Date> contactTags) {
		this.contactTags = contactTags;
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
}
