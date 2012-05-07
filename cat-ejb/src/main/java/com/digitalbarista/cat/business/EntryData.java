package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.data.EntryPointType;

@XmlType
public class EntryData
{
	private EntryPointType entryType;
	private String entryPoint;
	private String keyword;
	private Integer maxMessages = 0;
	
	@XmlAttribute
	public EntryPointType getEntryType() {
		return entryType;
	}
	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}
	@XmlAttribute
	public String getEntryPoint() {
		return entryPoint;
	}
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}
	@XmlAttribute
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	@XmlAttribute
	public Integer getMaxMessages()
  {
  	return maxMessages;
  }
	public void setMaxMessages(Integer maxMessages)
  {
  	this.maxMessages = maxMessages;
  }
	
}
