package com.digitalbarista.cat.ejb.session;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.digitalbarista.cat.business.KeyValuePair;
import com.digitalbarista.cat.business.reporting.AnalyticsResponse;
import com.digitalbarista.cat.business.reporting.DashboardData;
import com.digitalbarista.cat.business.reporting.DateData;
import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;
import com.digitalbarista.cat.business.reporting.TagSummary;
import com.digitalbarista.cat.exception.ReportingManagerException;
import javax.ws.rs.*;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;


@Local
@Path("/reporting")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface ReportingManager 
{

  @Path("/outgoingMessages")
  @Wrapped(element="OutgoingMessageSummaries")
  @GET
	List<OutgoingMessageSummary> getOutgoingMessageSummaries() throws ReportingManagerException ;
  
  @Path("/dashboard")
  @GET
	DashboardData getDashboardData(@QueryParam("clientID") List<Long> clientIds) throws ReportingManagerException ;
  
  @Path("/tagSummaries")
  @Wrapped(element="TagSummaries")
  @GET
	List<TagSummary> getTagSummaries(@QueryParam("clientID") List<Long> clientIds) throws ReportingManagerException;
	List<DateData> getContactCreates(List<Long> clientIds, Calendar start, Calendar end) throws ReportingManagerException;
	List<DateData> getMessageSendDates(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException;
	AnalyticsResponse getAnalyticsData(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException;
  
  @Path("/endpointSubscriberCount")
  @Wrapped(element="Counts")
  @GET
	List<KeyValuePair> getEndpointSubscriberCount(@QueryParam("clientID") List<Long> clientIDs);
	Map<String, String> getUserAnalyticsHistory(String facebookUID) throws ReportingManagerException;
}
