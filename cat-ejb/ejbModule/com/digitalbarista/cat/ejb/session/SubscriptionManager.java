package com.digitalbarista.cat.ejb.session;

import java.util.Set;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.digitalbarista.cat.data.EntryPointType;

@Local
@Path("/subscriptions")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface SubscriptionManager {
	@Path("/{type}")
	@GET
	public Set<String> getAllAddresses(@QueryParam("clientid") Long clientId, @PathParam("type") EntryPointType type);
	public void subscribeToEntryPoint(Set<String> addresses, String entryPointUID, EntryPointType subscriptionType);
}
