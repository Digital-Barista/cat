package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignEntryPointDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.KeywordDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.SubscriberBlacklistDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.data.UserDO;
import com.digitalbarista.cat.message.event.CATEvent;

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
	
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<Contact> getContacts()
	{
		Criteria crit = null;
		List<Contact> ret = new ArrayList<Contact>();
		crit = session.createCriteria(ContactDO.class);

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
}
