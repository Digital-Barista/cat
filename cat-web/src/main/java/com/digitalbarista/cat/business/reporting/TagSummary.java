package com.digitalbarista.cat.business.reporting;

import java.math.BigInteger;

import com.digitalbarista.cat.data.EntryPointType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class TagSummary 
{
	private Long clientId;
	private String clientName;
	private EntryPointType entryPointType;
	private String tag;
	private BigInteger userCount;
	
	public TagSummary()
	{
	}

  @XmlAttribute
	public Long getClientId()
	{
		return clientId;
	}

	public void setClientId(Long clientId)
	{
		this.clientId = clientId;
	}

  @XmlAttribute
	public String getClientName()
	{
		return clientName;
	}

	public void setClientName(String clientName)
	{
		this.clientName = clientName;
	}

  @XmlAttribute
	public EntryPointType getEntryPointType()
	{
		return entryPointType;
	}

	public void setEntryPointType(EntryPointType entryPointType)
	{
		this.entryPointType = entryPointType;
	}

  @XmlAttribute
	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

  @XmlAttribute
	public BigInteger getUserCount()
	{
		return userCount;
	}

	public void setUserCount(BigInteger userCount)
	{
		this.userCount = userCount;
	}
}
