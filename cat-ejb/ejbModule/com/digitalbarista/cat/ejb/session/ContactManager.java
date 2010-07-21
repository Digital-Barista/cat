package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.PagingInfo;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.ContactTagType;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.util.PagedList;

@Local
@Path("/contacts")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface ContactManager {
	public boolean contactExists(String address, EntryPointType type, Long clientId);
	@GET
	@Wrapped(element="Contacts")
	public PagedList<Contact> getContacts(ContactSearchCriteria searchCriteria, PagingInfo paging);
	@Path("/tags")
	@GET
	@Wrapped(element="ContactTags")
	public List<ContactTag> getContactTags();
	public ContactTag findContactTag(Integer clientID, String tag, ContactTagType type);
	@POST
	@Path("/tags")
	public ContactTag save(ContactTag tag);
	@POST
	public Contact save(Contact contact);
	@DELETE
	@Path("/tags")
	public void delete(ContactTag tag);
	@DELETE
	public void delete(Contact contact);
	public void delete(List<Contact> contacts);
	public void addTagsToContacts(List<Contact> contacts, List<ContactTag> tags);
	public void removeTagsFromContacts(List<Contact> contacts, List<ContactTag> tags);
	public List<Contact> importContacts(List<Contact> contacts);
	
	public void tagContactNodeReached(String contact, String nodeUID, EntryPointType entryType);
}
