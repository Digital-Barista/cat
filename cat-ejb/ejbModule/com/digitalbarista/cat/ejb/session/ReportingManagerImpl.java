package com.digitalbarista.cat.ejb.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
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

import com.digitalbarista.cat.business.reporting.DashboardCount;
import com.digitalbarista.cat.business.reporting.DashboardData;
import com.digitalbarista.cat.business.reporting.MessageCreditInfo;
import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ClientInfoDO;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.exception.ReportingManagerException;

import edu.emory.mathcs.backport.java.util.Arrays;


/**
 * Session Bean implementation class SubscriptionManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/ReportingManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class ReportingManagerImpl implements ReportingManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
	Logger logger = LogManager.getLogger(ReportingManagerImpl.class);
	
	@PermitAll
	public List<OutgoingMessageSummary> getOutgoingMessageSummaries() throws ReportingManagerException 
	{
		List<OutgoingMessageSummary> ret = new ArrayList<OutgoingMessageSummary>();
		
		try
		{
			Set<Long> clientIDs = userManager.extractClientIds(ctx.getCallerPrincipal().getName());
			
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

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public DashboardData getDashboardData() throws ReportingManagerException 
	{
		DashboardData ret = new DashboardData();
		
		try
		{
			// Get client count
			Set<Long> clientIDs = userManager.extractClientIds(ctx.getCallerPrincipal().getName());
			ret.setClientCount(Integer.toString(clientIDs.size()) );
			
			// Get campaign count
			Criteria crit = session.createCriteria(CampaignDO.class);
			crit.add(Restrictions.in("client.primaryKey", clientIDs));
			crit.add(Restrictions.eq("status", CampaignStatus.Active));
			crit.add(Restrictions.eq("mode", CampaignMode.Normal));
			crit.setProjection(Projections.rowCount());
			ret.setCampaignCount(crit.uniqueResult().toString());
			

			// Get per EntryPointType counts
			ret.setContactCounts(getContactCounts(clientIDs));
			ret.setCouponsRedeemed(getCouponsRedeemed(clientIDs));
			ret.setCouponsSent(getCouponsSent(clientIDs));
			ret.setMessagesReceived(getMessageReceivedCounts(clientIDs));
			ret.setMessagesSent(getMessageSentCounts(clientIDs));

			// Get message credit infos
			ret.setMessageCreditInfos(getMessageCreditInfos(clientIDs));
			
			// Get subscriber count
			crit = session.createCriteria(CampaignSubscriberLinkDO.class);
			crit.createAlias("campaign", "campaign");
			crit.add(Restrictions.in("campaign.client.primaryKey", clientIDs));
			crit.setProjection(Projections.rowCount());
			ret.setSubscriberCount(crit.uniqueResult().toString());
			
		}
		catch(Exception e)
		{
			String error = "Error getting dashboard data";
			logger.error(error, e);
			throw new ReportingManagerException(error, e);
		}
		
		return ret;
	}
	
	private Integer getCouponsSent(Set<Long> clientIds)
	{
		Criteria crit = session.createCriteria(CouponOfferDO.class);
		crit.createAlias("campaign", "campaign");
		crit.add(Restrictions.in("campaign.client.primaryKey", clientIds));
		crit.setProjection(Projections.rowCount());
		
		return (Integer)crit.uniqueResult();
	}
	
	private Integer getCouponsRedeemed(Set<Long> clientIds)
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
	private List<DashboardCount> getMessageSentCounts(Set<Long> clientIds)
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
	private List<DashboardCount> getMessageReceivedCounts(Set<Long> clientIds)
	{
		List<DashboardCount> ret = new ArrayList<DashboardCount>();
			
		// Get outgoing message counts
		String queryString = 
			"select incoming_type, count(*) " +
			"from audit_incoming_message where incoming_audit_id in " +
	
			"(select distinct a.incoming_audit_id " +
			"from audit_incoming_message a, nodes n, connectors con, campaigns cam, client cli " +
	
			"where (a.matched_uid = n.uid or a.matched_uid = con.uid) " +
			"and n.campaign_id = cam.campaign_id " +
			"and con.campaign_id = cam.campaign_id " +
			"and cam.client_id = cli.client_id " +
			"and cli.client_id in (:clientIds)) " +
	
			"group by incoming_type";

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
	private List<DashboardCount> getContactCounts(Set<Long> clientIds)
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
	
	private List<MessageCreditInfo> getMessageCreditInfos(Set<Long> clientIds)
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
}
