package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.ContactTagType;
import com.digitalbarista.cat.data.EntryPointType;

@XmlType
public class ContactTag  implements BusinessObject<ContactTagDO>
{
	private Long contactTagId;
	private String tag;
	private ContactTagType type;
	private Long clientId;
	

	@XmlAttribute
	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	@XmlAttribute
	public Long getContactTagId() {
		return contactTagId;
	}
	public void setContactTagId(Long contactTagId) {
		this.contactTagId = contactTagId;
	}

	@XmlAttribute
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}

	@XmlAttribute
	public ContactTagType getType() {
		return type;
	}
	public void setType(ContactTagType type) {
		this.type = type;
	}
	@Override
	public void copyFrom(ContactTagDO dataObject) 
	{
		contactTagId = dataObject.getContactTagId();
		tag = dataObject.getTag();
		type = dataObject.getType();
		clientId = dataObject.getClient().getPrimaryKey();
	}
	@Override
	public void copyTo(ContactTagDO dataObject) 
	{
		dataObject.setContactTagId(contactTagId);
		dataObject.setTag(tag);
		dataObject.setType(type);
	}
	
	
}
