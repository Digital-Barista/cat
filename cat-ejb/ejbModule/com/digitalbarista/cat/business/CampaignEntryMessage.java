package com.digitalbarista.cat.business;

import java.util.List;

public class CampaignEntryMessage {

	private Node messageNode;
	private boolean active;
private List<EntryData> entryData;
	
	public Node getMessageNode() {
		return messageNode;
	}
	public void setMessageNode(Node messageNode) {
		this.messageNode = messageNode;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public List<EntryData> getEntryData() {
		return entryData;
	}
	public void setEntryData(List<EntryData> entryData) {
		this.entryData = entryData;
	}

}
