package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.EntryPointType;

@XmlRootElement(name="Address")
public class Address {
	private String address;
	private EntryPointType type;
	
	@XmlAttribute(name="address")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	@XmlAttribute(name="type")
	public EntryPointType getType() {
		return type;
	}
	public void setType(EntryPointType type) {
		this.type = type;
	}
}
