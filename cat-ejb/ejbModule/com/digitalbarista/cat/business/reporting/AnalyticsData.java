package com.digitalbarista.cat.business.reporting;


public class AnalyticsData extends DateData
{
	private Long visits = 0l;
	private Long newVisits = 0l;
	private Long timeOnSite = 0l;
	
	public Long getVisits()
	{
		return visits;
	}
	public void setVisits(Long visits)
	{
		this.visits = visits;
	}
	public Long getNewVisits()
	{
		return newVisits;
	}
	public void setNewVisits(Long newVisits)
	{
		this.newVisits = newVisits;
	}
	public Long getTimeOnSite()
	{
		return timeOnSite;
	}
	public void setTimeOnSite(Long timeOnSite)
	{
		this.timeOnSite = timeOnSite;
	}
		
}
