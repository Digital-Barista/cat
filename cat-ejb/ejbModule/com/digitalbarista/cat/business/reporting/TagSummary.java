package com.digitalbarista.cat.business.reporting;

import java.math.BigInteger;

import com.digitalbarista.cat.data.EntryPointType;

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

	public Long getClientId()
	{
		return clientId;
	}

	public void setClientId(Long clientId)
	{
		this.clientId = clientId;
	}

	public String getClientName()
	{
		return clientName;
	}

	public void setClientName(String clientName)
	{
		this.clientName = clientName;
	}

	public EntryPointType getEntryPointType()
	{
		return entryPointType;
	}

	public void setEntryPointType(EntryPointType entryPointType)
	{
		this.entryPointType = entryPointType;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public BigInteger getUserCount()
	{
		return userCount;
	}

	public void setUserCount(BigInteger userCount)
	{
		this.userCount = userCount;
	}
}
