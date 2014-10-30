package com.digitalbarista.cat.business.reporting;

import java.util.List;

import com.digitalbarista.cat.business.KeyValuePair;
import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlType
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
	
	
	
  @XmlAttribute
	public String getClientCount() {
		return clientCount;
	}
	public void setClientCount(String clientCount) {
		this.clientCount = clientCount;
	}

  @XmlAttribute
	public String getCampaignCount() {
		return campaignCount;
	}
	public void setCampaignCount(String campaignCount) {
		this.campaignCount = campaignCount;
	}
	

  @XmlAttribute
	public String getSubscriberCount() {
		return subscriberCount;
	}
	public void setSubscriberCount(String subscriberCount) {
		this.subscriberCount = subscriberCount;
	}
  
  @XmlElementWrapper(name="MessageCreditInfoList")
  @XmlElementRef
	public List<MessageCreditInfo> getMessageCreditInfos() {
		return messageCreditInfos;
	}
	public void setMessageCreditInfos(List<MessageCreditInfo> messageCreditInfos) {
		this.messageCreditInfos = messageCreditInfos;
	}

  @XmlElementWrapper(name="ContactCounts")
  @XmlElementRef
	public List<DashboardCount> getContactCounts() {
		return contactCounts;
	}
	public void setContactCounts(List<DashboardCount> contactCounts) {
		this.contactCounts = contactCounts;
	}
  
  @XmlElementWrapper(name="MessagesSent")
  @XmlElementRef
	public List<DashboardCount> getMessagesSent() {
		return messagesSent;
	}
	public void setMessagesSent(List<DashboardCount> messagesSent) {
		this.messagesSent = messagesSent;
	}
  
  @XmlElementWrapper(name="MessagesReceived")
  @XmlElementRef
	public List<DashboardCount> getMessagesReceived() {
		return messagesReceived;
	}
	public void setMessagesReceived(List<DashboardCount> messagesReceived) {
		this.messagesReceived = messagesReceived;
	}

  @XmlAttribute
	public Long getCouponsSent() {
		return couponsSent;
	}
	public void setCouponsSent(Long couponsSent) {
		this.couponsSent = couponsSent;
	}

  @XmlAttribute
	public Integer getCouponsRedeemed() {
		return couponsRedeemed;
	}
	public void setCouponsRedeemed(Integer couponsRedeemed) {
		this.couponsRedeemed = couponsRedeemed;
	}

  @XmlElementWrapper(name="EndpointSubscriberCounts")
  @XmlElementRef
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
