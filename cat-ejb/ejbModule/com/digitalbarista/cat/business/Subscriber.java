package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.SubscriberDO;

@XmlRootElement
public class Subscriber implements BusinessObject<SubscriberDO>{

	private Long subscriberId;
	private EntryPointType entryPointType;
	private String address;

	@Override
	public void copyFrom(SubscriberDO dataObject) {
		subscriberId = dataObject.getPrimaryKey();
		
		entryPointType = dataObject.getType();
		address = dataObject.getAddress();
	}
	@Override
	public void copyTo(SubscriberDO dataObject) {
		// TODO Auto-generated method stub
		
	}
	
	public Long getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(Long subscriberId) {
		this.subscriberId = subscriberId;
	}
	public EntryPointType getEntryPointType() {
		return entryPointType;
	}
	public void setEntryPointType(EntryPointType entryPointType) {
		this.entryPointType = entryPointType;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
}
