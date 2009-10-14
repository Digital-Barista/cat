package com.digitalbarista.cat.ejb.session;

import java.util.HashSet;
import java.util.List;
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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberBlacklistDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.message.event.CATEvent;

/**
 * Session Bean implementation class SubscriptionManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/SubscriptionManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class SubscriptionManagerImpl implements SubscriptionManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@EJB(name="ejb/cat/CampaignManager")
	private CampaignManager campaignManager;
	
	@EJB(name="ejb/cat/EventManager")
	private EventManager eventManager;
	
	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
	@SuppressWarnings("unchecked")
	@PermitAll
	public Set<String> getAllAddresses(Long clientId, EntryPointType type)
	{
		if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), clientId))
			throw new SecurityException("Current user is not allowed to view addresses for the specified client.");
		
		Set<String> ret = new HashSet<String>();
		Query q = em.createQuery("select s from CampaignSubscriberLinkDO l " +
				"join l.subscriber s " +
				"join l.campaign c where c.client.primaryKey = :clientId");
		q.setParameter("clientId", clientId);
		
		for(SubscriberDO sub : (List<SubscriberDO>)q.getResultList())
		{
			if (type == EntryPointType.Email)
			{
				if (sub.getEmail() != null &&
					sub.getEmail().length() > 0)
					ret.add(sub.getEmail());
			}
			else if (type == EntryPointType.SMS)
			{
				if (sub.getPhoneNumber() != null &&
					sub.getPhoneNumber().length() > 0)
					ret.add(sub.getPhoneNumber());
			}
			else if (type == EntryPointType.Twitter)
			{
				if (sub.getTwitterUsername() != null &&
					sub.getTwitterUsername().length() > 0)
					ret.add(sub.getTwitterUsername());
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PermitAll
	public void subscribeToEntryPoint(Set<String> addresses, String entryPointUID, EntryPointType subscriptionType) {
		//Get the campaign and entry node
		Node entryNode = campaignManager.getNode(entryPointUID);
		CampaignDO camp = campaignManager.getSimpleCampaign(entryNode.getCampaignUID());
		
		//Check permissions.
		if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), camp.getClient().getPrimaryKey()))
			throw new SecurityException("Current user is not allowed to add subscriptions to the specified campaign.");
		
		//Nullcheck the entry node.
		if(entryNode==null)
			throw new IllegalArgumentException("Could not find node with UID='"+entryPointUID+"'");
		
		//And make sure this IS an entry node!
		if(!entryNode.getType().equals(NodeType.Entry))
			throw new IllegalArgumentException("NodeDO '"+entryPointUID+"' is not an entry point.");
		
		//Get the raw node.
		NodeDO nodeDO = campaignManager.getSimpleNode(entryPointUID);
		Criteria crit = session.createCriteria(SubscriberDO.class);
		int entryPointIndex=-1;
		for(int loop=0; loop<((EntryNode)entryNode).getEntryData().length; loop++)
		{
			if(((EntryNode)entryNode).getEntryData()[loop].getEntryType().equals(subscriptionType))
			{
				entryPointIndex=loop;
				break;
			}
		}
		
		//Double-check blacklist and remove blacklisted addresses
		Criteria blacklistCrit = session.createCriteria(SubscriberBlacklistDO.class);
		blacklistCrit.add(Restrictions.eq("type", subscriptionType));
		blacklistCrit.add(Restrictions.eq("incomingAddress", ((EntryNode)entryNode).getEntryData()[entryPointIndex].getEntryPoint()));
		blacklistCrit.createAlias("subscriber", "sub");
		if(subscriptionType.equals(EntryPointType.Email))
			blacklistCrit.add(Restrictions.in("sub.email",addresses));
		else if(subscriptionType.equals(EntryPointType.SMS))
			blacklistCrit.add(Restrictions.in("sub.phoneNumber", addresses));
		else if(subscriptionType.equals(EntryPointType.Twitter))
			blacklistCrit.add(Restrictions.in("sub.twitterUsername", addresses));
		List<SubscriberBlacklistDO> blacklisted = blacklistCrit.list();
		for(SubscriberBlacklistDO subToRemove : blacklisted)
		{
			if(subscriptionType.equals(EntryPointType.Email))
				addresses.remove(subToRemove.getSubscriber().getEmail());
			else if(subscriptionType.equals(EntryPointType.SMS))
				addresses.remove(subToRemove.getSubscriber().getPhoneNumber());
			else if(subscriptionType.equals(EntryPointType.Twitter))
				addresses.remove(subToRemove.getSubscriber().getTwitterUsername());
		}
		
		if(addresses.size()==0)
			return;
		
		//Now that we've removed blacklisted addresses, get all the subscribers that match the list.
		switch(subscriptionType)
		{
			case Email:
				crit.add(Restrictions.in("email", addresses));
				break;
				
			case SMS:
				crit.add(Restrictions.in("phoneNumber", addresses));
				break;
				
			case Twitter:
				crit.add(Restrictions.in("twitterUsername", addresses));
				break;
		}
		
		Set<SubscriberDO> subscribers = new HashSet<SubscriberDO>(crit.list());
		
		//So . . . this looks odd, but . . . we have CURRENT subscribers . . . 
		//  now we need to remove all the subscribers in the system from the list
		//  of people we want to subscribe, so we know what ones need to be created
		//  from scratch.
		for(SubscriberDO sub : subscribers)
		{
			switch(subscriptionType)
			{
				case Email:
					addresses.remove(sub.getEmail());
					break;
					
				case SMS:
					addresses.remove(sub.getPhoneNumber());
					break;
					
				case Twitter:
					addresses.remove(sub.getTwitterUsername());
					break;
			}
		}
		
		//And of course anything left is a NEW subscriber that needs to be created.
		SubscriberDO subTemp;
		for(String address : addresses)
		{
			subTemp = new SubscriberDO();
			switch(subscriptionType)
			{
				case Email:
					subTemp.setEmail(address);
					break;
					
				case SMS:
					subTemp.setPhoneNumber(address);
					break;
					
				case Twitter:
					subTemp.setTwitterUsername(address);
					break;
			}
			em.persist(subTemp);
			subscribers.add(subTemp);
		}
		
		//Now that everybody's a subscriber, actually subscribe them to the campaign.
		CampaignSubscriberLinkDO link;
		
		for(SubscriberDO sub : subscribers)
		{
			//Of course if they're already subscribe to this campaign, skip them.
			if(sub.getSubscriptions().get(camp)!=null)
				continue;
			
			//Otherwise actually subscribe them.
			link = new CampaignSubscriberLinkDO();
			link.setCampaign(camp);
			link.setSubscriber(sub);
			link.setLastHitNode(nodeDO);
			link.setLastHitEntryType(subscriptionType);
			link.setLastHitEntryPoint(((EntryNode)entryNode).getEntryData()[entryPointIndex].getEntryPoint());
			em.persist(link);
			CATEvent nodeCompleted = CATEvent.buildNodeOperationCompletedEvent(nodeDO.getUID(), sub.getPrimaryKey().toString());
			eventManager.queueEvent(nodeCompleted);
		}
	}

}
