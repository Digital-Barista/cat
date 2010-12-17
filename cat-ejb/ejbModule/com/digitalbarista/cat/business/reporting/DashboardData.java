package com.digitalbarista.cat.business.reporting;

import java.util.List;
import java.util.Map;

import com.digitalbarista.cat.business.KeyValuePair;
import com.digitalbarista.cat.data.EntryPointType;

public class DashboardData 
{
	private String clientCount;
	private String campaignCount;
	private String subscriberCount;
	private Long couponsSent;
	private Integer couponsRedeemed;
	private List<MessageCreditInfo> messageCreditInfos;
	private List<DashboardCount> contactCounts;
	private List<DashboardCount> messagesSent;
	private List<DashboardCount> messagesReceived;
	private List<KeyValuePair> endPointSubscriberCounts;
	
	
	
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

	public List<DashboardCount> getContactCounts() {
		return contactCounts;
	}
	public void setContactCounts(List<DashboardCount> contactCounts) {
		this.contactCounts = contactCounts;
	}
	public List<DashboardCount> getMessagesSent() {
		return messagesSent;
	}
	public void setMessagesSent(List<DashboardCount> messagesSent) {
		this.messagesSent = messagesSent;
	}
	public List<DashboardCount> getMessagesReceived() {
		return messagesReceived;
	}
	public void setMessagesReceived(List<DashboardCount> messagesReceived) {
		this.messagesReceived = messagesReceived;
	}
	public Long getCouponsSent() {
		return couponsSent;
	}
	public void setCouponsSent(Long couponsSent) {
		this.couponsSent = couponsSent;
	}
	public Integer getCouponsRedeemed() {
		return couponsRedeemed;
	}
	public void setCouponsRedeemed(Integer couponsRedeemed) {
		this.couponsRedeemed = couponsRedeemed;
	}
	public List<KeyValuePair> getEndPointSubscriberCounts()
	{
		return endPointSubscriberCounts;
	}
	public void setEndPointSubscriberCounts(
			List<KeyValuePair> endPointSubscriberCounts)
	{
		this.endPointSubscriberCounts = endPointSubscriberCounts;
	}
	
	
}
