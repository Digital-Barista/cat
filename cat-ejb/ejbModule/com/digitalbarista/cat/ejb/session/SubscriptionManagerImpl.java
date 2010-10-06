package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.Subscriber;
import com.digitalbarista.cat.data.BlacklistDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberBlacklistDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.message.event.CATEvent;

import flex.messaging.io.ArrayCollection;


/**
 * Session Bean implementation class SubscriptionManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/SubscriptionManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SubscriptionManagerImpl implements SubscriptionManager {

	private Logger logger = LogManager.getLogger(SubscriptionManagerImpl.class);
	
	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@EJB(name="ejb/cat/CampaignManager")
	private CampaignManager campaignManager;
	
	@EJB(name="ejb/cat/ClientManager")
	private ClientManager clientManager;
	
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
			else if (type == EntryPointType.Facebook)
			{
				if (sub.getFacebookID() != null &&
					sub.getFacebookID().length() > 0)
					ret.add(sub.getFacebookID());
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
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
		if(!entryNode.getType().equals(NodeType.Entry) &&
			!entryNode.getType().equals(NodeType.OutgoingEntry))
			throw new IllegalArgumentException("NodeDO '"+entryPointUID+"' is not an entry point.");
		
		//Get the raw node.
		NodeDO nodeDO = campaignManager.getSimpleNode(entryPointUID);
		Criteria crit = session.createCriteria(SubscriberDO.class);
		int entryPointIndex=-1;
		for(int loop=0; loop<((EntryNode)entryNode).getEntryData().size(); loop++)
		{
			if(((EntryNode)entryNode).getEntryData().get(loop).getEntryType().equals(subscriptionType))
			{
				entryPointIndex=loop;
				break;
			}
		}
		
		//Double-check blacklist and remove blacklisted addresses
		Criteria blacklistCrit = session.createCriteria(SubscriberBlacklistDO.class);
		blacklistCrit.add(Restrictions.eq("type", subscriptionType));
		blacklistCrit.add(Restrictions.eq("incomingAddress", ((EntryNode)entryNode).getEntryData().get(entryPointIndex).getEntryPoint()));
		blacklistCrit.createAlias("subscriber", "sub");
		if(subscriptionType.equals(EntryPointType.Email))
			blacklistCrit.add(Restrictions.in("sub.email",addresses));
		else if(subscriptionType.equals(EntryPointType.SMS))
			blacklistCrit.add(Restrictions.in("sub.phoneNumber", addresses));
		else if(subscriptionType.equals(EntryPointType.Twitter))
			blacklistCrit.add(Restrictions.in("sub.twitterUsername", addresses));
		else if(subscriptionType.equals(EntryPointType.Facebook))
			blacklistCrit.add(Restrictions.in("sub.facebookID", addresses));
		List<SubscriberBlacklistDO> blacklisted = blacklistCrit.list();
		for(SubscriberBlacklistDO subToRemove : blacklisted)
		{
			if(subscriptionType.equals(EntryPointType.Email))
				addresses.remove(subToRemove.getSubscriber().getEmail());
			else if(subscriptionType.equals(EntryPointType.SMS))
				addresses.remove(subToRemove.getSubscriber().getPhoneNumber());
			else if(subscriptionType.equals(EntryPointType.Twitter))
				addresses.remove(subToRemove.getSubscriber().getTwitterUsername());
			else if(subscriptionType.equals(EntryPointType.Facebook))
				addresses.remove(subToRemove.getSubscriber().getFacebookID());
		}
		
		if(addresses.size()==0)
			return;
		
		//Now that we've removed blacklisted addresses, get all the subscribers that match the list.
		Disjunction dj = Restrictions.disjunction();
		switch(subscriptionType)
		{
			case Email:
				crit.add(Restrictions.in("email", addresses));
				break;
				
			case SMS:
				crit.add(Restrictions.in("phoneNumber", addresses));
				break;
				
			case Twitter:
				dj.add(Restrictions.in("twitterUsername", addresses));
				dj.add(Restrictions.in("twitterID", addresses));
				crit.add(dj);
				break;
				
			case Facebook:
				dj.add(Restrictions.in("facebookID", addresses));
				crit.add(dj);
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
					addresses.remove(sub.getTwitterID());
					break;
					
				case Facebook:
					addresses.remove(sub.getFacebookID());
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
					
				case Facebook:
					subTemp.setFacebookID(address);
					break;
			}
			em.persist(subTemp);
			subscribers.add(subTemp);
		}
		
		//Now that everybody's a subscriber, actually subscribe them to the campaign.
		CampaignSubscriberLinkDO link;
		
		for(SubscriberDO sub : subscribers)
		{
			boolean isSubscribed=false;
			for(CampaignDO subCamp : sub.getSubscriptions().keySet())
			{
				if(subCamp.getUID().equalsIgnoreCase(camp.getUID()))
				{
					isSubscribed=true;
					break;
				}
			}
			//Of course if they're already subscribe to this campaign, skip them.
			if(isSubscribed)
				continue;
			
			//Otherwise actually subscribe them.
			link = new CampaignSubscriberLinkDO();
			link.setCampaign(camp);
			link.setSubscriber(sub);
			link.setLastHitNode(nodeDO);
			link.setLastHitEntryType(subscriptionType);
			link.setLastHitEntryPoint(((EntryNode)entryNode).getEntryData().get(entryPointIndex).getEntryPoint());
			em.persist(link);
			CATEvent nodeCompleted = CATEvent.buildNodeOperationCompletedEvent(nodeDO.getUID(), sub.getPrimaryKey().toString());
			eventManager.queueEvent(nodeCompleted);
		}
	}

	@Override
	public void subscribeContactsToEntryPoint(List<Contact> contacts, String entryPointUID) 
	{

		// Sort contacts by type
		Collections.sort(contacts);
		
		EntryPointType lastType = null;
		Set<String> addresses = new HashSet<String>();
		for (Contact c : contacts)
		{
			if (lastType != null &&
				lastType != c.getType())
			{
				subscribeToEntryPoint(addresses, entryPointUID, lastType);
				addresses = new HashSet<String>();
			}
			
			addresses.add(c.getAddress());
			lastType = c.getType();
		}

		// Subscribe last type
		subscribeToEntryPoint(addresses, entryPointUID, lastType);
	}

	@SuppressWarnings("unchecked")
	@Override
	@PermitAll
	public List<Subscriber> getSubscribedAddresses(String nodeUID) 
	{
		// Load node and campaign
		Node node = campaignManager.getNode(nodeUID);
		CampaignDO camp = campaignManager.getSimpleCampaign(node.getCampaignUID());
		
		//Check permissions.
		if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), camp.getClient().getPrimaryKey()))
			throw new SecurityException("Current user is not allowed to view subscriptions to the specified campaign.");
		
		// Query for subscriber addresses for this node
		List<Subscriber> ret = new ArrayList<Subscriber>();
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.createAlias("subscriptions", "subscriptions");
		crit.createAlias("subscriptions.lastHitNode", "lastHitNode");
		crit.add(Restrictions.eq("lastHitNode.UID", nodeUID));
		
		// Load addresses from subscriber into String list
		for (SubscriberDO subscriber : (List<SubscriberDO>)crit.list())
		{
			Subscriber s = new Subscriber();
			s.copyFrom(subscriber);
			ret.add(s);
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	public void unsubscribeSubscribers(List<Long> subscriberIds, Long campaignId)
	{
		try
		{
			// This is really dumb, but Flex sends the list as Integers
			List<Long> ids = new ArrayList<Long>();
			if (subscriberIds instanceof ArrayCollection)
			{
				ArrayCollection collection = (ArrayCollection)subscriberIds;
				for (Object id : collection)
				{
					Integer intID = (Integer)id;
					ids.add(intID.longValue());
				}
			}
			else
			{
				ids = subscriberIds;
			}
			
			Criteria crit = session.createCriteria(CampaignSubscriberLinkDO.class);
			crit.add(Restrictions.in("subscriber.primaryKey", ids));
			crit.add(Restrictions.eq("campaign.primaryKey", campaignId));
			
			for (CampaignSubscriberLinkDO link : (List<CampaignSubscriberLinkDO>)crit.list())
				em.remove(link);
		}
		catch(Exception e)
		{
			logger.error("Error unsubscribing", e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean isSubscriberBlacklisted(Long subscriberId, String entryPoint, EntryPointType type) {
		Criteria crit = session.createCriteria(SubscriberBlacklistDO.class);
		crit.add(Restrictions.eq("subscriber.id", subscriberId));
		crit.add(Restrictions.eq("incomingAddress", entryPoint));
		crit.add(Restrictions.eq("type", type));
		return crit.uniqueResult()!=null;
	}

	@Override
	public void registerTwitterFollower(String twitterID, String accountName) {
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("twitterID",twitterID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub==null)
		{
			sub = new SubscriberDO();
			sub.setTwitterID(twitterID);
			em.persist(sub);
		} else {
			crit = session.createCriteria(SubscriberBlacklistDO.class);
			crit.add(Restrictions.eq("subscriber.id", sub.getPrimaryKey()));
			crit.add(Restrictions.eq("incomingAddress", accountName));
			crit.add(Restrictions.eq("type", EntryPointType.Twitter));
			SubscriberBlacklistDO bl = (SubscriberBlacklistDO)crit.uniqueResult();
			if(bl!=null)
				em.remove(bl);
		}
		
		EntryPointDefinition epd = clientManager.getEntryPointDefinition(EntryPointType.Twitter,accountName);
		for(Integer clientID : epd.getClientIDs())
		{
			crit = session.createCriteria(ContactDO.class);
			crit.add(Restrictions.eq("type", EntryPointType.Twitter));
			crit.add(Restrictions.eq("alternateId", twitterID));
			crit.add(Restrictions.eq("client.id",new Long(clientID)));
			ContactDO contact = (ContactDO)crit.uniqueResult();
			if(contact==null)
			{
				contact = new ContactDO();
				contact.setType(EntryPointType.Twitter);
				contact.setAlternateId(twitterID);
				contact.setClient(em.find(ClientDO.class, new Long(clientID)));
				contact.setCreateDate(Calendar.getInstance());
				em.persist(contact);
			}
		}
		
		crit = session.createCriteria(CampaignInfoDO.class);
		crit.add(Restrictions.eq("entryType", EntryPointType.Twitter));
		crit.add(Restrictions.eq("entryAddress", accountName));
		crit.add(Restrictions.eq("name", CampaignInfoDO.KEY_AUTO_START_NODE_UID));
		CampaignInfoDO cInfo = (CampaignInfoDO)crit.uniqueResult();
		
		if(cInfo==null)
			return;
		
		HashSet<String> addresses = new HashSet<String>();
		addresses.add(twitterID);
		subscribeToEntryPoint(addresses,cInfo.getValue(),EntryPointType.Twitter);
	}

	@Override
	public void removeTwitterFollower(String twitterID, String accountName) {
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("twitterID",twitterID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub!=null)
		{
			SubscriberBlacklistDO bl = new SubscriberBlacklistDO();
			bl.setIncomingAddress(accountName);
			bl.setType(EntryPointType.Twitter);
			bl.setSubscriber(sub);
			em.persist(sub);
		}
	}
	
	@Override
	public void registerFacebookFollower(String facebookID, String accountName) {
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("facebookID",facebookID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub==null)
		{
			sub = new SubscriberDO();
			sub.setFacebookID(facebookID);
			em.persist(sub);
		} else {
			crit = session.createCriteria(SubscriberBlacklistDO.class);
			crit.add(Restrictions.eq("subscriber.id", sub.getPrimaryKey()));
			crit.add(Restrictions.eq("incomingAddress", accountName));
			crit.add(Restrictions.eq("type", EntryPointType.Facebook));
			SubscriberBlacklistDO bl = (SubscriberBlacklistDO)crit.uniqueResult();
			if(bl!=null)
				em.remove(bl);
		}
		
		EntryPointDefinition epd = clientManager.getEntryPointDefinition(EntryPointType.Facebook,accountName);
		for(Integer clientID : epd.getClientIDs())
		{
			crit = session.createCriteria(ContactDO.class);
			crit.add(Restrictions.eq("type", EntryPointType.Facebook));
			crit.add(Restrictions.eq("alternateId", facebookID));
			crit.add(Restrictions.eq("client.id",new Long(clientID)));
			ContactDO contact = (ContactDO)crit.uniqueResult();
			if(contact==null)
			{
				contact = new ContactDO();
				contact.setType(EntryPointType.Facebook);
				contact.setAlternateId(facebookID);
				contact.setClient(em.find(ClientDO.class, new Long(clientID)));
				contact.setCreateDate(Calendar.getInstance());
				em.persist(contact);
			}
		}
		
		crit = session.createCriteria(CampaignInfoDO.class);
		crit.add(Restrictions.eq("entryType", EntryPointType.Facebook));
		crit.add(Restrictions.eq("entryAddress", accountName));
		crit.add(Restrictions.eq("name", CampaignInfoDO.KEY_AUTO_START_NODE_UID));
		CampaignInfoDO cInfo = (CampaignInfoDO)crit.uniqueResult();
		
		if(cInfo==null)
			return;
		
		HashSet<String> addresses = new HashSet<String>();
		addresses.add(facebookID);
		subscribeToEntryPoint(addresses,cInfo.getValue(),EntryPointType.Facebook);
	}

	@Override
	public void removeFacebookFollower(String facebookID, String accountName) {
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("facebookID",facebookID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub!=null)
		{
			SubscriberBlacklistDO bl = new SubscriberBlacklistDO();
			bl.setIncomingAddress(accountName);
			bl.setType(EntryPointType.Facebook);
			bl.setSubscriber(sub);
			em.persist(sub);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Blacklist a set of contacts addresses.  These don't need to
	 * be existing contacts, only the address and type will be used
	 * add to the blacklist.
	 */
	public void blacklistAddresses(List<Contact> contacts) 
	{
		// List of addresses indexed by type
		Map<EntryPointType, List<String>> contactTypes = new HashMap<EntryPointType, List<String>>();
		
		for (Contact c : contacts)
		{
			if (contactTypes.get(c.getType()) == null)
				contactTypes.put(c.getType(), new ArrayList<String>());
			contactTypes.get(c.getType()).add(c.getAddress());
		}
		
		// Search for each address and type
		for (EntryPointType type : contactTypes.keySet())
		{
			List<String> addresses = contactTypes.get(type);
			
			// Create query
			Criteria crit = session.createCriteria(BlacklistDO.class);
			crit.add(Restrictions.in("address", addresses));
			crit.add(Restrictions.eq("entryPointType", type));
			
			// Filter out addresses already blacklisted
			List<String> newAddresses = new ArrayList<String>(addresses);
			for (BlacklistDO blacklist : (List<BlacklistDO>)crit.list())
			{
				if (newAddresses.contains(blacklist.getAddress()))
						newAddresses.remove(blacklist.getAddress());
			}
			
			// Create new blacklist records
			for (String address : newAddresses)
			{
				BlacklistDO blacklist = new BlacklistDO();
				blacklist.setEntryPointType(type);
				blacklist.setAddress(address);
				em.persist(blacklist);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unBlacklistAddresses(List<Contact> contacts)
	{
		// List of addresses indexed by type
		Map<EntryPointType, List<String>> contactTypes = new HashMap<EntryPointType, List<String>>();
		
		for (Contact c : contacts)
		{
			if (contactTypes.get(c.getType()) == null)
				contactTypes.put(c.getType(), new ArrayList<String>());
			contactTypes.get(c.getType()).add(c.getAddress());
		}
		
		// Search for each address and type
		for (EntryPointType type : contactTypes.keySet())
		{
			List<String> addresses = contactTypes.get(type);
			
			// Create query
			Criteria crit = session.createCriteria(BlacklistDO.class);
			crit.add(Restrictions.in("address", addresses));
			crit.add(Restrictions.eq("entryPointType", type));
			
			// Filter out addresses already blacklisted
			for (BlacklistDO blacklist : (List<BlacklistDO>)crit.list())
			{
				em.remove(blacklist);
			}
		}
	}
}
