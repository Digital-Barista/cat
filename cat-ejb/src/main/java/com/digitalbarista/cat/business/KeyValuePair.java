package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class KeyValuePair {
	private String key;
 	private String value;
	
	public KeyValuePair(){}
	public KeyValuePair(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
 
  @XmlAttribute
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
 
  @XmlAttribute
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
