package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
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

import com.digitalbarista.cat.business.Address;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.Subscriber;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.BlacklistDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.util.PagedList;
import com.digitalbarista.cat.util.SecurityUtil;

import flex.messaging.io.ArrayCollection;


/**
 * Session Bean implementation class SubscriptionManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/SubscriptionManager")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@RunAs("admin")
public class SubscriptionManagerImpl implements SubscriptionManager {

	private Logger logger = LogManager.getLogger(SubscriptionManagerImpl.class);
	
	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;

	@EJB(name="ejb/cat/ContactManager")
	private ContactManager contactManager;
	
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
				"join l.campaign c where c.client.primaryKey = :clientId " +
				"and s.type=:type");
		q.setParameter("clientId", clientId);
		q.setParameter("type", type);
		
		for(SubscriberDO sub : (List<SubscriberDO>)q.getResultList())
		{
			if (sub.getAddress() != null &&
				sub.getAddress().length() > 0)
				ret.add(sub.getAddress());
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@PermitAll
	public Set<Address> getAllAddressesAsObjects(Long clientId, EntryPointType type)
	{
		Set<Address> ret = new HashSet<Address>();
		
		for(String address : getAllAddresses(clientId,type))
		{
			Address a = new Address();
			a.setType(type);
			a.setAddress(address);
			ret.add(a);
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	@PermitAll
	public void subscribeToEntryPoint(String address, String entryPointUID, EntryPointType subscriptionType) {
		
		// If there are no addresses we're done
		if (address == null)
			return;
		
		//Get the campaign and entry node
		Node entryNode = campaignManager.getSpecificNodeVersion(entryPointUID,campaignManager.getCurrentCampaignVersionForNode(entryPointUID)-1);
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
		Criteria blacklistCrit = session.createCriteria(BlacklistDO.class);
		blacklistCrit.add(Restrictions.eq("entryPointType", subscriptionType));
		blacklistCrit.add(Restrictions.eq("incomingAddress", ((EntryNode)entryNode).getEntryData().get(entryPointIndex).getEntryPoint()));
		List<BlacklistDO> blacklisted = blacklistCrit.list();
		if(blacklisted!=null && blacklisted.size()>0)
      return;
		
		blacklistCrit = session.createCriteria(BlacklistDO.class);
		blacklistCrit.add(Restrictions.eq("entryPointType", subscriptionType));
		blacklistCrit.add(Restrictions.eq("client.id", nodeDO.getCampaign().getClient().getPrimaryKey()));
		blacklisted = blacklistCrit.list();
		if(blacklisted!=null && blacklisted.size()>0)
      return;
		
		//Now that we know this address isn't blacklisted, get any subscriber that matches the list.
		Disjunction dj = Restrictions.disjunction();
		crit.add(Restrictions.eq("type", subscriptionType));
		crit.add(Restrictions.eq("address", address));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();

    //If we get a result, this user is already exists, and we're done.  Otherwise, create them.
    if(sub==null)
    {
      sub = new SubscriberDO();
      sub.setType(subscriptionType);
      sub.setAddress(address);
      em.persist(sub);
    }

    //Now our subscriber exists, actually subscribe them to the campaign.
		CampaignSubscriberLinkDO link;
		
    boolean isSubscribed=false;
    for(CampaignDO subCamp : sub.getSubscriptions().keySet())
    {
      if(subCamp.getUID().equalsIgnoreCase(camp.getUID()))
      {
        isSubscribed=true;
        break;
      }
    }
    
    //Of course if they're already subscribe to this campaign, we're done.
    if(isSubscribed)
      return;
      
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

	@SuppressWarnings("unchecked")
	@Override
	@PermitAll
	public void subscribeToEntryPoint(Set<String> addresses, String entryPointUID, EntryPointType subscriptionType) {
		
		// If there are no addresses we're done
		if (addresses == null ||
			addresses.size() <= 0)
			return;
    
    //All the previous code goes away.  Now, we queue them up, and let the queue do the work.
    for(String address : addresses)
    {
      eventManager.queueEvent(CATEvent.buildSubscriptionRequestedEvent(address, subscriptionType, entryPointUID));
    }
	}

	@SuppressWarnings("unchecked")
	@Override
	@PermitAll
	public void subscribeToEntryPointWithObjects(Set<Address> addresses, String entryPointUID) 
	{
		Map<EntryPointType,Set<String>> typeMap = new HashMap<EntryPointType,Set<String>>();
		
		for(Address address : addresses)
		{
			if(!typeMap.containsKey(address.getType()))
			{
				typeMap.put(address.getType(), new HashSet<String>());
			}
			typeMap.get(address.getType()).add(address.getAddress());
		}
		
		for(EntryPointType type : typeMap.keySet())
			subscribeToEntryPoint(typeMap.get(type),entryPointUID,type);
	}

	@Override
	public void subscribeContactsToEntryPoint(List<Contact> contacts, String entryPointUID) 
	{
		// Sort contacts by type
		Collections.sort(contacts);

		// Get entry point types that are valid for this entry point
		
		EntryNode entryNode = (EntryNode)campaignManager.getSpecificNodeVersion(entryPointUID,campaignManager.getCurrentCampaignVersionForNode(entryPointUID)-1);
		List<EntryPointType> validEntryPointTypes = new ArrayList<EntryPointType>();
		for(int loop = 0; loop < entryNode.getEntryData().size(); loop++)
		{
			validEntryPointTypes.add(entryNode.getEntryData().get(loop).getEntryType());
		}
		
		EntryPointType lastType = null;
		Set<String> addresses = new HashSet<String>();
		for (Contact c : contacts)
		{
			// Ignore contacts that don't match a valid entry point type
			if (validEntryPointTypes.contains(c.getType()) )
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
		}

		// Subscribe last type
		subscribeToEntryPoint(addresses, entryPointUID, lastType);
	}


	public void subscribeContactFilterToEntryPoint(ContactSearchCriteria searchCriteria, String entryPointUID)
	{
		PagedList<Contact> contacts = contactManager.getContacts(searchCriteria, null);
		searchCriteria.setClientIds(SecurityUtil.getAllowedClientIDs(ctx, session, searchCriteria.getClientIds()));
		subscribeContactsToEntryPoint(contacts.getResults(), entryPointUID);
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
			Contact c = contactManager.getContactForSubscription(subscriber, camp);
			if(c==null)
			{
				LogManager.getLogger(getClass()).warn("multiple contacts found for subscription! subID="+subscriber.getPrimaryKey()+", campaignID="+camp.getPrimaryKey());
			} else {
				s.setAddress(c.getUID());
			}
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
	public boolean isSubscriberBlacklisted(String address, EntryPointType type, String incomingAddress, Long clientID)
	{
		Criteria crit = session.createCriteria(BlacklistDO.class);
		crit.add(Restrictions.eq("address", address));
		crit.add(Restrictions.eq("entryPointType", type));
		List<BlacklistDO> entries = crit.list();
		for(BlacklistDO entry : entries)
		{
			if(entry.getIncomingAddress()==null && entry.getClient()==null)
				return true;
			if(incomingAddress.equals(entry.getIncomingAddress()))
				return true;
			if(entry.getClient()!=null && clientID.equals(entry.getClient().getPrimaryKey()))
				return true;
		}
		return false;
	}

	@Override
	public void registerTwitterFollower(String twitterID, String accountName) {
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("type",EntryPointType.Twitter));
		crit.add(Restrictions.eq("address",twitterID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub==null)
		{
			sub = new SubscriberDO();
			sub.setType(EntryPointType.Twitter);
			sub.setAddress(twitterID);
			em.persist(sub);
		} else {
			unBlacklistAddressForEntryPoint(twitterID, EntryPointType.Twitter, accountName);
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
		blacklistAddressForEntryPoint(twitterID, EntryPointType.Twitter, accountName);
	}
	
	@Override
	public void registerFacebookFollower(String facebookID, String accountName) {
		Criteria crit = session.createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("facebookID",facebookID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub==null)
		{
			sub = new SubscriberDO();
			sub.setType(EntryPointType.Facebook);
			sub.setAddress(facebookID);
			em.persist(sub);
		} else {
			unBlacklistAddressForEntryPoint(facebookID, EntryPointType.Facebook, accountName);
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
		blacklistAddressForEntryPoint(facebookID, EntryPointType.Facebook, accountName);
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Blacklist a set of contacts addresses.  These don't need to
	 * be existing contacts, only the address and type will be used
	 * add to the blacklist.
	 */
	public void blacklistContacts(List<Contact> contacts) 
	{
		for (Contact c : contacts)
		{
			if(c.getClientId()==null && !ctx.isCallerInRole("admin"))
				throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+c.getAddress()+", type:"+c.getType()+")");

			// Create query
			Criteria crit = session.createCriteria(BlacklistDO.class);
			crit.add(Restrictions.eq("address", c.getAddress()));
			crit.add(Restrictions.eq("entryPointType", c.getType()));
			crit.add(Restrictions.eq("client.id",c.getClientId()));
			
			List<BlacklistDO> blacklistEntries = (List<BlacklistDO>)crit.list();
			if(blacklistEntries == null || blacklistEntries.size()==0)
			{
				BlacklistDO blacklist = new BlacklistDO();
				blacklist.setEntryPointType(c.getType());
				blacklist.setAddress(c.getAddress());
				blacklist.setClient(em.find(ClientDO.class, c.getClientId()));
				em.persist(blacklist);

				for(CampaignSubscriberLinkDO link : findSubscriptionsByClient(c.getAddress(),c.getType(),c.getClientId()))
				{
					link.setActive(false);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unBlacklistContacts(List<Contact> contacts)
	{
		for (Contact c : contacts)
		{
			if(c.getClientId()==null && !ctx.isCallerInRole("admin"))
				throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+c.getAddress()+", type:"+c.getType()+")");
			Criteria crit = session.createCriteria(BlacklistDO.class);
			crit.add(Restrictions.eq("address", c.getAddress()));
			crit.add(Restrictions.eq("entryPointType", c.getType()));
		
			// Filter out addresses already blacklisted
			for (BlacklistDO blacklist : (List<BlacklistDO>)crit.list())
			{
				em.remove(blacklist);
			}
			
			for(CampaignSubscriberLinkDO link : findSubscriptionsByClient(c.getAddress(),c.getType(),c.getClientId()))
			{
				link.setActive(true);
				eventManager.queueEvent(CATEvent.buildNodeOperationCompletedEvent(link.getLastHitNode().getUID(), ""+link.getSubscriber().getPrimaryKey()));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<CampaignSubscriberLinkDO> findSubscriptionsByClient(String address, EntryPointType type, Long clientId)
	{
		Criteria crit = session.createCriteria(CampaignSubscriberLinkDO.class);
		crit.createAlias("subscriber","sub");
		crit.createAlias("campaign","camp");
		crit.add(Restrictions.eq("sub.address",address));
		crit.add(Restrictions.eq("sub.type",type));
		crit.add(Restrictions.eq("camp.client.id",clientId));
		
		return (List<CampaignSubscriberLinkDO>)crit.list();
	}

	@SuppressWarnings("unchecked")
	private List<CampaignSubscriberLinkDO> findSubscriptionsByEntryPoint(String address, EntryPointType type, String incomingAddress)
	{
		String query = "select distinct ep.campaign " +
		"from CampaignEntryPointDO ep " +
		"where ep.entryPoint=:entryPoint " +
		"and ep.type=:type " +
		"and ep.published=:published";
		Query q = em.createQuery(query);
		q.setParameter("entryPoint", incomingAddress);
		q.setParameter("type", type);
		q.setParameter("published", true);
		
		List<CampaignDO> campaigns = (List<CampaignDO>)q.getResultList();
		
		if(campaigns==null || campaigns.size()==0)
			return new ArrayList<CampaignSubscriberLinkDO>();
		
		Criteria crit = session.createCriteria(CampaignSubscriberLinkDO.class);
		crit.createAlias("subscriber","sub");
		crit.add(Restrictions.eq("sub.address",address));
		crit.add(Restrictions.eq("sub.type", type));
		crit.add(Restrictions.in("campaign", campaigns));
		
		return (List<CampaignSubscriberLinkDO>)crit.list();
	}

	@Override
	public void blacklistAddressForEntryPoint(String address,
			EntryPointType type, String incomingAddress) {
		
		if(incomingAddress==null && !ctx.isCallerInRole("admin"))
			throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+address+", type:"+type+")");
		
		Criteria crit = session.createCriteria(BlacklistDO.class);
		crit.add(Restrictions.eq("address", address));
		crit.add(Restrictions.eq("entryPointType", type));
		crit.add(Restrictions.eq("incomingAddress", incomingAddress));

		List<BlacklistDO> blacklistEntries = (List<BlacklistDO>)crit.list();
		if(blacklistEntries == null || blacklistEntries.size()==0)
		{
			BlacklistDO blacklist = new BlacklistDO();
			blacklist.setEntryPointType(type);
			blacklist.setAddress(address);
			blacklist.setIncomingAddress(incomingAddress);
			em.persist(blacklist);

			for(CampaignSubscriberLinkDO link : findSubscriptionsByEntryPoint(address,type,incomingAddress))
			{
				link.setActive(false);
			}
		}
		
	}

	@Override
	public void unBlacklistAddressForEntryPoint(String address,
			EntryPointType type, String incomingAddress) {

		if(incomingAddress==null && !ctx.isCallerInRole("admin"))
			throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+address+", type:"+type+")");
		
		Criteria crit = session.createCriteria(BlacklistDO.class);
		crit.add(Restrictions.eq("address", address));
		crit.add(Restrictions.eq("entryPointType", type));

		List<BlacklistDO> bList = (List<BlacklistDO>)crit.list();
		Set<Long> clientIDs = new HashSet<Long>();
		// Filter out addresses already blacklisted
		for (BlacklistDO blacklist : bList)
		{
			if(blacklist.getIncomingAddress().equals(incomingAddress))
				em.remove(blacklist);
			if(blacklist.getClient()!=null)
				clientIDs.add(blacklist.getClient().getPrimaryKey());
		}
		
		for(CampaignSubscriberLinkDO link : findSubscriptionsByEntryPoint(address, type, incomingAddress))
		{
			//Don't reactivate if the CLIENT is still blacklisted.
			if(!clientIDs.contains(link.getCampaign().getClient().getPrimaryKey()))
				link.setActive(true);
		}
	}
	
	
}
