package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.FacebookMessage;


@Local
@Path("/facebook")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface FacebookManager 
{
	@GET
	@Path("/messages/{facebookAppId}/{uid}")
	@Wrapped(element="messages")
	List<FacebookMessage> getMessages(@PathParam("facebookAppId") String facebookAppId, @PathParam("uid") String uid);
}
