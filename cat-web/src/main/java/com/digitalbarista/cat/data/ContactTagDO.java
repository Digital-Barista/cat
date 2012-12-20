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

@Entity
@Table(name="contact_tag")
public class ContactTagDO implements Serializable,DataObject {

	private Long contactTagId;
	private String tag;
	private ContactTagType type;
	private ClientDO client;

	public ContactTagDO() {
		super();
	}
	

	@Id
	@Column(name="contact_tag_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getContactTagId() {
		return contactTagId;
	}

	public void setContactTagId(Long contactTagId) {
		this.contactTagId = contactTagId;
	}

	@Column(name="tag")
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}


	@Column(name="type")
	public ContactTagType getType() {
		return type;
	}


	public void setType(ContactTagType type) {
		this.type = type;
	}

	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="client_id")
	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}
   
}
