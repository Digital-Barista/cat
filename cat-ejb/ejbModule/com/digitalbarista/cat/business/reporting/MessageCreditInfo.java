package com.digitalbarista.cat.business.reporting;

public class MessageCreditInfo 
{
	private String clientName;
	private String network;
	private String credits;
	private String usedThisMonth;
	private String usedTotal;
	
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
	public String getCredits() {
		return credits;
	}
	public void setCredits(String credits) {
		this.credits = credits;
	}
	public String getUsedThisMonth() {
		return usedThisMonth;
	}
	public void setUsedThisMonth(String usedThisMonth) {
		this.usedThisMonth = usedThisMonth;
	}
	public String getUsedTotal() {
		return usedTotal;
	}
	public void setUsedTotal(String usedTotal) {
		this.usedTotal = usedTotal;
	}
	
}
