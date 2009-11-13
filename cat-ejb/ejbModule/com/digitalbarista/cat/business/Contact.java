package com.digitalbarista.cat.business;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.RoleDO;

@XmlRootElement
public class Contact implements BusinessObject<ContactDO>{

	private Long contactId;
	private String address;
	private Calendar createDate;
	private Set<ContactTag> contactTags;
	private Long clientId;
	private EntryPointType type;
	
	
	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	public Long getContactId() {
		return contactId;
	}
	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Calendar getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Calendar createDate) {
		this.createDate = createDate;
	}
	public Set<ContactTag> getContactTags() {
		return contactTags;
	}
	public void setContactTags(Set<ContactTag> contactTags) {
		this.contactTags = contactTags;
	}
	
	
	public EntryPointType getType() {
		return type;
	}
	public void setType(EntryPointType type) {
		this.type = type;
	}
	@Override
	public void copyFrom(ContactDO dataObject) {
		contactId = dataObject.getContactId();
		address = dataObject.getAddress();
		createDate = dataObject.getCreateDate();
		clientId = dataObject.getClient().getPrimaryKey();
		type = dataObject.getType();
		
		contactTags = new HashSet<ContactTag>();
		if (dataObject.getContactTags() != null)
		{
			for (ContactTagDO tag : dataObject.getContactTags())
			{
				ContactTag temp = new ContactTag();
				temp.copyFrom(tag);
				contactTags.add(temp);
			}
		}
	}
	@Override
	public void copyTo(ContactDO dataObject) {
		dataObject.setContactId(contactId);
		dataObject.setAddress(address);
		dataObject.setCreateDate(createDate);
		dataObject.setType(type);
	}
	
	
}
