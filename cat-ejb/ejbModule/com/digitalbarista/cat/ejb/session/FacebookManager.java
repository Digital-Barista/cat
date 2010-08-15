package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.exception.FacebookManagerException;


@Local
@Path("/facebook")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface FacebookManager 
{
	@GET
	@Path("/messages/list/{facebookAppId}")
	@Wrapped(element="messages")
	List<FacebookMessage> getMessages(@PathParam("facebookAppId") String facebookAppId, @Context UriInfo ui) throws FacebookManagerException;

	@PUT
	@Path("/messages/{facebookMessageId}/{response}")
	FacebookMessage respond(@PathParam("facebookMessageId") Integer facebookMessageId, @PathParam("response") String response, @Context UriInfo ui) throws FacebookManagerException;
	
	@DELETE
	@Path("/messages/{facebookMessageId}")
	void delete(@PathParam("facebookMessageId") Integer facebookMessageId, @Context UriInfo ui) throws FacebookManagerException;
	
	@PUT
	@Path("/messages/authorize/{facebookAppId}/{uid}")
	void userAuthorizeApp(@PathParam("facebookAppId") String facebookAppId, @PathParam("uid") String uid);
	
	@DELETE
	@Path("/messages/authorize/{facebookAppId}/{uid}")
	void userDeauthorizeApp(@PathParam("facebookAppId") String facebookAppId, @PathParam("uid") String uid);

	void updateMessageCounter(String appId, String uid);
	void updateMessageCounter(String appId, String uid, Integer count);
	
	Boolean isUserAllowingApp(String facebookUID, String facebookAppId);
}
