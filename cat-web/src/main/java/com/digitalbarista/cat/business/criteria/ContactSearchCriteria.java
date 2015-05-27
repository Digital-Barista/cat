package com.digitalbarista.cat.business.criteria;

import java.util.List;

import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.data.EntryPointType;

public class ContactSearchCriteria {

	private List<Long> clientIds;
	private List<EntryPointType> entryTypes; 
	private List<ContactTag> contactTags;
	private String address;
        private String contactUID;

        public String getContactUID() {
            return contactUID;
        }
        public void setContactUID(String contactUID) {
            this.contactUID = contactUID;
        }
	public List<Long> getClientIds() {
		return clientIds;
	}
	public void setClientIds(List<Long> clientIds) {
		this.clientIds = clientIds;
	}
	
	public List<EntryPointType> getEntryTypes()
	{
		return entryTypes;
	}
	public void setEntryTypes(List<EntryPointType> entryTypes)
	{
		this.entryTypes = entryTypes;
	}
	public List<ContactTag> getContactTags() {
		return contactTags;
	}
	public void setContactTags(List<ContactTag> contactTags) {
		this.contactTags = contactTags;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}
