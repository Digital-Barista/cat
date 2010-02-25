package com.digitalbarista.cat.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="contact_tag_link")
public class ContactTagLinkDO {

	private ContactDO contact;
	private ContactTagDO tag;
	private Long primaryKey;
	private Date initialTagDate;
	
	@ManyToOne
	@JoinColumn(name="contact_id")
	public ContactDO getContact() {
		return contact;
	}
	public void setContact(ContactDO contact) {
		this.contact = contact;
	}
	
	@ManyToOne
	@JoinColumn(name="contact_tag_id")
	public ContactTagDO getTag() {
		return tag;
	}
	public void setTag(ContactTagDO tag) {
		this.tag = tag;
	}
	
	@Id
	@Column(name="contact_tag_link_id")
	public Long getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="initial_tag_date")
	public Date getInitialTagDate() {
		return initialTagDate;
	}
	public void setInitialTagDate(Date initialTagDate) {
		this.initialTagDate = initialTagDate;
	}
	
	
}
