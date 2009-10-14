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

import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.business.Keyword;
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
	public List<Client> getVisibleClients();
	@GET
	@Path("/entryPoints")
	public List<EntryPointDefinition> getEntryPointDefinitions();
	@GET
	@Path("/entryPoints")
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
	@POST
	public List<Keyword> getAllKeywords();
	@Path("/entryPoints/{id}/keywords")
	@GET
	public List<Keyword> getAllKeywordsForEntryPoint(@PathParam("id") Long entryPointID);
	@Path("/{id}/keywords")
	@GET
	public List<Keyword> getAllKeywordsForClient(@PathParam("id") Long clientID);
	@Path("/{cid}/entryPoints/{eid}/keywords")
	@GET
	public List<Keyword> getAllKeywordsForClientAndEntryPoint(@PathParam("cid") Long clientID, @PathParam("eid") Long entryPointID);
}
