package com.digitalbarista.cat.business.criteria;

import java.util.List;

import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.data.EntryPointType;

public class ContactSearchCriteria {

	private List<Long> clientIds;
	private EntryPointType entryType;
	private List<ContactTag> contactTags;
	
	
	public List<Long> getClientIds() {
		return clientIds;
	}
	public void setClientIds(List<Long> clientIds) {
		this.clientIds = clientIds;
	}
	public EntryPointType getEntryType() {
		return entryType;
	}
	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}
	public List<ContactTag> getContactTags() {
		return contactTags;
	}
	public void setContactTags(List<ContactTag> contactTags) {
		this.contactTags = contactTags;
	}
}
