package com.digitalbarista.cat.business;

import com.digitalbarista.cat.data.ContactInfoDO;

public class ContactInfo implements BusinessObject<ContactInfoDO>
{
	private Long contactInfoId;
	private String name;
	private String value;

	
	public Long getContactInfoId()
	{
		return contactInfoId;
	}

	public void setContactInfoId(Long contactInfoId)
	{
		this.contactInfoId = contactInfoId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public void copyFrom(ContactInfoDO dataObject)
	{
		this.setContactInfoId(dataObject.getContactInfoId());
		this.setName(dataObject.getName());
		this.setValue(dataObject.getValue());
	}

	@Override
	public void copyTo(ContactInfoDO dataObject)
	{
		// TODO Auto-generated method stub
		
	}

}
