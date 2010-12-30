package com.digitalbarista.cat.business.reporting;

import java.util.Calendar;
import java.util.List;

public class AnalyticsResponse
{
	private Calendar startDate;
	private Calendar endDate;
	private Long maxCount;
	List<AnalyticsData> dataList;
	
	public Calendar getStartDate()
	{
		return startDate;
	}
	public void setStartDate(Calendar startDate)
	{
		this.startDate = startDate;
	}
	public Calendar getEndDate()
	{
		return endDate;
	}
	public void setEndDate(Calendar endDate)
	{
		this.endDate = endDate;
	}
	public Long getMaxCount()
	{
		return maxCount;
	}
	public void setMaxCount(Long maxCount)
	{
		this.maxCount = maxCount;
	}
	public List<AnalyticsData> getDataList()
	{
		return dataList;
	}
	public void setDataList(List<AnalyticsData> dataList)
	{
		this.dataList = dataList;
	}
	
	
}
