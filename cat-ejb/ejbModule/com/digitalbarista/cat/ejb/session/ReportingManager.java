package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;


@Local
public interface ReportingManager 
{
	List<OutgoingMessageSummary> getOutgoingMessageSummaries();
}
