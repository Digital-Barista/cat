package com.digitalbarista.cat.ejb.session;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.ContactTagLinkDO;
import com.digitalbarista.cat.data.ContactTagType;
import com.digitalbarista.cat.data.EntryPointType;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class FacebookManagerImpl implements FacebookManager {

	private final static String FACEBOOK_REST_URL = "https://api.facebook.com/restserver.php";
	private final static String FACEBOOK_REST_API_URL = "https://api.facebook.com";
	private final static String FACEBOOK_GRAPH_API_URL = "https://graph.facebook.com";
	private final static String FACEBOOK_ACCESS_TOKEN_URL = FACEBOOK_GRAPH_API_URL + "/oauth/access_token";

	private final static String FACEBOOK_REST_API_PROTOCAL = "https";
	private final static String FACEBOOK_REST_API_DOMAIN = "api.facebook.com";
	
	private final static String FACEBOOK_PARAM_PREFIX = "fb_sig_";
	private final static String FACEBOOK_PARAM_APP_ID = "fb_sig_app_id";
	private final static String FACEBOOK_PARAM_USER_ID = "uid";;
	private final static String FACEBOOK_PARAM_SIGNATURE = "fb_sig";
	private final static String CONTACT_TAGS = "tags";
	
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
		
		// Make sure this user is a contact and has appropriate tags
		updateContactInfo(ui);
		
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
		String sig = params.getFirst(FACEBOOK_PARAM_SIGNATURE);
		return hashed.equals(sig);
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
	
	/**
	 * Check the UID to see if it is a facebook contact adding
	 * it if necessary.  If "tags" are specified in the URL
	 * look them up and add them to the contact, creating new tags
	 * if necessary.
	 * 
	 * @param ui
	 */
	private void updateContactInfo(UriInfo ui)
	{
		try
		{
			// Lookup facebook app
			String appId = ui.getQueryParameters().getFirst(FACEBOOK_PARAM_APP_ID);
			if (appId != null)
			{
				FacebookAppDO app = findFacebookApp(appId);
				if (app != null)
				{
					// Lookup contact by UID
					String uid = ui.getQueryParameters().getFirst(FACEBOOK_PARAM_USER_ID);
					if (uid != null)
					{
						Criteria crit = session.createCriteria(ContactDO.class);
						crit.add(Restrictions.eq("address", uid));
						crit.add(Restrictions.eq("type", EntryPointType.Facebook));
						crit.add(Restrictions.eq("client.primaryKey", app.getClient().getPrimaryKey()));
						ContactDO contact = (ContactDO)crit.uniqueResult();
						
						// Create contact if it doesn't exist
						if (contact == null)
						{
							contact = new ContactDO();
							contact.setAddress(uid);
							contact.setClient(app.getClient());
							contact.setCreateDate(Calendar.getInstance());
							contact.setType(EntryPointType.Facebook);
							em.persist(contact);
							
							crit = session.createCriteria(CampaignInfoDO.class);
							crit.add(Restrictions.eq("entryType", EntryPointType.Facebook));
							crit.add(Restrictions.eq("entryAddress", app.getFacebookAppId()));
							crit.add(Restrictions.eq("name", CampaignInfoDO.KEY_AUTO_START_NODE_UID));
							CampaignInfoDO cInfo = (CampaignInfoDO)crit.uniqueResult();
							
							if(cInfo==null)
								return;
							
							HashSet<String> addresses = new HashSet<String>();
							addresses.add(uid);
							subscriptionManager.subscribeToEntryPoint(addresses,cInfo.getValue(),EntryPointType.Facebook);
						}
						
						// Lookup tags from query string
						String tagList = ui.getQueryParameters().getFirst(CONTACT_TAGS);
						if (tagList != null)
						{
							String[] tags = tagList.split(",");
							for (String tag : tags)
							{
								ContactTagDO contactTag = findTag(tag.trim(), app.getClient());
								if (contact.findLink(contactTag) == null)
								{
									ContactTagLinkDO ctldo = new ContactTagLinkDO();
									ctldo.setContact(contact);
									ctldo.setTag(contactTag);
									ctldo.setInitialTagDate(new Date());
									contact.getContactTags().add(ctldo);
									em.persist(ctldo);
								}
							}
							
							// Save any tag changes
							em.persist(contact);
						}
					}
				}
			}
		}
		// Log failure, but hide the exception
		catch(Exception e)
		{
			logger.error("Error trying to add contact and tags for facebook request", e);
		}
	}
	
	/**
	 * Looks up a contact tag and creates it if not found.
	 * 
	 * @param tag
	 * @param type
	 * @param client
	 * @return
	 */
	private ContactTagDO findTag(String tag, ClientDO client)
	{
		Criteria crit = session.createCriteria(ContactTagDO.class);
		crit.add(Restrictions.eq("client.id", client.getPrimaryKey()));
		crit.add(Restrictions.eq("tag", tag));
		crit.add(Restrictions.eq("type", ContactTagType.USER));

		ContactTagDO contactTag = (ContactTagDO)crit.uniqueResult();
		if (contactTag == null)
		{
			contactTag = new ContactTagDO();
			contactTag.setClient(client);
			contactTag.setTag(tag);
			contactTag.setType(ContactTagType.USER);
			em.persist(contactTag);
		}
		
		return contactTag;
	}
	
	private String callFacebookMethod(String secret, Map<String, String> postParameters) throws FacebookManagerException
	{
		String ret = "";
		String url = FACEBOOK_REST_URL + "?";
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		// Sort keys
		Object[] keys = postParameters.keySet().toArray();
		Arrays.sort(keys);
		
		// Add MD5 hash
		String params = "";
		for (Object key : keys)
		{
			String value = postParameters.get(key);
			params += key + "=" + value;
			url += "&" + key + "=" + value;
		}

		params += secret;
		String hashed = md5(params);
		
		// Build a the URL with all parameters on it
		url += "&sig=" + hashed;
		HttpGet get = new HttpGet(url);
		
		try 
		{
			HttpResponse result = client.execute(get);
			
			if (result!=null && result.getStatusLine().getStatusCode() == 200)
			{
				ret = EntityUtils.toString(result.getEntity());
			}
			else
			{
				throw new FacebookManagerException("Facebook request returned with status code: " + result.getStatusLine().getStatusCode());
			}
		} 
		catch (IOException e) 
		{
			throw new FacebookManagerException("Error making facebook request", e);
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
			md.reset();
	        md.update(value.getBytes());
	        byte data[] = md.digest();
//	        hashed = new BigInteger(1, data).toString(16);
	        
	        hashed = "";
	        for (byte element : data) 
	        {
	        	hashed += Character.forDigit((element >> 4) & 0xf, 16);
	        	hashed += Character.forDigit(element & 0xf, 16);
	        }

		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return hashed;
	}

	@Override
	public void userAuthorizeApp(String facebookAppId, String uid) {
		subscriptionManager.registerFacebookFollower(uid, facebookAppId);
	}


	@Override
	public void userDeauthorizeApp(String facebookAppId, String signedRequest) {
		//Need to implement sig checking.
		signedRequest = signedRequest.substring(signedRequest.indexOf('.')+1);
		signedRequest = new String(new Base64().decode(signedRequest.getBytes()));
		LogManager.getLogger(FacebookManagerImpl.class).error("Facebook deauth data is '"+signedRequest+"'");
//		subscriptionManager.removeFacebookFollower(uid, facebookAppId);
	}

	@Override
	public Boolean isUserAllowingApp(String facebookUID, String facebookAppId) throws FacebookManagerException
	{
		Boolean ret = true;
		String token = getFacebookAppAccessToken(facebookAppId);
		if (token == null)
		{
			throw new FacebookManagerException("Could not retrieve access token for facebook app ID: " + facebookAppId);
		}
		else
		{
			// Call facebook method
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("uid", facebookUID));
			qparams.add(new BasicNameValuePair("access_token", token));
			
			Document doc = callFacebookMethod("users.isAppUser", qparams);
			
			// Check return flag
			String value = doc.getDocumentElement().getFirstChild().getNodeValue();
			ret = !value.equals("0");
		}
		return ret;
	}
	
	private Document callFacebookMethod(String method, List<NameValuePair> parameters) throws FacebookManagerException
	{
		Document ret = null;
		
		try 
		{
			DefaultHttpClient client = new DefaultHttpClient();
			
			URI uri = URIUtils.createURI(FACEBOOK_REST_API_PROTOCAL, 
					FACEBOOK_REST_API_DOMAIN, 
					-1, 
					"/method/" + method, 
					URLEncodedUtils.format(parameters, "UTF-8"), 
					null);
			HttpGet get = new HttpGet(uri);
			
			
			HttpResponse result = client.execute(get);
			String messageXML = null;
			
			if (result!=null && result.getStatusLine().getStatusCode() == 200)
			{
				messageXML = EntityUtils.toString(result.getEntity());
			}
			else
			{
				throw new FacebookManagerException("Facebook request returned with status code: " + result.getStatusLine().getStatusCode());
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(messageXML));
			Document doc = db.parse(is);
			
			// Check for error response
			if (doc.getElementsByTagName("error_response").getLength() > 0)
			{
				throw new FacebookManagerException("Facebook returned an error: \n" + messageXML);
			}
			ret = doc;
		} 
		catch (IOException e) 
		{
			throw new FacebookManagerException("Error making facebook request", e);
		} 
		catch (URISyntaxException e) 
		{
			throw new FacebookManagerException("Error building facebook URI", e);
		} 
		catch (ParserConfigurationException e) 
		{
			throw new FacebookManagerException("Could not parse facebook response", e);
		} 
		catch (SAXException e) 
		{
			throw new FacebookManagerException("Could not parse facebook response", e);
		} 
		
		return ret;
	}
	
	private String getFacebookAppAccessToken(String facebookAppId)
	{
		String token = null;
		
		try
		{
			// Get facebook app
			FacebookAppDO appDO = em.find(FacebookAppDO.class, facebookAppId);
			if (appDO != null)
			{
				DefaultHttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(FACEBOOK_ACCESS_TOKEN_URL);
				
				List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
				formparams.add(new BasicNameValuePair("type", "client_cred"));
				formparams.add(new BasicNameValuePair("client_id", appDO.getId()));
				formparams.add(new BasicNameValuePair("client_secret", appDO.getSecret()));

				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
				post.setEntity(entity);
				
				HttpResponse result = client.execute(post);
				
				if (result != null)
				{
					String content = EntityUtils.toString(result.getEntity());
					String[] parts = content.split("=");
					if (parts.length == 2 &&
						parts[0].equals("access_token") )
					{
						token = parts[1];
					}
					else
					{
						throw new FacebookManagerException("Error returned from OAuth service: " + content);
					}
				}
				else
				{
					throw new FacebookManagerException("Facebook request returned with status code: " + result.getStatusLine().getStatusCode());
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Error trying to retrieve access token", e);
		}
		
		return token;
	}
}
