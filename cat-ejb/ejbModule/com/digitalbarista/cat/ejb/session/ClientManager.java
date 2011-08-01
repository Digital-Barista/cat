package com.digitalbarista.cat.ejb.session;
import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.business.Keyword;
import com.digitalbarista.cat.business.ReservedKeyword;
import com.digitalbarista.cat.data.EntryPointType;

@Local
@Path("/clients")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface ClientManager {
    @Path("/{id}")
    @GET
	public Client getClientById(@PathParam("id") long id);
    @GET
    @Wrapped(element="Clients")
	public List<Client> getVisibleClients();
	
    @GET
	@Path("/entryPoints")
	@Wrapped(element="EntryPoints")
	public List<EntryPointDefinition> getEntryPointDefinitions();
	public List<EntryPointDefinition> getEntryPointDefinitions(List<Long> clientIds);
	
	@GET
	@Path("/entryPoints")
	@Wrapped(element="EntryPoints")
	public EntryPointDefinition getEntryPointDefinition(@QueryParam("type") EntryPointType type, @QueryParam("account") String account);
	@POST
	public Client save(Client client);
	@POST
	@Path("/entryPoints")
	public EntryPointDefinition save(EntryPointDefinition epd);
	@Path("/keywords")
	@POST
	public Keyword save(Keyword kwd);
	@DELETE
	@Path("/keywords")
	public void delete(Keyword kwd);
	
	@Path("/keywords")
	@GET
	@Wrapped(element="Keywords")
	public List<Keyword> getAllKeywords();
	public List<Keyword> getKeywords(List<Long> clientIds);
	
	@Path("/entryPoints/{id}/keywords")
	@GET
	@Wrapped(element="Keywords")
	public List<Keyword> getAllKeywordsForEntryPoint(@PathParam("id") Long entryPointID);
	@Path("/{id}/keywords")
	@GET
	@Wrapped(element="Keywords")
	public List<Keyword> getAllKeywordsForClient(@PathParam("id") Long clientID);
	@Path("/{cid}/entryPoints/{eid}/keywords")
	@GET
	@Wrapped(element="Keywords")
	public List<Keyword> getAllKeywordsForClientAndEntryPoint(@PathParam("cid") Long clientID, @PathParam("eid") Long entryPointID);
	@Path("/reservedKeywords")
	@GET
	@Wrapped(element="Keywords")
	public List<ReservedKeyword> getAllReservedKeywords();
	@Path("/reservedKeywords")
	@POST
	public ReservedKeyword save(ReservedKeyword keyword);
	@Path("/reservedKeywords")
	@DELETE
	public void delete(ReservedKeyword keyword);
	@Path("/reservedKeywords/available")
	@POST
	public Boolean checkKeywordAvailability(Keyword keyword);
	public void disableClient(Long clientID);
	public void enableClient(Long clientID);
	
	@Path("/twitter/start-auth")
	@GET
	public String startTwitterAuth(@QueryParam("callbackURL")String callbackURL);
	
	@Path("/twitter/auth")
	@GET
	public void authTwitterAccount(@QueryParam("oauth_token")String oauthToken, @QueryParam("oauth_verifier")String oauthVerifier);
}
