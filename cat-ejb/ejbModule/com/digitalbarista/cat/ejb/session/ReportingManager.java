package com.digitalbarista.cat.ejb.session;

import java.util.Calendar;
import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.reporting.AnalyticsResponse;
import com.digitalbarista.cat.business.reporting.DashboardData;
import com.digitalbarista.cat.business.reporting.DateData;
import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;
import com.digitalbarista.cat.business.reporting.TagSummary;
import com.digitalbarista.cat.exception.ReportingManagerException;


@Local
public interface ReportingManager 
{
	List<OutgoingMessageSummary> getOutgoingMessageSummaries() throws ReportingManagerException ;
	DashboardData getDashboardData(List<Long> clientIds) throws ReportingManagerException ;
	List<TagSummary> getTagSummaries(List<Long> clientIds) throws ReportingManagerException;
	List<DateData> getContactCreates(List<Long> clientIds, Calendar start, Calendar end) throws ReportingManagerException;
	List<DateData> getMessageSendDates(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException;
	AnalyticsResponse getAnalyticsData(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException;
}
