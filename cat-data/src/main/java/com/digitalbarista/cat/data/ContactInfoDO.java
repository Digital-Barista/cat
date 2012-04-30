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
@Table(name="contact_info")
public class ContactInfoDO implements DataObject, Serializable 
{
	private Long contactInfoId;
	private ContactDO contact;
	private String name;
	private String value;

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="contact_info_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getContactInfoId()
	{
		return contactInfoId;
	}
	public void setContactInfoId(Long contactInfoId)
	{
		this.contactInfoId = contactInfoId;
	}
	

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="contact_id")
	public ContactDO getContact()
	{
		return contact;
	}
	public void setContact(ContactDO contact)
	{
		this.contact = contact;
	}
	

	@Column(name="name")
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	

	@Column(name="value")
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	
	
}
