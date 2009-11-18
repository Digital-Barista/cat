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

@Local
@Path("/contacts")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface ContactManager {
	@GET
	@Wrapped(element="Contacts")
	public List<Contact> getContacts();
	@Path("/tags")
	@GET
	@Wrapped(element="ContactTags")
	public List<ContactTag> getContactTags();
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
	@POST
	@Path("/tags/assign")
	public void addTagsToContacts(@FormParam("contacts") List<Contact> contacts, @FormParam("tags") List<ContactTag> tags);
}
