package com.digitalbarista.cat.ejb.session;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import bsh.StringUtil;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.data.FacebookAppDO;
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
	private final static String FACEBOOK_PARAM_PREFIX = "fb_sig_";
	private final static String FACEBOOK_PARAM_APP_ID = "fb_sig_app_id";
	private final static String FACEBOOK_PARAM_USER_ID = "uid"; //"fb_sig_user";
	private final static String FACEBOOK_PARAM_SIGNATURE = "fb_sig";
	
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
	public List<FacebookMessage> getMessages(String facebookAppId, UriInfo ui) throws FacebookManagerException 
	{
		// Require valid session
		if (!isAuthorized(ui))
			throw new FacebookManagerException("Could not authenticate");
		
		// Require facebook user ID
		String uid = ui.getQueryParameters().getFirst(FACEBOOK_PARAM_USER_ID);
		if (uid == null ||
			uid.length() == 0)
			throw new FacebookManagerException("Could not find facebook user ID");
		
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
		
		// Update count
		updateMessageCounter(ui.getQueryParameters().getFirst(FACEBOOK_PARAM_APP_ID), uid, ret.size());
		
		return ret;
	}


	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(Integer facebookMessageId, UriInfo ui) throws FacebookManagerException 
	{
		if (!isAuthorized(ui))
			throw new FacebookManagerException("Could not authenticate");
		
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			em.remove(message);
		}
		
		// Update message counter
		updateMessageCounter(ui.getQueryParameters().getFirst(FACEBOOK_PARAM_APP_ID), 
				ui.getQueryParameters().getFirst(FACEBOOK_PARAM_USER_ID));
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FacebookMessage respond(Integer facebookMessageId, String response, UriInfo ui) throws FacebookManagerException 
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

		// Update message counter
		updateMessageCounter(ui.getQueryParameters().getFirst(FACEBOOK_PARAM_APP_ID), 
				ui.getQueryParameters().getFirst(FACEBOOK_PARAM_USER_ID));
		
		return ret;
	}

	public void updateMessageCounter(String appId, String uid, Integer count)
	{
		FacebookAppDO app = findFacebookApp(appId);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", "dashboard.setCount");
		params.put("api_key", app.getApiKey());
		params.put("v", "1.0");
		params.put("call_id", Long.toString(Calendar.getInstance().getTime().getTime()));
		params.put("count", count.toString());
		params.put("uid", uid);
		
		try 
		{
			callFacebookMethod(app.getSecret(), params);
		} 
		catch (FacebookManagerException e) 
		{
			logger.error("Error updating counter for App ID: " + appId + ", UID: " + uid, e);
		}
	}
	
	public void updateMessageCounter(String appId, String uid)
	{
		FacebookAppDO app = findFacebookApp(appId);
		
		Criteria crit = session.createCriteria(FacebookMessageDO.class);
		crit.add(Restrictions.eq("facebookUID", uid));
		crit.add(Restrictions.eq("facebookAppId", app.getFacebookAppId()));
		crit.setProjection(Projections.rowCount());
		
		Integer count = (Integer)crit.uniqueResult();
		
		updateMessageCounter(app.getId(), uid, count);
	}
	
	private boolean isAuthorized(UriInfo ui)
	{
		MultivaluedMap<String, String> params = ui.getQueryParameters();
		return validateSignature(params);
	}
	
	private boolean validateSignature(MultivaluedMap<String, String> params)
	{
		// If the FB signature parameter isn't present this call isn't valid
		if (params == null ||
			!params.containsKey(FACEBOOK_PARAM_APP_ID) ||
			!params.containsKey(FACEBOOK_PARAM_SIGNATURE) )
		{
			logger.error("Parameters missing for validateSignature");
			return false;
		}
		
		// Get facebook app
		FacebookAppDO app = findFacebookApp(params.getFirst(FACEBOOK_PARAM_APP_ID));
		if (app == null)
			return false;
		
		// Build map of proper names to sort
		Map<String, String> values = new HashMap<String, String>();
		for (String key : params.keySet())
		{
			if (key.indexOf(FACEBOOK_PARAM_PREFIX) == 0)
			{
				String value = params.getFirst(key);
				values.put(key.substring(FACEBOOK_PARAM_PREFIX.length()), value);
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
		paramString += app.getSecret();
		String hashed = md5(paramString);
		
		// Compare signature to our hash with our secret
		return hashed.equals(params.getFirst(FACEBOOK_PARAM_SIGNATURE));
	}
	
	private FacebookAppDO findFacebookApp(String appId)
	{
		// Look up secret by facebook app id (NOT our facebook_api_id which is the name of the application)
		Criteria crit = session.createCriteria(FacebookAppDO.class);
		crit.add(Restrictions.eq("id", appId));
		FacebookAppDO app = (FacebookAppDO)crit.uniqueResult();
		
		// The app should exist
		if (app == null)
		{
			logger.error("No facebook app found for App ID: " + appId);
		}
		
		return app;
	}
	
	
	private String callFacebookMethod(String secret, Map<String, String> postParameters) throws FacebookManagerException
	{
		String ret = "";
		
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(FACEBOOK_REST_URL);
		
		// Sort keys
		Object[] keys = postParameters.keySet().toArray();
		Arrays.sort(keys);
		
		// Add MD5 hash
		String params = "";
		for (Object key : keys)
		{
			String value = postParameters.get(key);
			post.addParameter(new NameValuePair(key.toString(), value));
			params += key + "=" + value;
		}

		params += secret;
		String hashed = md5(params);
		post.addParameter("sig", hashed);
		
		try 
		{
			int result = client.executeMethod(post);
			
			if (result == 200)
			{
				ret = post.getResponseBodyAsString();
			}
			else
			{
				throw new FacebookManagerException("Facebook request returned with status code: " + result);
			}
		} 
		catch (HttpException e) 
		{
			throw new FacebookManagerException("Error making facebook request", e);
		} 
		catch (IOException e) 
		{
			throw new FacebookManagerException("Error making facebook request", e);
		} 
		finally 
		{
			post.releaseConnection();
		}
		
		return ret;
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
