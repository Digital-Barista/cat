package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Calendar;
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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.EntryPointType;

/**
 * Session Bean implementation class ContactManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/ContactManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class ContactManagerImpl implements ContactManager {

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
	public List<Contact> getContacts()
	{
		Criteria crit = null;
		List<Contact> ret = new ArrayList<Contact>();
		crit = session.createCriteria(ContactDO.class);

		// Limit query by allowed clients if necessary
    	if(!ctx.isCallerInRole("admin"))
    		crit.add(Restrictions.in("client.primaryKey", userManager.extractClientIds(ctx.getCallerPrincipal().getName())));
		
		for(ContactDO contact : (List<ContactDO>)crit.list())
		{
			Contact c = new Contact();
			c.copyFrom(contact);
			ret.add(c);
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
    		crit.add(Restrictions.in("client.primaryKey", userManager.extractClientIds(ctx.getCallerPrincipal().getName())));
		
		for (ContactTagDO tag : (List<ContactTagDO>)crit.list())
		{
			ContactTag t = new ContactTag();
			t.copyFrom(tag);
			ret.add(t);
		}
		return ret;
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addTagsToContacts(List<Contact> contacts, List<ContactTag> tags) 
	{
		for (Contact c : contacts)
		{
			ContactDO cDO = em.find(ContactDO.class, c.getContactId());
			for (ContactTag tag : tags)
			{
				ContactTagDO tagDO = em.find(ContactTagDO.class, tag.getContactTagId());
				if (!cDO.getContactTags().contains(tagDO))
					cDO.getContactTags().add(tagDO);
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
				if (cDO.getContactTags().contains(tagDO))
					cDO.getContactTags().remove(tagDO);
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
					cDO.setContactTags(new HashSet<ContactTagDO>());
				
				if (!cDO.getContactTags().contains(tagDO))
					cDO.getContactTags().add(tagDO);
			}
			
			// Add persisted contact to return list
			Contact retContact = new Contact();
			retContact.copyFrom(cDO);
			ret.add(retContact);
		}
		return ret;
	}
}
