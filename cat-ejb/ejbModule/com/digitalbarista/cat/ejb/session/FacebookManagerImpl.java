package com.digitalbarista.cat.ejb.session;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.data.FacebookMessageDO;
import com.digitalbarista.cat.exception.FacebookManagerException;
import com.digitalbarista.cat.message.event.CATEvent;


/**
 * Session Bean implementation class FacebookManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/FacebookManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class FacebookManagerImpl implements FacebookManager {

	private final static String FACEBOOK_REST_URL = "https://api.facebook.com/restserver.php";
	
	private Logger logger = LogManager.getLogger(getClass());
	
	@Resource
	private SessionContext ctx; 

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;

	@EJB(name="ejb/cat/EventManager")
	EventManager eventManager;

	@EJB(name="ejb/cat/SubscriptionManager")
	SubscriptionManager subscriptionManager;
	
	@SuppressWarnings("unchecked")
	@Override
	@PermitAll
	public List<FacebookMessage> getMessages(String facebookAppId, String uid, UriInfo ui) 
	{
		if (!isAuthorized(ui))
			throw new FacebookManagerException("Could not authenticate");
		
		List<FacebookMessage> ret = new ArrayList<FacebookMessage>();
		
		Criteria crit = session.createCriteria(FacebookMessageDO.class);
		crit.add(Restrictions.eq("facebookUID", uid));
		crit.add(Restrictions.eq("facebookAppId", facebookAppId));
		crit.addOrder(Order.desc("createDate"));
		
		for (FacebookMessageDO messageDO : (List<FacebookMessageDO>)crit.list())
		{
			FacebookMessage message = new FacebookMessage();
			message.copyFrom(messageDO);
			ret.add(message);
		}
		
		return ret;
	}


	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(Integer facebookMessageId, UriInfo ui) 
	{
		if (!isAuthorized(ui))
			throw new FacebookManagerException("Could not authenticate");
		
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			em.remove(message);
		}
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FacebookMessage respond(Integer facebookMessageId, String response, UriInfo ui) 
	{
		if (!isAuthorized(ui))
			throw new FacebookManagerException("Could not authenticate");
		
		FacebookMessage ret = null;
		
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			// If the response is valid update the message
			if (message.getMetadata().indexOf(response) > -1)
				message.setResponse(response);
			
			eventManager.queueEvent(CATEvent.buildIncomingFacebookEvent(message.getFacebookUID(), message.getFacebookAppId(), response, message.getFacebookUID()));
			
			ret = new FacebookMessage();
			ret.copyFrom(message);
		}
		return ret;
	}


	@Override
	public String authorize(UriInfo ui) 
	{
		MultivaluedMap<String, String> params = ui.getQueryParameters();
		
		if (validateSignature(params) )
			return "<response>Valid session signature</response>";
		else
			return "<error>Invalid session signature</error>";
	}
	
	private boolean isAuthorized(UriInfo ui)
	{
		return true;
//		MultivaluedMap<String, String> params = ui.getQueryParameters();
//		return validateSignature(params);
	}
	
	private boolean validateSignature(MultivaluedMap<String, String> params)
	{
		String secret = "8c7bd765ef219a7bea8132c03dbe2892";
		
		// Build map of proper names to sort
		Map<String, String> values = new HashMap<String, String>();
		for (String key : params.keySet())
		{
			if (key.indexOf("fb_sig_") == 0)
			{
				String value = params.getFirst(key);
				values.put(key.substring(7), value);
			}
		}
		
		// Sort keys
		Object[] keys = values.keySet().toArray();
		Arrays.sort(keys);

		// Build param string
		String paramString = "";
		for (Object oKey : keys)
		{
			String key = oKey.toString();
			String value = values.get(key);
			paramString += key + "=" + value;
		}
		paramString += secret;
		String hashed = md5(paramString);
		
		return hashed.equals(params.getFirst("fb_sig"));
	}
	
	private boolean validateAuthtoken(String authToken)
	{
		String apiKey = "6c028b61ae2d51b43e8582420b8a75be";
		String secret = "8c7bd765ef219a7bea8132c03dbe2892";
		
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(FACEBOOK_REST_URL);
		post.addParameter("api_key", apiKey);
		post.addParameter("auth_token", authToken);
		post.addParameter("method", "auth.getSession");
		post.addParameter("v", "1.0");
		
		// Add MD5 hash
		String params = "";
		for (NameValuePair pair : post.getParameters())
		{
			params += pair.getName() + "=" + pair.getValue();
		}

		params += secret;
		String hashed = md5(params);
		post.addParameter("sig", hashed);
		
		try 
		{
			int result = client.executeMethod(post);
			
			if (result == 200)
			{
				String response = post.getResponseBodyAsString();
				if (response.indexOf("<auth_getSession") > -1)
					return true;
			}
		} 
		catch (HttpException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			post.releaseConnection();
		} 
		
		return false;
	}

	private String md5(String value)
	{
		MessageDigest md;
		String hashed = null;
		try 
		{
			md = MessageDigest.getInstance("MD5");
	        md.update(value.getBytes());
	        byte keyB[] = md.digest();
	        hashed = new BigInteger(1, keyB).toString(16);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return hashed;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void userAuthorizeApp(String facebookAppId, String uid) {
		subscriptionManager.registerFacebookFollower(uid, facebookAppId);
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void userDeauthorizeApp(String facebookAppId, String uid) {
		subscriptionManager.removeFacebookFollower(uid, facebookAppId);
	}

}
