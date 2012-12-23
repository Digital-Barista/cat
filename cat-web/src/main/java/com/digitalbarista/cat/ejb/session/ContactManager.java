package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactInfo;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.CouponRedemption;
import com.digitalbarista.cat.business.PagingInfo;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactInfoDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.ContactTagLinkDO;
import com.digitalbarista.cat.data.ContactTagType;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.exception.ReportingManagerException;
import com.digitalbarista.cat.util.PagedList;
import com.digitalbarista.cat.util.PagingUtil;
import com.digitalbarista.cat.util.SecurityUtil;

import java.util.Arrays;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Session Bean implementation class ContactManagerImpl
 */
@Controller
@Transactional(propagation=Propagation.REQUIRED)
@RequestMapping(value="/contacts",
                produces={"application/xml","application/json"},
                consumes={"application/xml","application/json"})
public class ContactManager{

	private Logger log = LogManager.getLogger(ContactManager.class);
	
        @Autowired
        private SessionFactory sf;

        @Autowired
        UserManager userManager;

        @Autowired
        ReportingManager reportingManager;
	
        @Autowired
	FacebookManager facebookManager;
        
        @Autowired
        SecurityUtil securityUtil;
	
	private static final String[] ADMIN_FACEBOOK_FIELDS = {"link", "name", "first_name", "last_name", "id"};
	
	public boolean contactExists(String address, EntryPointType type, Long clientId)
	{
		if(address==null)
			throw new IllegalArgumentException("Address must be provided.");
		if(type==null)
			throw new IllegalArgumentException("Type must be provided.");
		if(clientId==null)
			throw new IllegalArgumentException("ClientID must be provided.");
		
		Criteria crit = null;

		crit = sf.getCurrentSession().createCriteria(ContactDO.class);
		crit.add(Restrictions.eq("address", address));
		crit.add(Restrictions.eq("type", type));
		crit.add(Restrictions.eq("client.id", clientId));

		return crit.uniqueResult()!=null;
	}
	
	@SuppressWarnings("unchecked")
        @RequestMapping(method=RequestMethod.GET)
	public PagedList<Contact> getContacts(ContactSearchCriteria searchCriteria, PagingInfo paging)
	{
		Criteria crit = null;
		PagedList<Contact> ret = new PagedList<Contact>();
		crit = sf.getCurrentSession().createCriteria(ContactDO.class);

		// Find allowed client IDs to query on
		List<Long> requestedClientIds = null;
		if (searchCriteria != null)
		{
			requestedClientIds = searchCriteria.getClientIds();
		}
		List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), requestedClientIds);
		
		// Don't do a query if the filter will filter everything out
		if (allowedClientIds.size() > 0 &&
			isValidSearch(searchCriteria))
		{
			crit.add(Restrictions.in("client.primaryKey", allowedClientIds));
    	
			// Apply search criteria
	    	if (searchCriteria != null)
	    	{
	    		// Filter by address
	    		if (searchCriteria.getAddress() != null)
	    		{
	    			crit.add(Restrictions.eq("address", searchCriteria.getAddress()));
	    		}
	    		
	    		// Filter by entry point types
	    		if (searchCriteria.getEntryTypes() != null)
	    		{
	    			List<EntryPointType> types = new ArrayList<EntryPointType>();
	    			for (int i = 0; i < searchCriteria.getEntryTypes().size(); i++)
	    			{
	    				Object item = searchCriteria.getEntryTypes().get(i);
	    				EntryPointType type = item instanceof EntryPointType ? (EntryPointType)item : EntryPointType.valueOf(item.toString());
	    				types.add(type);
	    			}
	    			crit.add(Restrictions.in("type", types));
	    		}
	    		
	    		// Filter by list of contacts associated with the contact
	    		if (searchCriteria.getContactTags() != null &&
	    			searchCriteria.getContactTags().size() > 0)
	    		{
	    			List<Long> tagIds = new ArrayList<Long>();
	    			for (ContactTag tag : searchCriteria.getContactTags())
	    				tagIds.add(tag.getContactTagId());
	    			crit.createAlias("contactTags", "contactTags");
	    			crit.createAlias("contactTags.tag", "tag");
	    			crit.add(Restrictions.in("tag.contactTagId", tagIds));
	    		}
	    	}
	    	
	    	// Get unpaged total results from criteria
	    	ret.setTotalResultCount(PagingUtil.getTotalResultCount(crit));
	    	
	    	// Apply paging info
	    	if(paging!=null)
	    		PagingUtil.applyPagingInfo(crit, paging);
	    	
	    	// Get only distinct contacts
	    	crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    	
	    	// Convert data objects to business objects
			for(ContactDO contact : (List<ContactDO>)crit.list())
			{
				Contact c = new Contact();
				c.copyFrom(contact);
				ret.getResults().add(c);
			}
		}
		return ret;
	}
	private Boolean isValidSearch(ContactSearchCriteria criteria)
	{
		Boolean ret = true;
		
		if (criteria != null)
		{
			if (criteria.getEntryTypes() != null &&
				criteria.getEntryTypes().size() == 0)
			{
				ret = false;
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
        @RequestMapping(method=RequestMethod.GET,value="/tags")
	public List<ContactTag> getContactTags()
	{
		Criteria crit = null;
		List<ContactTag> ret = new ArrayList<ContactTag>();
		crit = sf.getCurrentSession().createCriteria(ContactTagDO.class);

		// Limit query by allowed clients if necessary
                if(!securityUtil.isAdmin())
                {
                        crit.add(Restrictions.in("client.primaryKey", securityUtil.extractClientIds(sf.getCurrentSession())));
                                if(securityUtil.extractClientIds(sf.getCurrentSession()).size() == 0)
                                        return ret;
                }
    	
		for (ContactTagDO tag : (List<ContactTagDO>)crit.list())
		{
			ContactTag t = new ContactTag();
			t.copyFrom(tag);
			ret.add(t);
		}
		return ret;
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	public Contact getContactForSubscription(SubscriberDO sub, CampaignDO camp)
	{
		ContactSearchCriteria searchCrit = new ContactSearchCriteria();
		List<Long> clientIds = new ArrayList<Long>();
		clientIds.add(camp.getClient().getPrimaryKey());
		searchCrit.setClientIds(clientIds);
		searchCrit.setEntryTypes(Arrays.asList(new EntryPointType[]{sub.getType()}));
		searchCrit.setAddress(sub.getAddress());
		PagedList<Contact> matchingContacts = getContacts(searchCrit, null);
		if(matchingContacts.getTotalResultCount()!=1)
		{
			return null;
		}
		return matchingContacts.getResults().get(0);
	}
	
        @RequestMapping(method=RequestMethod.POST,value="/tags")
	@AuditEvent(AuditType.SaveContactTag)
	public ContactTag save(@RequestBody ContactTag tag) 
	{
		if(tag == null)
			throw new IllegalArgumentException("Cannot save a null tag.");
		
		ContactTagDO t = null;
		if (tag.getContactTagId() != null)
			t = (ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, tag.getContactTagId());
		
		if(t == null)
			t = new ContactTagDO();
		
		// Copy all properties to DO
		tag.copyTo(t);
		t.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, tag.getClientId()));

		sf.getCurrentSession().persist(t);
		sf.getCurrentSession().flush();
		ContactTag ret = new ContactTag();
		ret.copyFrom(t);
		return ret;
	}

        @RequestMapping(method=RequestMethod.POST)
	@AuditEvent(AuditType.SaveContact)
	public Contact save(@RequestBody Contact contact) 
	{
		if(contact == null)
			throw new IllegalArgumentException("Cannot save a null contact.");
		
		ContactDO c = null;
		if (contact.getContactId() != null)
			c = (ContactDO)sf.getCurrentSession().get(ContactDO.class, contact.getContactId());
		
		if(c == null)
			c = new ContactDO();
		
		// Copy all properties to DO
		contact.copyTo(c);
		c.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, contact.getClientId()));
		c.setCreateDate(Calendar.getInstance());

		sf.getCurrentSession().persist(c);
		sf.getCurrentSession().flush();
		Contact ret = new Contact();
		ret.copyFrom(c);
		return ret;
	}

        @RequestMapping(method=RequestMethod.DELETE,value="/tags")
	public void delete(@RequestBody ContactTag tag) {
		if(tag == null)
			throw new IllegalArgumentException("Cannot delete a null tag.");
				
		ContactTagDO tagDO = null;
		if(tag.getContactTagId()!=null)
			tagDO = (ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, tag.getContactTagId());

		if (tagDO == null)
			throw new IllegalArgumentException("Cannot find tag to delete with contactTagId: " + tag.getContactTagId());
		
		sf.getCurrentSession().delete(tagDO);
	}

        @RequestMapping(method=RequestMethod.DELETE)
	public void delete(@RequestBody Contact contact) {
		if(contact == null)
			throw new IllegalArgumentException("Cannot delete a null contact.");
				
		ContactDO contactDO=null;
		if(contact.getContactId()!=null)
			contactDO = (ContactDO)sf.getCurrentSession().get(ContactDO.class, contact.getContactId());

		if (contactDO == null)
			throw new IllegalArgumentException("Cannot find contact to delete with contactId: " + contact.getContactId());
		
		sf.getCurrentSession().delete(contactDO);
	}

	public void delete(List<Contact> contacts)
	{
		for (Contact c : contacts)
		{
			delete(c);
		}
	}

	public void addTagsToContacts(List<Contact> contacts, List<ContactTag> tags) 
	{
		for (Contact c : contacts)
		{
			ContactDO cDO = (ContactDO)sf.getCurrentSession().get(ContactDO.class, c.getContactId());
			for (ContactTag tag : tags)
			{
				ContactTagDO tagDO = (ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, tag.getContactTagId());
				if (cDO.findLink(tagDO)==null)
				{
					ContactTagLinkDO ctldo = new ContactTagLinkDO();
					ctldo.setContact(cDO);
					ctldo.setTag(tagDO);
					ctldo.setInitialTagDate(new Date());
					cDO.getContactTags().add(ctldo);
					sf.getCurrentSession().persist(ctldo);
				}
			}
			sf.getCurrentSession().persist(cDO);
		}
	}

	public void removeTagsFromContacts(List<Contact> contacts, List<ContactTag> tags) 
	{
		for (Contact c : contacts)
		{
			ContactDO cDO = (ContactDO)sf.getCurrentSession().get(ContactDO.class, c.getContactId());
			for (ContactTag tag : tags)
			{
				ContactTagDO tagDO = (ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, tag.getContactTagId());
				ContactTagLinkDO linkDO = cDO.findLink(tagDO);
				if (linkDO != null)
				{
					cDO.getContactTags().remove(linkDO);
					sf.getCurrentSession().delete(linkDO);
				}
			}
			sf.getCurrentSession().persist(cDO);
		}
	}

	public List<Contact> importContacts(List<Contact> contacts) 
	{
		List<Contact> ret = new ArrayList<Contact>();
		
		for (Contact c : contacts)
		{
			// Create contact
			ContactDO cDO = new ContactDO();
			c.copyTo(cDO);
			cDO.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, c.getClientId()));
			cDO.setCreateDate(Calendar.getInstance());

			sf.getCurrentSession().persist(cDO);
			
			// Add tags
			for (ContactTag tag : c.getContactTags())
			{
				ContactTagDO tagDO = (ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, tag.getContactTagId());
				if (cDO.getContactTags() == null)
					cDO.setContactTags(new HashSet<ContactTagLinkDO>());
				
				if (cDO.findLink(tagDO)==null)
				{
					ContactTagLinkDO ctldo = new ContactTagLinkDO();
					ctldo.setContact(cDO);
					ctldo.setTag(tagDO);
					ctldo.setInitialTagDate(new Date());
					cDO.getContactTags().add(ctldo);
					sf.getCurrentSession().persist(ctldo);
				}
			}
			
			// Add persisted contact to return list
			Contact retContact = new Contact();
			retContact.copyFrom(cDO);
			ret.add(retContact);
		}
		return ret;
	}

	public ContactTag findContactTag(Integer clientID, String tag, ContactTagType type) {
		Criteria crit = sf.getCurrentSession().createCriteria(ContactTagDO.class);
		crit.add(Restrictions.eq("client.primaryKey", clientID.longValue()));
		crit.add(Restrictions.eq("tag", tag));
		crit.add(Restrictions.eq("type", type));
		ContactTag ret = new ContactTag();
		ret.copyFrom((ContactTagDO)crit.uniqueResult());
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public void tagContactNodeReached(String contact, String nodeUID, EntryPointType entryType)
	{
		// Find node
		NodeDO nDO = null;
		Criteria crit = sf.getCurrentSession().createCriteria(NodeDO.class);
		crit.add(Restrictions.eq("uid", nodeUID));
		List<NodeDO> nodes = (List<NodeDO>)crit.list();
		if (nodes.size() > 0)
			nDO = (NodeDO)nodes.get(0);
		
		if (nDO != null)
		{
			// Find contact with given name for the associated client
			crit = sf.getCurrentSession().createCriteria(ContactDO.class);
			crit.add(Restrictions.eq("address", nDO.getUID()));
			crit.add(Restrictions.eq("client.primaryKey", nDO.getCampaign().getClient().getPrimaryKey()));
			crit.add(Restrictions.eq("type", entryType));
			List<ContactDO> contacts = (List<ContactDO>)crit.list();
			
			if (contacts.size() > 0)
			{
				// Find contact tag with name == nodeUID
				crit = sf.getCurrentSession().createCriteria(ContactTagDO.class);
				crit.add(Restrictions.eq("tag", nodeUID));
				crit.add(Restrictions.eq("client.primaryKey", nDO.getCampaign().getClient().getPrimaryKey()));
				List<ContactTagDO> tags = (List<ContactTagDO>)crit.list();
				
				ContactTagDO tag = null;
				if (tags.size() > 0)
					tag = tags.get(0);
				
				// If the tag doesn't exist create it
				if (tag == null)
				{
					tag = new ContactTagDO();
					tag.setClient(nDO.getCampaign().getClient());
					tag.setTag(nDO.getUID());
					tag.setType(ContactTagType.NODE_REACHED);
					
					sf.getCurrentSession().persist(tag);
				}
				
				// Add tag to all matching contacts
				for (ContactDO c : contacts)
				{
					if (c.findLink(tag)==null)
					{
						ContactTagLinkDO ctldo = new ContactTagLinkDO();
						ctldo.setContact(c);
						ctldo.setTag(tag);
						ctldo.setInitialTagDate(new Date());
						c.getContactTags().add(ctldo);
						sf.getCurrentSession().persist(ctldo);
						sf.getCurrentSession().persist(c);
					}
				}
			}
		}
		else
		{
			log.error("Could not find node with UID: " + nodeUID + " to add node reached tag");
		}
		
	}

	public Contact getDetailedContact(Long clientID)
	{
		Contact ret = null;
		ContactDO contactDO = (ContactDO)sf.getCurrentSession().get(ContactDO.class, clientID);
		if (contactDO != null)
		{
			ret = new Contact();
			ret.copyFrom(contactDO);
			
			// Get facebook profile info
			if (contactDO.getType() == EntryPointType.Facebook)
			{
				Contact c = new Contact();
				c.copyFrom(contactDO);
				List<ContactInfo> values = facebookManager.updateProfileInformation(c);
				
				// Remove values only visible to admins
				if (!securityUtil.isAdmin())
				{
					ret.setContactInfos(new HashSet<ContactInfo>());
					for (ContactInfo ci : values)
					{
						if (!Arrays.asList(ADMIN_FACEBOOK_FIELDS).contains(ci.getName()))
						{
							ret.getContactInfos().add(ci);
						}
					}
				}
				else
				{
					ret.setContactInfos(new HashSet<ContactInfo>(values));
				}
				

				// Call analytics for app visits
				try
				{
					Map<String, String> appVisits = reportingManager.getUserAnalyticsHistory(contactDO.getAddress());
					ret.setAppVisits(appVisits);
				} 
				catch (ReportingManagerException e)
				{
					log.error("Failed trying to get Facebook App analytics", e);
				}
			}
			else
			{
				// Add contact info
				for (ContactInfoDO ciDO : contactDO.getContactInfos())
				{
					ContactInfo ci = new ContactInfo();
					ci.copyFrom(ciDO);
					ret.getContactInfos().add(ci);
				}
			}
			
			// Query for campaigns this contact address is currently in
			Criteria crit = sf.getCurrentSession().createCriteria(CampaignDO.class);
			crit.createAlias("subscribers", "subscribers");
			crit.createAlias("subscribers.subscriber", "subscriber");
			crit.add(Restrictions.eq("subscribers.active", true));
			crit.add(Restrictions.eq("subscriber.address", contactDO.getAddress()));
			crit.add(Restrictions.eq("subscriber.type", contactDO.getType()));
	    	crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    	
			Set<Campaign> subscribedCampaigns = new HashSet<Campaign>();
			for (CampaignDO campaignDO : (List<CampaignDO>)crit.list())
			{
				Campaign campaign = new Campaign();
				campaign.copyFrom(campaignDO);
				subscribedCampaigns.add(campaign);
			}
			ret.setSubscribedCampaigns(subscribedCampaigns);
			
			// Query for coupon redemptions
			crit = sf.getCurrentSession().createCriteria(CouponRedemptionDO.class);
			crit.createAlias("couponResponse", "couponResponse");
			crit.createAlias("couponResponse.subscriber", "subscriber");
			crit.add(Restrictions.eq("subscriber.address", contactDO.getAddress()));
			List<CouponRedemptionDO> redemptions = (List<CouponRedemptionDO>)crit.list();
			for (CouponRedemptionDO rDO : redemptions)
			{
				CouponRedemption r = new CouponRedemption();
				r.setActualMessage(rDO.getCouponResponse().getActualMessage());
				r.setCouponExpirationDate(rDO.getCouponResponse().getCouponOffer().getCouponExpirationDate());
				r.setCouponExpirationDays(rDO.getCouponResponse().getCouponOffer().getCouponExpirationDays());
				r.setCouponName(rDO.getCouponResponse().getCouponOffer().getCouponName());
				r.setOfferCode(rDO.getCouponResponse().getCouponOffer().getOfferCode());
				r.setOfferUnavailableDate(rDO.getCouponResponse().getCouponOffer().getOfferUnavailableDate());
				r.setResponseDate(rDO.getCouponResponse().getResponseDate());
				r.setResponseDetail(rDO.getCouponResponse().getResponseDetail());
				r.setResponseType(rDO.getCouponResponse().getResponseType());
				ret.getCouponRedemptions().add(r);
			}
			
		}
		
		return ret;
	}
}
