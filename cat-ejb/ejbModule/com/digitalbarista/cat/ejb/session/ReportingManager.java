package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.reporting.DashboardData;
import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;
import com.digitalbarista.cat.exception.ReportingManagerException;


@Local
public interface ReportingManager 
{
	List<OutgoingMessageSummary> getOutgoingMessageSummaries() throws ReportingManagerException ;
	DashboardData getDashboardData() throws ReportingManagerException ;
}
