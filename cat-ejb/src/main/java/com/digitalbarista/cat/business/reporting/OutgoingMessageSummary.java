package com.digitalbarista.cat.business.reporting;

import java.math.BigInteger;

import com.digitalbarista.cat.data.EntryPointType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class OutgoingMessageSummary 
{
	private BigInteger clientId;
	private String clientName;
	private Integer campaignId;
	private String campaignName;
	private EntryPointType messageType;
	private BigInteger messageCount;
	
	  
  @XmlAttribute
	public BigInteger getClientId() {
		return clientId;
	}
	public void setClientId(BigInteger clientId) {
		this.clientId = clientId;
	}
	  
  @XmlAttribute
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	  
  @XmlAttribute
	public Integer getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(Integer campaignId) {
		this.campaignId = campaignId;
	}
	  
  @XmlAttribute
	public String getCampaignName() {
		return campaignName;
	}
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}
	  
  @XmlAttribute
	public EntryPointType getMessageType() {
		return messageType;
	}
	public void setMessageType(EntryPointType messageType) {
		this.messageType = messageType;
	}
	  
  @XmlAttribute
	public BigInteger getMessageCount() {
		return messageCount;
	}
	public void setMessageCount(BigInteger messageCount) {
		this.messageCount = messageCount;
	}
	
}
