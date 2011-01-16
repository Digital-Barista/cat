package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.PagingInfo;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.ContactTagLinkDO;
import com.digitalbarista.cat.data.ContactTagType;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.util.PagedList;
import com.digitalbarista.cat.util.PagingUtil;
import com.digitalbarista.cat.util.SecurityUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Session Bean implementation class ContactManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/ContactManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ContactManagerImpl implements ContactManager {

	private Logger log = LogManager.getLogger(ContactManagerImpl.class);
	
	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;

	@PermitAll
	public boolean contactExists(String address, EntryPointType type, Long clientId)
	{
		if(address==null)
			throw new IllegalArgumentException("Address must be provided.");
		if(type==null)
			throw new IllegalArgumentException("Type must be provided.");
		if(clientId==null)
			throw new IllegalArgumentException("ClientID must be provided.");
		
		Criteria crit = null;

		crit = session.createCriteria(ContactDO.class);
		crit.add(Restrictions.eq("address", address));
		crit.add(Restrictions.eq("type", type));
		crit.add(Restrictions.eq("client.id", clientId));

		return crit.uniqueResult()!=null;
	}
	
	@SuppressWarnings("unchecked")
	@PermitAll
	public PagedList<Contact> getContacts(ContactSearchCriteria searchCriteria, PagingInfo paging)
	{
		Criteria crit = null;
		PagedList<Contact> ret = new PagedList<Contact>();
		crit = session.createCriteria(ContactDO.class);

		// Find allowed client IDs to query on
		List<Long> requestedClientIds = null;
		if (searchCriteria != null)
		{
			requestedClientIds = searchCriteria.getClientIds();
		}
		List<Long> allowedClientIds = SecurityUtil.getAllowedClientIDs(ctx, session, requestedClientIds);
		
		// Don't do a query if the filter will filter everything out
		if (allowedClientIds.size() > 0 &&
			isValidSearch(searchCriteria))
		{
			crit.add(Restrictions.in("client.primaryKey", allowedClientIds));
    	
			// Apply search criteria
	    	if (searchCriteria != null)
	    	{
	    		// Filter by entry point types
	    		if (searchCriteria.getEntryTypes() != null)
	    		{
	    			List<EntryPointType> types = new ArrayList<EntryPointType>();
	    			for (Object item : searchCriteria.getEntryTypes())
	    			{
	    				if (item instanceof String)
	    				{
	    					types.add(EntryPointType.valueOf(item.toString()));
	    				}
	    				else if (item instanceof EntryPointType)
	    				{
	    					types.add((EntryPointType)item);
	    				}
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
			if (criteria.getContactTags() != null &&
				criteria.getContactTags().size() == 0)
			{
				ret = false;
			}
			
			if (criteria.getEntryTypes() != null &&
				criteria.getEntryTypes().size() == 0)
			{
				ret = false;
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<ContactTag> getContactTags()
	{
		Criteria crit = null;
		List<ContactTag> ret = new ArrayList<ContactTag>();
		crit = session.createCriteria(ContactTagDO.class);

		// Limit query by allowed clients if necessary
    	if(!ctx.isCallerInRole("admin"))
    	{
    		crit.add(Restrictions.in("client.primaryKey", SecurityUtil.extractClientIds(ctx, session)));
			if(SecurityUtil.extractClientIds(ctx, session).size() == 0)
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
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
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
	
	@Override
	@PermitAll
	@AuditEvent(AuditType.SaveContactTag)
	public ContactTag save(ContactTag tag) 
	{
		if(tag == null)
			throw new IllegalArgumentException("Cannot save a null tag.");
		
		ContactTagDO t = null;
		if (tag.getContactTagId() != null)
			t = em.find(ContactTagDO.class, tag.getContactTagId());
		
		if(t == null)
			t = new ContactTagDO();
		
		// Copy all properties to DO
		tag.copyTo(t);
		t.setClient(em.find(ClientDO.class, tag.getClientId()));

		em.persist(t);
		em.flush();
		ContactTag ret = new ContactTag();
		ret.copyFrom(t);
		return ret;
	}

	@Override
	@PermitAll
	@AuditEvent(AuditType.SaveContact)
	public Contact save(Contact contact) 
	{
		if(contact == null)
			throw new IllegalArgumentException("Cannot save a null contact.");
		
		ContactDO c = null;
		if (contact.getContactId() != null)
			c = em.find(ContactDO.class, contact.getContactId());
		
		if(c == null)
			c = new ContactDO();
		
		// Copy all properties to DO
		contact.copyTo(c);
		c.setClient(em.find(ClientDO.class, contact.getClientId()));
		c.setCreateDate(Calendar.getInstance());

		em.persist(c);
		em.flush();
		Contact ret = new Contact();
		ret.copyFrom(c);
		return ret;
	}

	@Override
	public void delete(ContactTag tag) {
		if(tag == null)
			throw new IllegalArgumentException("Cannot delete a null tag.");
				
		ContactTagDO tagDO = null;
		if(tag.getContactTagId()!=null)
			tagDO = em.find(ContactTagDO.class, tag.getContactTagId());

		if (tagDO == null)
			throw new IllegalArgumentException("Cannot find tag to delete with contactTagId: " + tag.getContactTagId());
		
		em.remove(tagDO);
	}

	@Override
	public void delete(Contact contact) {
		if(contact == null)
			throw new IllegalArgumentException("Cannot delete a null contact.");
				
		ContactDO contactDO=null;
		if(contact.getContactId()!=null)
			contactDO = em.find(ContactDO.class, contact.getContactId());

		if (contactDO == null)
			throw new IllegalArgumentException("Cannot find contact to delete with contactId: " + contact.getContactId());
		
		em.remove(contactDO);
	}

	@Override
	public void delete(List<Contact> contacts)
	{
		for (Contact c : contacts)
		{
			delete(c);
		}
	}

	@Override
	public void addTagsToContacts(List<Contact> contacts, List<ContactTag> tags) 
	{
		for (Contact c : contacts)
		{
			ContactDO cDO = em.find(ContactDO.class, c.getContactId());
			for (ContactTag tag : tags)
			{
				ContactTagDO tagDO = em.find(ContactTagDO.class, tag.getContactTagId());
				if (cDO.findLink(tagDO)==null)
				{
					ContactTagLinkDO ctldo = new ContactTagLinkDO();
					ctldo.setContact(cDO);
					ctldo.setTag(tagDO);
					ctldo.setInitialTagDate(new Date());
					cDO.getContactTags().add(ctldo);
					em.persist(ctldo);
				}
			}
			em.persist(cDO);
		}
	}

	@Override
	public void removeTagsFromContacts(List<Contact> contacts, List<ContactTag> tags) 
	{
		for (Contact c : contacts)
		{
			ContactDO cDO = em.find(ContactDO.class, c.getContactId());
			for (ContactTag tag : tags)
			{
				ContactTagDO tagDO = em.find(ContactTagDO.class, tag.getContactTagId());
				ContactTagLinkDO linkDO = cDO.findLink(tagDO);
				if (linkDO != null)
				{
					cDO.getContactTags().remove(linkDO);
					em.remove(linkDO);
				}
			}
			em.persist(cDO);
		}
	}

	@Override
	public List<Contact> importContacts(List<Contact> contacts) 
	{
		List<Contact> ret = new ArrayList<Contact>();
		
		for (Contact c : contacts)
		{
			// Create contact
			ContactDO cDO = new ContactDO();
			c.copyTo(cDO);
			cDO.setClient(em.find(ClientDO.class, c.getClientId()));
			cDO.setCreateDate(Calendar.getInstance());

			em.persist(cDO);
			
			// Add tags
			for (ContactTag tag : c.getContactTags())
			{
				ContactTagDO tagDO = em.find(ContactTagDO.class, tag.getContactTagId());
				if (cDO.getContactTags() == null)
					cDO.setContactTags(new HashSet<ContactTagLinkDO>());
				
				if (cDO.findLink(tagDO)==null)
				{
					ContactTagLinkDO ctldo = new ContactTagLinkDO();
					ctldo.setContact(cDO);
					ctldo.setTag(tagDO);
					ctldo.setInitialTagDate(new Date());
					cDO.getContactTags().add(ctldo);
					em.persist(ctldo);
				}
			}
			
			// Add persisted contact to return list
			Contact retContact = new Contact();
			retContact.copyFrom(cDO);
			ret.add(retContact);
		}
		return ret;
	}

	@Override
	public ContactTag findContactTag(Integer clientID, String tag, ContactTagType type) {
		Criteria crit = session.createCriteria(ContactTagDO.class);
		crit.add(Restrictions.eq("client.primaryKey", clientID.longValue()));
		crit.add(Restrictions.eq("tag", tag));
		crit.add(Restrictions.eq("type", type));
		ContactTag ret = new ContactTag();
		ret.copyFrom((ContactTagDO)crit.uniqueResult());
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void tagContactNodeReached(String contact, String nodeUID, EntryPointType entryType)
	{
		// Find node
		NodeDO nDO = null;
		Criteria crit = session.createCriteria(NodeDO.class);
		crit.add(Restrictions.eq("uid", nodeUID));
		List<NodeDO> nodes = (List<NodeDO>)crit.list();
		if (nodes.size() > 0)
			nDO = (NodeDO)nodes.get(0);
		
		if (nDO != null)
		{
			// Find contact with given name for the associated client
			crit = session.createCriteria(ContactDO.class);
			crit.add(Restrictions.eq("address", nDO.getUID()));
			crit.add(Restrictions.eq("client.primaryKey", nDO.getCampaign().getClient().getPrimaryKey()));
			crit.add(Restrictions.eq("type", entryType));
			List<ContactDO> contacts = (List<ContactDO>)crit.list();
			
			if (contacts.size() > 0)
			{
				// Find contact tag with name == nodeUID
				crit = session.createCriteria(ContactTagDO.class);
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
					
					em.persist(tag);
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
						em.persist(ctldo);
						em.persist(c);
					}
				}
			}
		}
		else
		{
			log.error("Could not find node with UID: " + nodeUID + " to add node reached tag");
		}
		
	}
}
