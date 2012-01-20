package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactInfo;
import com.digitalbarista.cat.business.FacebookApp;
import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.business.FacebookTrackingInfo;
import com.digitalbarista.cat.exception.FacebookManagerException;


@Local
@Path("/facebook")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface FacebookManager 
{
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@GET
	@Path("/messages/list/{appName}")
	@Wrapped(element="messages")
	List<FacebookMessage> getMessages(@PathParam("appName") String appName, 
			@FormParam("uid")String uid, @FormParam("signedRequest") String signedRequest, 
			@Context UriInfo ui) throws FacebookManagerException;

	@PUT
	@Consumes("application/x-www-form-urlencoded")
	@Path("/messages/{facebookMessageId}/{response}")
	FacebookMessage respond(@PathParam("facebookMessageId") Integer facebookMessageId, 
			@PathParam("response") String response, 
			@FormParam("uid") String uid,
			@FormParam("signedRequest") String signedRequest, 
			@Context UriInfo ui) throws FacebookManagerException;
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/messages/{facebookMessageId}")
	void delete(@PathParam("facebookMessageId") Integer facebookMessageId,
			@FormParam("signedRequest") String signedRequest, 
			@Context UriInfo ui) throws FacebookManagerException;
	
	@PUT
	@Path("/messages/authorize/{appName}/{uid}")
	void userAuthorizeApp(@PathParam("appName") String appName, @PathParam("uid") String uid);
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/deauthorize/{appName}")
	void userDeauthorizeApp(@PathParam("appName") String appName, @FormParam("fb_sig_user") String uid);

	void updateMessageCounter(String appName, String uid);
	void updateMessageCounter(String appName, String uid, Integer count);
	FacebookApp findFacebookAppByName(String applicationName);
	
	Boolean isUserAllowingApp(String facebookUID, String appName) throws FacebookManagerException;
	
	List<ContactInfo> updateProfileInformation(Contact contact);
	
	FacebookTrackingInfo getFacebookTrackingInfo(HttpServletRequest request);
	
	Boolean sendAppRequest(String facebookUID, String appName, String message);
}
