package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactTag;

@Local
@Path("/contacts")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface ContactManager {
	@GET
	public List<Contact> getContacts();
	public List<ContactTag> getContactTags();
	public ContactTag save(ContactTag tag);
	public Contact save(Contact contact);
	public void delete(ContactTag tag);
	public void delete(Contact contact);
}
