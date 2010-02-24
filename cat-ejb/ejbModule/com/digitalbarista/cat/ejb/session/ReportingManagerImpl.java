package com.digitalbarista.cat.ejb.session;

import java.math.BigInteger;
import java.util.ArrayList;
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

import org.hibernate.Session;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.reporting.OutgoingMessageSummary;
import com.digitalbarista.cat.data.EntryPointType;


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
	
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<OutgoingMessageSummary> getOutgoingMessageSummaries()
	{
		List<OutgoingMessageSummary> ret = new ArrayList<OutgoingMessageSummary>();
		
		Set<Long> clientIDs = userManager.extractClientIds(ctx.getCallerPrincipal().getName());
		
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
		
		return ret;
	}
}
