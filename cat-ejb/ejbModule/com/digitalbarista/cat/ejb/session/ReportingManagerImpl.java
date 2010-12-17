package com.digitalbarista.cat.ejb.session;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.KeyValuePair;
import com.digitalbarista.cat.business.reporting.AnalyticsData;
import com.digitalbarista.cat.business.reporting.AnalyticsResponse;
import com.digitalbarista.cat.business.reporting.DashboardCount;
import com.digitalbarista.cat.business.reporting.DashboardData;
import com.digitalbarista.cat.business.reporting.DateData;
import com.digitalbarista.cat.business.reporting.MessageCreditInfo;
import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;
import com.digitalbarista.cat.business.reporting.TagSummary;
import com.digitalbarista.cat.data.BlacklistDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ClientInfoDO;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.FacebookAppDO;
import com.digitalbarista.cat.exception.ReportingManagerException;
import com.digitalbarista.cat.util.SecurityUtil;
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;


/**
 * Session Bean implementation class SubscriptionManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/ReportingManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ReportingManagerImpl implements ReportingManager 
{
	public static final String ACCOUNTS_URL = "https://www.google.com/analytics/feeds/accounts/default";
	private static final String ANALYTICS_DATA_URL = "https://www.google.com/analytics/feeds/data";
	

	private static final String DBI_ANALYTICS_USERNAME = "dbigaviewonly@gmail.com";
	private static final String DBI_ANALYTICS_PASSWORD = "Yamahar6";
	private static final String DBI_ANALYTICS_TABLE_ID = "ga:38779239";
	
	private static final String GA_VISITS = "ga:visits";
	private static final String GA_NEW_VISITS = "ga:newVisits";
	private static final String GA_TIME_ON_SITE = "ga:timeOnSite";
	private static final String GA_DATE = "ga:date";
	private static final String GA_PAGE_PATH = "ga:pagePath";
	
	
	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
	Logger logger = LogManager.getLogger(ReportingManagerImpl.class);
	SimpleDateFormat outgoingDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat incomingDateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	@PermitAll
	public List<OutgoingMessageSummary> getOutgoingMessageSummaries() throws ReportingManagerException 
	{
		List<OutgoingMessageSummary> ret = new ArrayList<OutgoingMessageSummary>();
		
		try
		{
			List<Long> clientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, null);
			
			if (clientIDs.size() > 0)
			{
				String queryString = 
					"select cli.client_id, cast(cli.name as char(200)), c.campaign_id, cast(c.name as char(200)), a.msg_type, count(*) as message_count " +
					"from audit_outgoing_message a " +
					"join nodes n on n.uid = a.node_uid " +
					"join campaigns as c on c.campaign_id = n.campaign_id " +
					"join client as cli on cli.client_id = c.client_id " +
					"where cli.client_id in (:clientIds) " +
					"group by cli.name, c.campaign_id, c.name, a.msg_type " +
					"order by c.name ";
				
				Query query = em.createNativeQuery(queryString);
				query.setParameter("clientIds", clientIDs);
				
				for (Object item : query.getResultList())
				{
					Object[] row = (Object[])item;
					OutgoingMessageSummary summary = new OutgoingMessageSummary();
					summary.setClientId((BigInteger)row[0]);
					summary.setClientName((String)row[1]);
					summary.setCampaignId((Integer)row[2]);
					summary.setCampaignName((String)row[3]);
					summary.setMessageType(EntryPointType.getById(new Integer((String)row[4])) );
					summary.setMessageCount((BigInteger)row[5]);
					ret.add(summary);
				}
			}
		}
		catch(Exception e)
		{
			String error = "Error getting outgoing message summary";
			logger.error(error, e);
			throw new ReportingManagerException(error, e);
		}
		return ret;
	}

	@Override
	public DashboardData getDashboardData(List<Long> clientIds) throws ReportingManagerException 
	{
		DashboardData ret = new DashboardData();
		
		try
		{
			// Get client count
			List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIds);
			
			if (allowedClientIDs.size() <= 0)
			{
				ret.setClientCount("0");
				ret.setCampaignCount("0");
				ret.setSubscriberCount("0");
				ret.setCouponsRedeemed(0);
				ret.setCouponsSent(0l);
			}
			else
			{
				ret.setClientCount(Integer.toString(allowedClientIDs.size()));
			
				// Get campaign count
				Criteria crit = session.createCriteria(CampaignDO.class);
				crit.add(Restrictions.in("client.id", allowedClientIDs));
				crit.add(Restrictions.eq("status", CampaignStatus.Active));
				crit.add(Restrictions.eq("mode", CampaignMode.Normal));
				crit.setProjection(Projections.rowCount());
				ret.setCampaignCount(crit.uniqueResult().toString());
				
	
				// Get per EntryPointType counts
				ret.setContactCounts(getContactCounts(allowedClientIDs));
				ret.setCouponsRedeemed(getCouponsRedeemed(allowedClientIDs));
				ret.setCouponsSent(getCouponsSent(allowedClientIDs));
				ret.setMessagesReceived(getMessageReceivedCounts(allowedClientIDs));
				ret.setMessagesSent(getMessageSentCounts(allowedClientIDs));
	
				// Get message credit infos
				ret.setMessageCreditInfos(getMessageCreditInfos(allowedClientIDs));
				
				// Get subscriber count
				crit = session.createCriteria(CampaignSubscriberLinkDO.class);
				crit.add(Restrictions.eq("active", true));
				crit.createAlias("campaign", "campaign");
				crit.add(Restrictions.in("campaign.client.primaryKey", allowedClientIDs));
				crit.setProjection(Projections.rowCount());
				ret.setSubscriberCount(crit.uniqueResult().toString());
			}
			
		}
		catch(Exception e)
		{
			String error = "Error getting dashboard data";
			logger.error(error, e);
			throw new ReportingManagerException(error, e);
		}
		
		return ret;
	}

	public List<TagSummary> getTagSummaries(List<Long> clientIds) throws ReportingManagerException 
	{
		List<TagSummary> ret = new ArrayList<TagSummary>();
		
		try
		{
			// Get allowed client IDs
			List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIds);
			
			if (allowedClientIDs.size() > 0)
			{
				String queryString = 
					"select cli.client_id, cli.name, c.type, t.tag, count(*) " +
					"from contact c " +
					"join client cli on cli.client_id = c.client_id " +
					"join contact_tag_link ctl on c.contact_id = ctl.contact_id " +
					"join contact_tag t on t.contact_tag_id = ctl.contact_tag_id " +
					"where cli.client_id in (:clientIds) " +
					"group by cli.client_id, cli.name, t.tag, c.type " +
					"order by cli.name, c.type, t.tag ";
				
				Query query = em.createNativeQuery(queryString);
				query.setParameter("clientIds", allowedClientIDs);
				
				for (Object item : query.getResultList())
				{
					Object[] row = (Object[])item;
					TagSummary summary = new TagSummary();
					summary.setClientId(((BigInteger)row[0]).longValue());
					summary.setClientName((String)row[1]);
					summary.setEntryPointType(EntryPointType.valueOf((String)row[2]));
					summary.setTag((String)row[3]);
					summary.setUserCount((BigInteger)row[4]);
					ret.add(summary);
				}
			}
		}
		catch(Exception e)
		{
			String error = "Error getting tag summary";
			logger.error(error, e);
			throw new ReportingManagerException(error, e);
		}
		return ret;
	}
	
	public List<DateData> getContactCreates(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException
	{
		List<DateData> ret = new ArrayList<DateData>();
		
		if (start == null)
		{
			throw new ReportingManagerException("Invalid start date: " + start);
		}
		
		try
		{
			// Get allowed client IDs
			List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIDs);
			
			// Default end date to now
			Calendar endDate = end;
			if (endDate == null)
				endDate = Calendar.getInstance();
			
			if (allowedClientIDs.size() > 0)
			{
				String queryString = 
					"select create_date, year(create_date), month(create_date), day(create_date), count(*), " +
					"	(select count(*) from contact where client_id in (:clientIds) and create_date < :start) " +
					"from contact " +
					"where client_id in (:clientIds) and create_date >= :start and create_date <= :end " +
					"group by year(create_date), month(create_date), day(create_date) " +
					"order by year(create_date), month(create_date), day(create_date) "; 
				
				Query query = em.createNativeQuery(queryString);
				query.setParameter("clientIds", allowedClientIDs);
				query.setParameter("start", start);
				query.setParameter("end", endDate);
				
				for (Object item : query.getResultList())
				{
					Object[] row = (Object[])item;
					DateData data = new DateData();
					Calendar createDate = Calendar.getInstance();
					createDate.setTime(new Date(((Timestamp)row[0]).getTime()));
					
					data.setDate(createDate);
					data.setYear((Integer)row[1]);
					data.setMonth((Integer)row[2]);
					data.setDay((Integer)row[3]);
					data.setCount((BigInteger)row[4]);
					data.setTotal((BigInteger)row[5]);
					ret.add(data);
				}
			}
		}
		catch(Exception e)
		{
			String error = "Error getting contact creates";
			logger.error(error, e);
			throw new ReportingManagerException(error, e);
		}
		return ret;
	}
	
	public List<DateData> getMessageSendDates(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException
	{
		List<DateData> ret = new ArrayList<DateData>();
		
		if (start == null)
		{
			throw new ReportingManagerException("Invalid start date: " + start);
		}
		
		try
		{
			// Get allowed client IDs
			List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIDs);
			
			// Default end date to now
			Calendar endDate = end;
			if (endDate == null)
				endDate = Calendar.getInstance();
			
			if (allowedClientIDs.size() > 0)
			{
				String queryString = 
					"select date_sent, year(date_sent), month(date_sent), day(date_sent), count(*), " +
					"  (select count(*) from audit_outgoing_message a " +
					"    join nodes n on n.uid = a.node_uid " +
					"    join campaigns as c on c.campaign_id = n.campaign_id " +
					"    join client as cli on cli.client_id = c.client_id " +
					"    where cli.client_id in (:clientIds)  and date_sent < :start) " +
					"from audit_outgoing_message a " +
					"join nodes n on n.uid = a.node_uid " +
					"join campaigns as c on c.campaign_id = n.campaign_id " +
					"join client as cli on cli.client_id = c.client_id " +
					"where cli.client_id in (:clientIds) and date_sent >= :start and date_sent <= :end " +
					"group by year(date_sent), month(date_sent), day(date_sent) " +
					"order by date_sent ";
				
				Query query = em.createNativeQuery(queryString);
				query.setParameter("clientIds", allowedClientIDs);
				query.setParameter("start", start);
				query.setParameter("end", endDate);
				
				for (Object item : query.getResultList())
				{
					Object[] row = (Object[])item;
					DateData data = new DateData();
					Calendar createDate = Calendar.getInstance();
					createDate.setTime(new Date(((Timestamp)row[0]).getTime()));
					
					data.setDate(createDate);
					data.setYear((Integer)row[1]);
					data.setMonth((Integer)row[2]);
					data.setDay((Integer)row[3]);
					data.setCount((BigInteger)row[4]);
					data.setTotal((BigInteger)row[5]);
					ret.add(data);
				}
			}
		}
		catch(Exception e)
		{
			String error = "Error getting message send dates";
			logger.error(error, e);
			throw new ReportingManagerException(error, e);
		}
		return ret;
	}
	
	public AnalyticsResponse getAnalyticsData(List<Long> clientIDs, Calendar start, Calendar end) throws ReportingManagerException
	{
		Map<Calendar, AnalyticsData> dataMap = new HashMap<Calendar, AnalyticsData>();

		// Find the max count for scaling the chart
		Long maxCount = 0l;
		
		try
		{
			// Get allowed client IDs
			List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIDs);
			
			// Default end date to now
			Calendar endDate = end;
			if (endDate == null)
				endDate = Calendar.getInstance();
			
			
			// Get the facebook URL filter.  If there are no apps to filter
			// by, don't bother doing the query
			String filter = getAnalyticsFacebookFilter(allowedClientIDs);
			
			if (allowedClientIDs.size() > 0 &&
				filter.length() > 0)
			{
				// Authenticate
				AnalyticsService analytics = new AnalyticsService("digitalbarista");
				analytics.setUserCredentials(DBI_ANALYTICS_USERNAME, DBI_ANALYTICS_PASSWORD);
				
				// Do data query
				DataQuery query = new DataQuery(new URL(ANALYTICS_DATA_URL));
				query.setIds(DBI_ANALYTICS_TABLE_ID);
				query.setStartDate(outgoingDateFormatter.format(start.getTime()));
				query.setEndDate(outgoingDateFormatter.format(end.getTime()));
				query.setMetrics(GA_NEW_VISITS + "," + GA_TIME_ON_SITE + "," + GA_VISITS);
				query.setDimensions(GA_DATE + "," + GA_PAGE_PATH);
				query.setMaxResults(10000);
				query.setSort(GA_DATE + "," + GA_PAGE_PATH);
				query.setFilters(filter);
				DataFeed dataFeed = analytics.getFeed(query, DataFeed.class);
				
				for (DataEntry entry : dataFeed.getEntries())
				{
					Long visits = entry.longValueOf(GA_VISITS);
					Long newVisits = entry.longValueOf(GA_NEW_VISITS);
					Long timeOnSite = entry.longValueOf(GA_TIME_ON_SITE);
					String date = entry.stringValueOf(GA_DATE);
					Calendar cal = Calendar.getInstance();
					cal.setTime(incomingDateFormatter.parse(date));
					
					AnalyticsData data = null;
					if (!dataMap.containsKey(cal))
					{
						data = new AnalyticsData();
						dataMap.put(cal, data);
					}
					else
					{
						data = dataMap.get(cal);
					}
					
					data.setDate(cal);
					
					// Set analytics data
					data.setVisits(data.getVisits() + visits);
					data.setNewVisits(data.getNewVisits() + newVisits);
					data.setTimeOnSite(data.getTimeOnSite() + timeOnSite);
					
					if (data.getVisits() > maxCount) maxCount = data.getVisits();
					if (data.getNewVisits() > maxCount) maxCount = data.getNewVisits();
				}
				
				// Need to loop through again to average time on site values
				for (AnalyticsData a : dataMap.values())
				{
					a.setTimeOnSite(a.getTimeOnSite() / a.getVisits());
				}
			}
		}
		catch (AuthenticationException e)
		{
			throw new ReportingManagerException("Could not login to google analytics", e);
		} 
		catch (MalformedURLException e)
		{
			throw new ReportingManagerException("Invalid analytics data url: " + ANALYTICS_DATA_URL, e);
		} 
		catch (IOException e)
		{
			throw new ReportingManagerException("Could not retrieve accounts feed", e);
		} 
		catch (ServiceException e)
		{
			throw new ReportingManagerException("An error occurred calling the accounts feed", e);
		} 
		catch (ParseException e)
		{
			throw new ReportingManagerException("Could not parse feed date", e);
		}
		
		AnalyticsResponse ret = new AnalyticsResponse();
		ret.setDataList(new ArrayList<AnalyticsData>(dataMap.values()));
		ret.setStartDate(start);
		ret.setEndDate(end);
		ret.setMaxCount(maxCount);
		return ret;
	}
	
	/**
	 * Get all the facebook apps for the associated client IDs
	 * and build a filter for the analytics query that will only
	 * count URLs from those apps
	 * 
	 * @param clientIDs
	 * @return
	 */
	private String getAnalyticsFacebookFilter(List<Long> clientIDs)
	{
		String filter = "";
		Criteria crit = session.createCriteria(FacebookAppDO.class);
		crit.add(Restrictions.in("client.primaryKey", clientIDs));
		for (FacebookAppDO app : (List<FacebookAppDO>)crit.list())
		{
			if (filter.length() > 0)
			{
				filter += ",";
			}
			filter += "ga:pagePath=@" + app.getFacebookAppId() + "/";
		}
		return filter;
	}
	
	
	private Long getCouponsSent(List<Long> clientIds)
	{
		Criteria crit = session.createCriteria(CouponOfferDO.class);
		crit.createAlias("campaign", "campaign");
		crit.add(Restrictions.in("campaign.client.primaryKey", clientIds));
		crit.setProjection(Projections.sum("issuedCouponCount"));
		
		return (Long)crit.uniqueResult();
	}
	
	private Integer getCouponsRedeemed(List<Long> clientIds)
	{
		Criteria crit = session.createCriteria(CouponRedemptionDO.class);
		crit.createAlias("couponResponse", "couponResponse");
		crit.createAlias("couponResponse.couponOffer", "couponOffer");
		crit.createAlias("couponOffer.campaign", "campaign");
		crit.add(Restrictions.in("campaign.client.primaryKey", clientIds));
		crit.setProjection(Projections.rowCount());
		
		return (Integer)crit.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	private List<DashboardCount> getMessageSentCounts(List<Long> clientIds)
	{
		List<DashboardCount> ret = new ArrayList<DashboardCount>();
			
		// Get outgoing message counts
		String queryString = "select a.msg_type, count(*)" +
			"from audit_outgoing_message a " +
			"join nodes n on n.uid = a.node_uid " +
			"join campaigns as c on c.campaign_id = n.campaign_id " +
			"join client as cli on cli.client_id = c.client_id " +
			"where cli.client_id in (:clientIds) " +
			"group by a.msg_type";

		Query query = em.createNativeQuery(queryString);
		query.setParameter("clientIds", clientIds);
		List<Object> outgoingResults = (List<Object>)query.getResultList();
		
		// Update outgoing counts
		Set<EntryPointType> unused = new HashSet<EntryPointType>(Arrays.asList(EntryPointType.values()));
		for (Object row : outgoingResults)
		{
			Object[] values = (Object[])row;
			EntryPointType type = EntryPointType.getById(new Integer(values[0].toString()));
			Long count = new Long(values[1].toString());
			ret.add(new DashboardCount(type, count));

			unused.remove(type);
		}
		
		// Add unused types
		for (EntryPointType type : unused)
			ret.add(new DashboardCount(type, new Long(0)));
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private List<DashboardCount> getMessageReceivedCounts(List<Long> clientIds)
	{
		List<DashboardCount> ret = new ArrayList<DashboardCount>();
			
		// Get outgoing message counts
		String queryString = 
			"select a.incoming_type, count(*) " +
			"from audit_incoming_message a "+
			"left join nodes n on a.matched_uid=n.uid " +
			"left join connectors con on a.matched_uid=con.uid " +
			"inner join campaigns cam on n.campaign_id=cam.campaign_id or con.campaign_id=cam.campaign_id " +
			"inner join client cli on cam.client_id=cli.client_id and cli.client_id in (:clientIds) " +
			"and cam.client_id = cli.client_id " +
			"and cli.client_id in (:clientIds) " +
			"group by a.incoming_type";

		Query query = em.createNativeQuery(queryString);
		query.setParameter("clientIds", clientIds);
		List<Object> results = (List<Object>)query.getResultList();
		
		// Update outgoing counts
		Set<EntryPointType> unused = new HashSet<EntryPointType>(Arrays.asList(EntryPointType.values()));
		for (Object row : results)
		{
			Object[] values = (Object[])row;
			EntryPointType type = EntryPointType.valueOf(values[0].toString());
			Long count = new Long(values[1].toString());
			ret.add(new DashboardCount(type, count));

			unused.remove(type);
		}

		// Add unused types
		for (EntryPointType type : unused)
			ret.add(new DashboardCount(type, new Long(0)));
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private List<DashboardCount> getContactCounts(List<Long> clientIds)
	{
		List<DashboardCount> ret = new ArrayList<DashboardCount>();
			
		// Get contact count
		String queryString = "select type, count(*) from contact " +
			"where client_id in (:clientIds) " +
			"group by type";

		Query query = em.createNativeQuery(queryString);
		query.setParameter("clientIds", clientIds);
		List<Object> contactResults = (List<Object>)query.getResultList();
		
		// Update contact counts
		Set<EntryPointType> unused = new HashSet<EntryPointType>(Arrays.asList(EntryPointType.values()));
		for (Object row : contactResults)
		{
			Object[] values = (Object[])row;
			EntryPointType type = EntryPointType.valueOf(values[0].toString());
			Long count = new Long(values[1].toString());
			ret.add(new DashboardCount(type, count));

			unused.remove(type);
		}

		// Add unused types
		for (EntryPointType type : unused)
			ret.add(new DashboardCount(type, new Long(0)));
		
		return ret;
	}
	
	private List<MessageCreditInfo> getMessageCreditInfos(List<Long> clientIds)
	{
		// Get message credit info
		List<MessageCreditInfo> creditInfos = new ArrayList<MessageCreditInfo>();
		Criteria crit = session.createCriteria(ClientDO.class);
		crit.add(Restrictions.in("primaryKey", clientIds));
		List<ClientDO> clients = (List<ClientDO>)crit.list();
		
		// Get message sent counts
		String queryString = 
			"select cli.client_id, a.msg_type, count(*)" +
			"from audit_outgoing_message a " +
			"join nodes n on n.uid = a.node_uid " +
			"join campaigns as c on c.campaign_id = n.campaign_id " +
			"join client as cli on cli.client_id = c.client_id " +
			"where cli.client_id in (:clientIds) ";
		
		String byMonth = " and a.date_sent >= (:dateSent)";
		String groupBy = " group by cli.client_id, a.msg_type ";

		// Get total messages
		Query query = em.createNativeQuery(queryString + groupBy);
		query.setParameter("clientIds", clientIds);
		List<Object> totalResults = (List<Object>)query.getResultList();
		
		// Get messages for this month
		Calendar now = Calendar.getInstance();
		Date dateSent = new Date(now.getTime().getYear(), now.getTime().getMonth(), 1);
		query = em.createNativeQuery(queryString + byMonth + groupBy);
		query.setParameter("clientIds", clientIds);
		query.setParameter("dateSent", dateSent);
		List<Object> monthResults = (List<Object>)query.getResultList();
		
		
		// Build message credit infos
		for (EntryPointType epType : EntryPointType.values())
		{
			for (ClientDO client : clients)
			{
				MessageCreditInfo info = new MessageCreditInfo();
				info.setClientName(client.getName());
				info.setNetwork(epType.getName());
				
				// Find client info for message credits
				ClientInfoDO clientInfo = null;
				for (ClientInfoDO cInfo : client.getClientInfos())
				{
					if (cInfo.getName().equals(ClientInfoDO.KEY_MESSAGE_CREDITS) &&
						cInfo.getEntryType() == epType)
					{
						clientInfo = cInfo;
						break;
					}
				}
				
				// Set credit text
				if (clientInfo == null)
				{
					if (epType.getDefaultMessageCredits() == null)
						info.setCredits("Unlimited");
					else
						info.setCredits("0");
				}
				else if (clientInfo.getValue() == null)
				{
					info.setCredits("Unlimited");
				}
				else
				{
					info.setCredits(clientInfo.getValue());
				}
				
				// Find outgoing total count
				info.setUsedTotal("0");
				for (Object result : totalResults)
				{
					Object[] row = (Object[])result;
					Long clientId = new Long(row[0].toString());
					EntryPointType type = EntryPointType.getById(new Integer(row[1].toString()));
					if (client.getPrimaryKey().equals(clientId) &&
						epType.equals(type))
					{
						info.setUsedTotal(row[2].toString());
						break;
					}
				}
				
				// Find month counts
				info.setUsedThisMonth("0");
				for (Object result : monthResults)
				{
					Object[] row = (Object[])result;
					Long clientId = new Long(row[0].toString());
					EntryPointType type = EntryPointType.getById(new Integer(row[1].toString()));
					if (client.getPrimaryKey().equals(clientId) &&
						epType.equals(type))
					{
						info.setUsedThisMonth(row[2].toString());
						break;
					}
				}
				
				creditInfos.add(info);
			}
		}
		return creditInfos;
	}

	@Override
	public List<KeyValuePair> getEndpointSubscriberCount(List<Long> clientIDs) {
		List<KeyValuePair> ret = new ArrayList<KeyValuePair>();
		ret.add(new KeyValuePair("app_1","150"));
		ret.add(new KeyValuePair("app_2","225"));
		ret.add(new KeyValuePair("app_3","600"));
		return ret;
		
//		clientIDs.retainAll(SecurityUtil.extractClientIds(ctx, session));
//		if(clientIDs==null || clientIDs.size()==0)
//			return new ArrayList<KeyValuePair>();
//		Query q = em.createQuery("select e from EntryPointDO e where e.client.id in (:clientIDs)");
//		q.setParameter("clientIDs", clientIDs);
//		List<EntryPointDO> availableEntryPoints = (List<EntryPointDO>)q.getResultList();
//		
//		Map<EntryPointType,List<String>> entryPoints = new HashMap<EntryPointType,List<String>>();
//		for(EntryPointDO ep : availableEntryPoints)
//		{
//			if(!entryPoints.containsKey(ep.getType()))
//				entryPoints.put(ep.getType(), new ArrayList<String>());
//			entryPoints.get(ep.getType()).add(ep.getValue());
//		}
//		
//		Map<EntryPointType,List<String>> applicableBlacklist = new HashMap<EntryPointType,List<String>>();
//		for(EntryPointType type : entryPoints.keySet())
//		{
//			Criteria crit = session.createCriteria(BlacklistDO.class);
//			crit.add(Restrictions.)
//		}
//		return null;
	}
	
}
