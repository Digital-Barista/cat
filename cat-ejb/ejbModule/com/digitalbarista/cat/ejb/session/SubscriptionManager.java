package com.digitalbarista.cat.ejb.session;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.Subscriber;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.EntryPointType;

@Local
@Path("/subscriptions")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface SubscriptionManager {
	@Path("/{type}")
	@GET
	@Wrapped(element="Addresses")
	public Set<String> getAllAddresses(@QueryParam("clientid") Long clientId, @PathParam("type") EntryPointType type);
	@Path("/{type}/{entryPointUID}")
	@POST
	public void subscribeToEntryPoint(@Wrapped(element="Addresses") Set<String> addresses, @PathParam("entryPointUID") String entryPointUID, @PathParam("type") EntryPointType subscriptionType);

	public void subscribeContactsToEntryPoint(@Wrapped(element="Contact") List<Contact> contacts, @PathParam("entryPointUID") String entryPointUID);

	public void subscribeContactFilterToEntryPoint(ContactSearchCriteria searchCriteria, String entryPointUID);
	
	public List<Subscriber> getSubscribedAddresses(String campaignUID);
	
	public void unsubscribeSubscribers(List<Long> subscriberIds, Long campaignId);
	
	public boolean isSubscriberBlacklisted(Long SubscriberId, String entryPoint, EntryPointType type);

	public void registerTwitterFollower(String twitterID, String accountName);
	
	public void removeTwitterFollower(String twitterID, String accountName);

	public void registerFacebookFollower(String facebookID, String accountName);
	
	public void removeFacebookFollower(String facebookID, String accountName);
	
	public void blacklistAddresses(List<Contact> contacts);
	
	public void unBlacklistAddresses(List<Contact> contacts);
	
}
