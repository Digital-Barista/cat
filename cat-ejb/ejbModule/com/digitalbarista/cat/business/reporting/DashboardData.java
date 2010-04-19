package com.digitalbarista.cat.business.reporting;

import java.util.List;

public class DashboardData 
{
	private String clientCount;
	private String campaignCount;
	private String contactCount;
	private String subscriberCount;
	private List<MessageCreditInfo> messageCreditInfos;
	
	
	
	public String getClientCount() {
		return clientCount;
	}
	public void setClientCount(String clientCount) {
		this.clientCount = clientCount;
	}
	public String getCampaignCount() {
		return campaignCount;
	}
	public void setCampaignCount(String campaignCount) {
		this.campaignCount = campaignCount;
	}
	public String getContactCount() {
		return contactCount;
	}
	public void setContactCount(String contactCount) {
		this.contactCount = contactCount;
	}
	public String getSubscriberCount() {
		return subscriberCount;
	}
	public void setSubscriberCount(String subscriberCount) {
		this.subscriberCount = subscriberCount;
	}
	public List<MessageCreditInfo> getMessageCreditInfos() {
		return messageCreditInfos;
	}
	public void setMessageCreditInfos(List<MessageCreditInfo> messageCreditInfos) {
		this.messageCreditInfos = messageCreditInfos;
	}
	
	
}
