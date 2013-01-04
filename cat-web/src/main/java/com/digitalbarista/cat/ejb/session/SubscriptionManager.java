package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RunAs;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

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
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Session Bean implementation class SubscriptionManagerImpl
 */
@Controller
@RequestMapping(value={"/rest/subscriptions","/rs/subscriptions"})
@Transactional(propagation=Propagation.REQUIRED)
@RunAs("admin")
public class SubscriptionManager {

	private Logger logger = LogManager.getLogger(SubscriptionManager.class);
	
  @Autowired
  private ApplicationContext ctx;
  
  @Autowired
  private SessionFactory sf;
  
  @Autowired
	private ContactManager contactManager;
  
  @Autowired
	private ClientManager clientManager;
	
  @Autowired
  private EventManager eventManager;
	
  @Autowired
  private UserManager userManager;
	
  @Autowired
  private SecurityUtil securityUtil;
  
	@SuppressWarnings("unchecked")
	public Set<String> getAllAddresses(Long clientId, EntryPointType type)
	{
		if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), clientId))
			throw new SecurityException("Current user is not allowed to view addresses for the specified client.");
		
		Set<String> ret = new HashSet<String>();
		Query q = sf.getCurrentSession().createQuery("select s from CampaignSubscriberLinkDO l " +
				"join l.subscriber s " +
				"join l.campaign c where c.client.primaryKey = :clientId " +
				"and s.type=:type");
		q.setParameter("clientId", clientId);
		q.setParameter("type", type);
		
		for(SubscriberDO sub : (List<SubscriberDO>)q.list())
		{
			if (sub.getAddress() != null &&
				sub.getAddress().length() > 0)
				ret.add(sub.getAddress());
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
  @RequestMapping(method=RequestMethod.GET,value="/{type}")
	public Set<Address> getAllAddressesAsObjects(@RequestParam("clientid") Long clientId, @PathVariable("type") EntryPointType type)
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
	public void subscribeToEntryPoint(String address, String entryPointUID, EntryPointType subscriptionType) {
		
    CampaignManager campaignManager = ctx.getBean(CampaignManager.class);
    
		// If there are no addresses we're done
		if (address == null)
			return;
		
		//Get the campaign and entry node
		Node entryNode = campaignManager.getSpecificNodeVersion(entryPointUID,campaignManager.getCurrentCampaignVersionForNode(entryPointUID)-1);
		CampaignDO camp = campaignManager.getSimpleCampaign(entryNode.getCampaignUID());
		
		//Check permissions.
		if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), camp.getClient().getPrimaryKey()))
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
		Criteria crit = sf.getCurrentSession().createCriteria(SubscriberDO.class);
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
		Criteria blacklistCrit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
		blacklistCrit.add(Restrictions.eq("entryPointType", subscriptionType));
		blacklistCrit.add(Restrictions.eq("incomingAddress", ((EntryNode)entryNode).getEntryData().get(entryPointIndex).getEntryPoint()));
		List<BlacklistDO> blacklisted = blacklistCrit.list();
		if(blacklisted!=null && blacklisted.size()>0)
      return;
		
		blacklistCrit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
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
      sf.getCurrentSession().persist(sub);
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
    sf.getCurrentSession().persist(link);
    CATEvent nodeCompleted = CATEvent.buildNodeOperationCompletedEvent(nodeDO.getUID(), sub.getPrimaryKey().toString());
    eventManager.queueEvent(nodeCompleted);
	}

	@SuppressWarnings("unchecked")
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

  @RequestMapping(method=RequestMethod.POST,value="/{entryPointUID}")
	public void subscribeToEntryPointWithObjects(Set<Address> addresses, @PathVariable("entryPointUID") String entryPointUID) 
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

	public void subscribeContactsToEntryPoint(List<Contact> contacts, String entryPointUID) 
	{
    CampaignManager campaignManager = ctx.getBean(CampaignManager.class);
    
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
		searchCriteria.setClientIds(securityUtil.getAllowedClientIDs(sf.getCurrentSession(), searchCriteria.getClientIds()));
		subscribeContactsToEntryPoint(contacts.getResults(), entryPointUID);
	}
	
	@SuppressWarnings("unchecked")
	public List<Subscriber> getSubscribedAddresses(String nodeUID) 
	{
    CampaignManager campaignManager = ctx.getBean(CampaignManager.class);

    // Load node and campaign
		Node node = campaignManager.getNode(nodeUID);
		CampaignDO camp = campaignManager.getSimpleCampaign(node.getCampaignUID());
		
		//Check permissions.
		if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), camp.getClient().getPrimaryKey()))
			throw new SecurityException("Current user is not allowed to view subscriptions to the specified campaign.");
		
		// Query for subscriber addresses for this node
		List<Subscriber> ret = new ArrayList<Subscriber>();
		Criteria crit = sf.getCurrentSession().createCriteria(SubscriberDO.class);
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
			
			Criteria crit = sf.getCurrentSession().createCriteria(CampaignSubscriberLinkDO.class);
			crit.add(Restrictions.in("subscriber.primaryKey", ids));
			crit.add(Restrictions.eq("campaign.primaryKey", campaignId));
			
			for (CampaignSubscriberLinkDO link : (List<CampaignSubscriberLinkDO>)crit.list())
				sf.getCurrentSession().delete(link);
		}
		catch(Exception e)
		{
			logger.error("Error unsubscribing", e);
			throw new RuntimeException(e);
		}
	}
	
	public boolean isSubscriberBlacklisted(String address, EntryPointType type, String incomingAddress, Long clientID)
	{
		Criteria crit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
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

	public void registerTwitterFollower(String twitterID, String accountName) {
		Criteria crit = sf.getCurrentSession().createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("type",EntryPointType.Twitter));
		crit.add(Restrictions.eq("address",twitterID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub==null)
		{
			sub = new SubscriberDO();
			sub.setType(EntryPointType.Twitter);
			sub.setAddress(twitterID);
			sf.getCurrentSession().persist(sub);
		} else {
			unBlacklistAddressForEntryPoint(twitterID, EntryPointType.Twitter, accountName);
		}
		
		EntryPointDefinition epd = clientManager.getEntryPointDefinition(EntryPointType.Twitter,accountName);
		for(Integer clientID : epd.getClientIDs())
		{
			crit = sf.getCurrentSession().createCriteria(ContactDO.class);
			crit.add(Restrictions.eq("type", EntryPointType.Twitter));
			crit.add(Restrictions.eq("alternateId", twitterID));
			crit.add(Restrictions.eq("client.id",new Long(clientID)));
			ContactDO contact = (ContactDO)crit.uniqueResult();
			if(contact==null)
			{
				contact = new ContactDO();
				contact.setType(EntryPointType.Twitter);
				contact.setAlternateId(twitterID);
				contact.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, new Long(clientID)));
				contact.setCreateDate(Calendar.getInstance());
				sf.getCurrentSession().persist(contact);
			}
		}
		
		crit = sf.getCurrentSession().createCriteria(CampaignInfoDO.class);
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

	public void removeTwitterFollower(String twitterID, String accountName) {
		blacklistAddressForEntryPoint(twitterID, EntryPointType.Twitter, accountName);
	}
	
	public void registerFacebookFollower(String facebookID, String accountName) {
		Criteria crit = sf.getCurrentSession().createCriteria(SubscriberDO.class);
		crit.add(Restrictions.eq("facebookID",facebookID));
		SubscriberDO sub = (SubscriberDO)crit.uniqueResult();
		if(sub==null)
		{
			sub = new SubscriberDO();
			sub.setType(EntryPointType.Facebook);
			sub.setAddress(facebookID);
			sf.getCurrentSession().persist(sub);
		} else {
			unBlacklistAddressForEntryPoint(facebookID, EntryPointType.Facebook, accountName);
		}
		
		EntryPointDefinition epd = clientManager.getEntryPointDefinition(EntryPointType.Facebook,accountName);
		for(Integer clientID : epd.getClientIDs())
		{
			crit = sf.getCurrentSession().createCriteria(ContactDO.class);
			crit.add(Restrictions.eq("type", EntryPointType.Facebook));
			crit.add(Restrictions.eq("alternateId", facebookID));
			crit.add(Restrictions.eq("client.id",new Long(clientID)));
			ContactDO contact = (ContactDO)crit.uniqueResult();
			if(contact==null)
			{
				contact = new ContactDO();
				contact.setType(EntryPointType.Facebook);
				contact.setAlternateId(facebookID);
				contact.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, new Long(clientID)));
				contact.setCreateDate(Calendar.getInstance());
				sf.getCurrentSession().persist(contact);
			}
		}
		
		crit = sf.getCurrentSession().createCriteria(CampaignInfoDO.class);
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

	public void removeFacebookFollower(String facebookID, String accountName) {
		blacklistAddressForEntryPoint(facebookID, EntryPointType.Facebook, accountName);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Blacklist a set of contacts addresses.  These don't need to
	 * be existing contacts, only the address and type will be used
	 * add to the blacklist.
	 */
	public void blacklistContacts(List<Contact> contacts) 
	{
		for (Contact c : contacts)
		{
			if(c.getClientId()==null && !securityUtil.isAdmin())
				throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+c.getAddress()+", type:"+c.getType()+")");

			// Create query
			Criteria crit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
			crit.add(Restrictions.eq("address", c.getAddress()));
			crit.add(Restrictions.eq("entryPointType", c.getType()));
			crit.add(Restrictions.eq("client.id",c.getClientId()));
			
			List<BlacklistDO> blacklistEntries = (List<BlacklistDO>)crit.list();
			if(blacklistEntries == null || blacklistEntries.size()==0)
			{
				BlacklistDO blacklist = new BlacklistDO();
				blacklist.setEntryPointType(c.getType());
				blacklist.setAddress(c.getAddress());
				blacklist.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, c.getClientId()));
				sf.getCurrentSession().persist(blacklist);

				for(CampaignSubscriberLinkDO link : findSubscriptionsByClient(c.getAddress(),c.getType(),c.getClientId()))
				{
					link.setActive(false);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void unBlacklistContacts(List<Contact> contacts)
	{
		for (Contact c : contacts)
		{
			if(c.getClientId()==null && !securityUtil.isAdmin())
				throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+c.getAddress()+", type:"+c.getType()+")");
			Criteria crit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
			crit.add(Restrictions.eq("address", c.getAddress()));
			crit.add(Restrictions.eq("entryPointType", c.getType()));
		
			// Filter out addresses already blacklisted
			for (BlacklistDO blacklist : (List<BlacklistDO>)crit.list())
			{
				sf.getCurrentSession().delete(blacklist);
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
		Criteria crit = sf.getCurrentSession().createCriteria(CampaignSubscriberLinkDO.class);
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
		Query q = sf.getCurrentSession().createQuery(query);
		q.setParameter("entryPoint", incomingAddress);
		q.setParameter("type", type);
		q.setParameter("published", true);
		
		List<CampaignDO> campaigns = (List<CampaignDO>)q.list();
		
		if(campaigns==null || campaigns.size()==0)
			return new ArrayList<CampaignSubscriberLinkDO>();
		
		Criteria crit = sf.getCurrentSession().createCriteria(CampaignSubscriberLinkDO.class);
		crit.createAlias("subscriber","sub");
		crit.add(Restrictions.eq("sub.address",address));
		crit.add(Restrictions.eq("sub.type", type));
		crit.add(Restrictions.in("campaign", campaigns));
		
		return (List<CampaignSubscriberLinkDO>)crit.list();
	}

	public void blacklistAddressForEntryPoint(String address,
			EntryPointType type, String incomingAddress) {
		
		if(incomingAddress==null && !securityUtil.isAdmin())
			throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+address+", type:"+type+")");
		
		Criteria crit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
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
			sf.getCurrentSession().persist(blacklist);

			for(CampaignSubscriberLinkDO link : findSubscriptionsByEntryPoint(address,type,incomingAddress))
			{
				link.setActive(false);
			}
		}
		
	}

	public void unBlacklistAddressForEntryPoint(String address,
			EntryPointType type, String incomingAddress) {

		if(incomingAddress==null && !securityUtil.isAdmin())
			throw new SecurityException("Only admins may place or lift a blanket blacklist!  (address:"+address+", type:"+type+")");
		
		Criteria crit = sf.getCurrentSession().createCriteria(BlacklistDO.class);
		crit.add(Restrictions.eq("address", address));
		crit.add(Restrictions.eq("entryPointType", type));

		List<BlacklistDO> bList = (List<BlacklistDO>)crit.list();
		Set<Long> clientIDs = new HashSet<Long>();
		// Filter out addresses already blacklisted
		for (BlacklistDO blacklist : bList)
		{
			if(blacklist.getIncomingAddress().equals(incomingAddress))
				sf.getCurrentSession().delete(blacklist);
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
