package com.digitalbarista.cat.business.reporting;

import com.digitalbarista.cat.data.EntryPointType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class DashboardCount 
{
  private EntryPointType entryPointType;
	private Long count;
	
	public DashboardCount()
	{
	}
	public DashboardCount(EntryPointType type, Long count)
	{
		this.entryPointType = type;
		this.count = count;
	}
	
	
  
  @XmlAttribute
	public EntryPointType getEntryPointType() {
		return entryPointType;
	}
	public void setEntryPointType(EntryPointType entryPointType) {
		this.entryPointType = entryPointType;
	}
  
  @XmlAttribute
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	
	
}
