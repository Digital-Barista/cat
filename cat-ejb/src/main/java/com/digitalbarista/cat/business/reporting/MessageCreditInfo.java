package com.digitalbarista.cat.business.reporting;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class MessageCreditInfo 
{
	private String clientName;
	private String network;
	private String credits;
	private String usedThisMonth;
	private String usedTotal;
	
  
  @XmlAttribute
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
  
  @XmlAttribute
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
  
  @XmlAttribute
	public String getCredits() {
		return credits;
	}
	public void setCredits(String credits) {
		this.credits = credits;
	}
  
  @XmlAttribute
	public String getUsedThisMonth() {
		return usedThisMonth;
	}
	public void setUsedThisMonth(String usedThisMonth) {
		this.usedThisMonth = usedThisMonth;
	}
  
  @XmlAttribute
	public String getUsedTotal() {
		return usedTotal;
	}
	public void setUsedTotal(String usedTotal) {
		this.usedTotal = usedTotal;
	}
	
}
