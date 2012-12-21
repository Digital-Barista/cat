package com.digitalbarista.cat.business;

import java.util.List;

import com.digitalbarista.cat.data.EntryPointType;

public class CampaignMessagePart 
{
	private EntryPointType entryType;
	private List<String> messages;
	
	public CampaignMessagePart()
	{
	}
	
	public EntryPointType getEntryType() {
		return entryType;
	}
	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	
}
