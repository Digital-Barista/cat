package com.digitalbarista.cat.ejb.session;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RunAs;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactInfo;
import com.digitalbarista.cat.business.FacebookApp;
import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.business.FacebookTrackingInfo;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactInfoDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.ContactTagLinkDO;
import com.digitalbarista.cat.data.ContactTagType;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.FacebookAppDO;
import com.digitalbarista.cat.data.FacebookMessageDO;
import com.digitalbarista.cat.exception.FacebookManagerException;
import com.digitalbarista.cat.message.event.CATEvent;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.JsonMapper;
import com.restfb.Parameter;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchRequest.BatchRequestBuilder;
import com.restfb.batch.BatchResponse;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class FacebookManagerImpl
 */
@Component
@Transactional(propagation= Propagation.REQUIRED)
@RunAs("admin")
public class FacebookManager
{
	private final static int APP_TOKEN_EXPIRE_SECONDS = 3600; // Not sure what the
																														// expiry is, so
																														// using 1 hour

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

	private static final String SIGN_ALGORITHM = "HMACSHA256";

	private Logger logger = LogManager.getLogger(getClass());

  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private UserManager userManager;

  @Autowired
  private EventManager eventManager;

  @Autowired
  private SubscriptionManager subscriptionManager;

	private String currentAppAccessToken;
	private Date appAccessTokenObtained;

  public List<FacebookMessage> getMessages(String appName, String uid, String signedRequest, HttpServletRequest ui) throws FacebookManagerException
	{
		// Try old way of validating first
		if (!checkAuthorizationByQuerystring(ui))
		{
			// Check oauth
			checkAuthorization(appName, signedRequest);
		}

		// Require facebook user ID
		if (uid == null || uid.length() == 0)
		{
			throw new FacebookManagerException("Could not find facebook user ID");
		}

		// Find app
		FacebookApp app = findFacebookAppByName(appName);

		// Make sure this user is a contact and has appropriate tags
		updateContactInfo(appName, uid, ui);

		List<FacebookMessage> ret = new ArrayList<FacebookMessage>();

		Criteria crit = sf.getCurrentSession().createCriteria(FacebookMessageDO.class);
		crit.add(Restrictions.eq("facebookUID", uid));
		crit.add(Restrictions.eq("facebookAppName", appName));
		crit.addOrder(Order.desc("createDate"));

		for (FacebookMessageDO messageDO : (List<FacebookMessageDO>) crit.list())
		{
			FacebookMessage message = new FacebookMessage();
			message.copyFrom(messageDO);
			ret.add(message);
		}

		return ret;
	}

	public void delete(Integer facebookMessageId, String signedRequest, HttpServletRequest ui) throws FacebookManagerException
	{

		FacebookMessageDO message = (FacebookMessageDO) sf.getCurrentSession().get(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			// Try old way of validating first
			if (!checkAuthorizationByQuerystring(ui))
			{
				// Check oauth
				checkAuthorization(message.getFacebookAppName(), signedRequest);
			}

			sf.getCurrentSession().delete(message);

			// Update message counter
			updateMessageCounter(message.getFacebookAppName(), message.getFacebookUID());
		}

	}

	public FacebookMessage respond(Integer facebookMessageId, String response, String uid, String signedRequest, HttpServletRequest ui) throws FacebookManagerException
	{

		FacebookMessage ret = null;

		FacebookMessageDO message = (FacebookMessageDO) sf.getCurrentSession().get(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			// Try old way of validating first
			if (!checkAuthorizationByQuerystring(ui))
			{
				// Check oauth
				checkAuthorization(message.getFacebookAppName(), signedRequest);
			}

			// If the response is valid update the message
			if (message.getMetadata().indexOf(response) > -1)
				message.setResponse(response);

			eventManager.queueEvent(CATEvent.buildIncomingFacebookEvent(message.getFacebookUID(), message.getFacebookAppName(), response,
			    message.getFacebookUID()));

			ret = new FacebookMessage();
			ret.copyFrom(message);

			// Update message counter
			updateMessageCounter(message.getFacebookAppName(), uid);
		}

		return ret;
	}

	public void updateMessageCounter(final String appName, final String uid, final Integer count)
	{
		final FacebookApp app = findFacebookAppByName(appName);

		final Map<String, String> params = new HashMap<String, String>();
		params.put("method", "dashboard.setCount");
		params.put("api_key", app.getApiKey());
		params.put("v", "1.0");
		params.put("call_id", Long.toString(Calendar.getInstance().getTime().getTime()));
		params.put("count", count.toString());
		params.put("uid", uid);

		Thread updater = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					callFacebookMethod(app.getSecret(), params);
				}
				catch (FacebookManagerException e)
				{
					logger.error("Error updating counter for App: " + appName + ", UID: " + uid, e);
				}
			}
		});
		updater.start();
	}

	public void updateMessageCounter(String appName, String uid)
	{
		FacebookApp app = findFacebookAppByName(appName);

		Criteria crit = sf.getCurrentSession().createCriteria(FacebookMessageDO.class);
		crit.add(Restrictions.eq("facebookUID", uid));
		crit.add(Restrictions.eq("facebookAppName", app.getAppName()));
		crit.setProjection(Projections.rowCount());

		Integer count = (Integer) crit.uniqueResult();

		updateMessageCounter(appName, uid, count);
	}

	/**
	 * Parse the signed request from facebook. If the signature is invalid return
	 * null;
	 */
	private JSONObject decodeSignedRequest(String appName, String signedRequest) throws FacebookManagerException
	{
		JSONObject ret = null;

		if (signedRequest != null)
		{
			/* split the string into signature and payload */
			int idx = signedRequest.indexOf(".");
			byte[] sig = new Base64(true).decode(signedRequest.substring(0, idx).getBytes());
			String rawpayload = signedRequest.substring(idx + 1);
			String payload = new String(new Base64(true).decode(rawpayload));

			/* parse the JSON payload and do the signature check */
			try
			{
				ret = new JSONObject(payload);

				/* check if it is HMAC-SHA256 */
				if (!ret.getString("algorithm").equals("HMAC-SHA256"))
				{
					/*
					 * note that this follows facebooks example, as published on
					 * 2010-07-21 (I wonder when this will break)
					 */
					throw new FacebookManagerException("Unexpected hash algorithm " + ret.getString("algorithm"));
				}

				// Look up facebook app
				FacebookApp app = findFacebookAppByName(appName);

				SecretKeySpec secretKeySpec = new SecretKeySpec(app.getSecret().getBytes(), SIGN_ALGORITHM);
				Mac mac = Mac.getInstance(SIGN_ALGORITHM);
				mac.init(secretKeySpec);
				byte[] mysig = mac.doFinal(rawpayload.getBytes());
				if (!Arrays.equals(mysig, sig))
				{
					ret = null;
				}

			}
			catch (Exception e)
			{
				throw new FacebookManagerException("Could not decode facebook signed request: " + payload, e);
			}
		}
		return ret;
	}

	boolean checkAuthorizationByQuerystring(HttpServletRequest req)
	{
		Map<String, String[]> params = req.getParameterMap();
		return validateSignature(params);
	}

	private boolean checkAuthorization(String appName, String signedRequest) throws FacebookManagerException
	{
		if (decodeSignedRequest(appName, signedRequest) == null)
		{
			throw new FacebookManagerException("Could not authenticate");
		}
		return true;
	}

	/**
	 * Deprecated method of validating a signature based on querystring parameters
	 * 
	 * @param params
	 * @return
	 */
	private boolean validateSignature(Map<String, String[]> params)
	{
		// If the FB signature parameter isn't present this call isn't valid
		if (params == null || !params.containsKey(FACEBOOK_PARAM_APP_ID) || !params.containsKey(FACEBOOK_PARAM_SIGNATURE))
		{
			return false;
		}

		// Get facebook app
    String[] apps = params.get(FACEBOOK_PARAM_APP_ID);
		FacebookAppDO app=null;
    if(apps==null || apps.length==0) 
      app = findFacebookApp(apps[0]);
		if (app == null)
			return false;

		// Build map of proper names to sort
		Map<String, String> values = new HashMap<String, String>();
		for (String key : params.keySet())
		{
			if (key.indexOf(FACEBOOK_PARAM_PREFIX) == 0)
			{
				String[] value = params.get(key);
				values.put(key.substring(FACEBOOK_PARAM_PREFIX.length()), value[0]);
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
		String[] sig = params.get(FACEBOOK_PARAM_SIGNATURE);
		return hashed.equals(sig[0]);
	}

	private FacebookAppDO findFacebookApp(String appId)
	{
		// Look up secret by facebook app id (NOT our facebook_api_id which is the
		// name of the application)
		Criteria crit = sf.getCurrentSession().createCriteria(FacebookAppDO.class);
		crit.add(Restrictions.eq("id", appId));
		FacebookAppDO app = (FacebookAppDO) crit.uniqueResult();

		// The app should exist
		if (app == null)
		{
			logger.error("No facebook app found for App ID: " + appId);
		}

		return app;
	}

	public FacebookApp findFacebookAppByName(String applicationName)
	{
		FacebookApp ret = null;

		// Look up secret by facebook application name (this is the facebook_api_id)
		Criteria crit = sf.getCurrentSession().createCriteria(FacebookAppDO.class);
		crit.add(Restrictions.eq("appName", applicationName));
		FacebookAppDO app = (FacebookAppDO) crit.uniqueResult();

		if (app != null)
		{
			ret = new FacebookApp();
			ret.copyFrom(app);
		}

		return ret;
	}

	/**
	 * Check the UID to see if it is a facebook contact adding it if necessary. If
	 * "tags" are specified in the URL look them up and add them to the contact,
	 * creating new tags if necessary.
	 * 
	 * @param ui
	 */
	private void updateContactInfo(String appName, String uid, HttpServletRequest req)
	{
		try
		{
			// Lookup facebook app
			if (appName != null)
			{
				FacebookApp app = findFacebookAppByName(appName);
				if (app != null)
				{
					// Lookup contact by UID
					if (uid != null)
					{
						Criteria crit = sf.getCurrentSession().createCriteria(ContactDO.class);
						crit.add(Restrictions.eq("address", uid));
						crit.add(Restrictions.eq("type", EntryPointType.Facebook));
						crit.add(Restrictions.eq("client.primaryKey", app.getClientId()));
						ContactDO contact = (ContactDO) crit.uniqueResult();

						// Get app client
						ClientDO appClient = (ClientDO) sf.getCurrentSession().get(ClientDO.class, app.getClientId());

						// Create contact if it doesn't exist
						if (contact == null)
						{
							contact = new ContactDO();
							contact.setAddress(uid);
							contact.setClient(appClient);
							contact.setCreateDate(Calendar.getInstance());
							contact.setType(EntryPointType.Facebook);
							sf.getCurrentSession().persist(contact);

							crit = sf.getCurrentSession().createCriteria(CampaignInfoDO.class);
							crit.add(Restrictions.eq("entryType", EntryPointType.Facebook));
							crit.add(Restrictions.eq("entryAddress", app.getAppName()));
							crit.add(Restrictions.eq("name", CampaignInfoDO.KEY_AUTO_START_NODE_UID));
							crit.createAlias("campaign", "c");
							crit.add(Restrictions.eq("c.status", CampaignStatus.Active));
							CampaignInfoDO cInfo = (CampaignInfoDO) crit.uniqueResult();

							if (cInfo == null)
								return;

							HashSet<String> addresses = new HashSet<String>();
							addresses.add(uid);
							subscriptionManager.subscribeToEntryPoint(addresses, cInfo.getValue(), EntryPointType.Facebook);
						}
						else
						{
							subscriptionManager.unBlacklistAddressForEntryPoint(uid, EntryPointType.Facebook, app.getAppName());
						}

						// Lookup tags from query string
						String[] tagList = ((Map<String,String[]>)req.getParameterMap()).get(CONTACT_TAGS);
						if (tagList != null && tagList.length>0)
						{
							String[] tags = tagList[0].split(",");
							for (String tag : tags)
							{
								ContactTagDO contactTag = findTag(tag.trim(), appClient);
								if (contact.findLink(contactTag) == null)
								{
									ContactTagLinkDO ctldo = new ContactTagLinkDO();
									ctldo.setContact(contact);
									ctldo.setTag(contactTag);
									ctldo.setInitialTagDate(new Date());
									contact.getContactTags().add(ctldo);
									sf.getCurrentSession().persist(ctldo);
								}
							}

							// Save any tag changes
							sf.getCurrentSession().persist(contact);
						}
					}
				}
			}
		}
		// Log failure, but hide the exception
		catch (Exception e)
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
		Criteria crit = sf.getCurrentSession().createCriteria(ContactTagDO.class);
		crit.add(Restrictions.eq("client.id", client.getPrimaryKey()));
		crit.add(Restrictions.eq("tag", tag));
		crit.add(Restrictions.eq("type", ContactTagType.USER));

		ContactTagDO contactTag = (ContactTagDO) crit.uniqueResult();
		if (contactTag == null)
		{
			contactTag = new ContactTagDO();
			contactTag.setClient(client);
			contactTag.setTag(tag);
			contactTag.setType(ContactTagType.USER);
			sf.getCurrentSession().persist(contactTag);
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

			if (result != null && result.getStatusLine().getStatusCode() == 200)
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
			// hashed = new BigInteger(1, data).toString(16);

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

	public void userAuthorizeApp(String appName, String uid)
	{
		subscriptionManager.registerFacebookFollower(uid, appName);
	}

  public void userDeauthorizeApp(String appName, String uid)
	{
		subscriptionManager.removeFacebookFollower(uid, appName);
	}

	public Boolean isUserAllowingApp(String facebookUID, String appName) throws FacebookManagerException
	{
		Boolean ret = true;
		String token = getFacebookAppAccessToken(appName);
		if (token == null)
		{
			throw new FacebookManagerException("Could not retrieve access token for facebook app ID: " + appName);
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

			URI uri = URIUtils.createURI(FACEBOOK_REST_API_PROTOCAL, FACEBOOK_REST_API_DOMAIN, -1, "/method/" + method,
			    URLEncodedUtils.format(parameters, "UTF-8"), null);
			HttpGet get = new HttpGet(uri);

			HttpResponse result = client.execute(get);
			String messageXML = null;

			if (result != null && result.getStatusLine().getStatusCode() == 200)
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

	private String getFacebookAppAccessToken(String appName)
	{
		// Expire token after APP_TOKEN_EXPIRE_SECONDS until we figure out
		// if these actually ever expire (default is 2 hours for USER access
		// tokens, but no mention of APP access tokens)
		if (appAccessTokenObtained != null)
		{
			Date now = new Date();
			if ((now.getTime() - appAccessTokenObtained.getTime())/1000 > APP_TOKEN_EXPIRE_SECONDS)
			{
				logger.debug("App access token expired and refreshing after " + APP_TOKEN_EXPIRE_SECONDS + " seconds");
				currentAppAccessToken = null;
			}
		}

		if (currentAppAccessToken == null)
		{
			try
			{
				// Get facebook app
				FacebookAppDO appDO = (FacebookAppDO)sf.getCurrentSession().get(FacebookAppDO.class, appName);
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
						if (parts.length == 2 && parts[0].equals("access_token"))
						{
							currentAppAccessToken = parts[1];
							appAccessTokenObtained = new Date();
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
			catch (Exception e)
			{
				logger.error("Error trying to retrieve access token", e);
			}
		}

		return currentAppAccessToken;
	}

	private JSONObject callGraphAPI(String uid, String accessToken)
	{
		JSONObject ret = null;

		try
		{
			HttpClient client = new DefaultHttpClient();

			String url = FACEBOOK_GRAPH_API_URL + "/" + uid;
			if (accessToken != null)
			{
				url += "?access_token=" + URLEncoder.encode(accessToken);
			}

			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);

			if (response.getStatusLine().getStatusCode() == 200)
			{
				String result = EntityUtils.toString(response.getEntity());
				ret = new JSONObject(result);
			}
		}
		catch (Exception e)
		{
			logger.error("Failed tyring to get call graph API", e);
		}
		return ret;
	}

	public List<ContactInfo> updateProfileInformation(Contact contact)
	{
		List<ContactInfo> ret = new ArrayList<ContactInfo>();

		/*
		 * Where our apps require more permissions we can get more information by
		 * adding the access token for the app. Currently there is no point.
		 */
		String token = null;
		// // Get an app ID for the user
		// Criteria crit = sf.getCurrentSession().createCriteria(FacebookMessageDO.class);
		// crit.add(Restrictions.eq("facebookUID", uid));
		// crit.setProjection(Projections.distinct(Projections.property("appName")));
		// List<String> appIds = (List<String>)crit.list();
		//
		// if (appIds.size() > 0)
		// {
		// String appId = appIds.get(0);
		// token = getFacebookAppAccessToken(appId);
		// }

		// Call graph API to get profile info
		JSONObject result = callGraphAPI(contact.getAddress(), token);

		if (result != null)
		{
			ContactDO cDO = (ContactDO) sf.getCurrentSession().get(ContactDO.class, contact.getContactId());

			try
			{
				for (Iterator iterator = result.keys(); iterator.hasNext();)
				{
					String key = (String) iterator.next();

					// Update existing keys
					ContactInfoDO contactInfo = null;
					for (ContactInfoDO ciDO : cDO.getContactInfos())
					{
						if (ciDO.getName().equals(key))
						{
							contactInfo = ciDO;
							break;
						}
					}

					if (contactInfo == null)
					{
						contactInfo = new ContactInfoDO();
						contactInfo.setContact(cDO);
						contactInfo.setName(key);
					}

					// Update value
					contactInfo.setValue(result.getString(key));
					sf.getCurrentSession().persist(contactInfo);

					// Add the DTO
					ContactInfo info = new ContactInfo();
					info.copyFrom(contactInfo);
					ret.add(info);
				}
			}
			catch (JSONException e)
			{
				logger.error("Failed parsing graph API JSON response", e);
			}
		}

		return ret;
	}

	public FacebookTrackingInfo getFacebookTrackingInfo(HttpServletRequest request)
	{
		FacebookTrackingInfo ret = new FacebookTrackingInfo();

		if (request != null)
		{
			ret.setAppName(request.getParameter("app_id"));
			String signedRequest = request.getParameter("signed_request");

			if (signedRequest != null)
			{
				JSONObject decoded;
				try
				{
					decoded = decodeSignedRequest(ret.getAppName(), signedRequest);
					ret.setFacebookUserId(decoded.optString("user_id"));
				}
				catch (FacebookManagerException e)
				{
					logger.error("Error decoding facebook response", e);
				}
			}

		}

		return ret;
	}

	/**
	 * Sends a message from an app to a list of users specified by UID.
	 * 
	 * @param facebookUIDs List of facebook user IDs to send to
	 * @param appName Name of the facebook app
	 * @param message Message to send to facebook
	 * 
	 * @return List of facebook user IDs that FAILED to get the message
	 */
	public List<String> sendAppRequest(List<String> facebookUIDs, String appName, String message) throws FacebookManagerException
	{
		List<String> ret = new ArrayList<String>(facebookUIDs);

		// API only allows 50 requests in batches
		if (facebookUIDs.size() > 50)
		{
			throw new FacebookManagerException("Invalid number of facebook UIDs. Max is 50");
		}

		// Get the APP access token
		String token = getFacebookAppAccessToken(appName);

		if (token != null)
		{
			try
			{

				// String uids = "";
				// for (String uid : facebookUIDs)
				// {
				// if (uids.length() > 0)
				// {
				// uids += ",";
				// }
				// uids += uid;
				// }
				// AppRequestType result = fbClient.publish("apprequests",
				// AppRequestType.class,
				// Parameter.with("message", message),
				// Parameter.with("ids", uids));

				// Build a batch request
				List<BatchRequest> commands = new ArrayList<BatchRequest>();
				for (String facebookUID : facebookUIDs)
				{
					BatchRequest br = new BatchRequestBuilder(facebookUID + "/apprequests").parameters(Parameter.with("method", "post"),
					    Parameter.with("message", message)).build();
					commands.add(br);
				}

				FacebookClient fbClient = new DefaultFacebookClient(token);
				List<BatchResponse> batchResponses = fbClient.executeBatch(commands, new ArrayList<BinaryAttachment>());

				// Decode responses
				JsonMapper jsonMapper = new DefaultJsonMapper();
				for (BatchResponse br : batchResponses)
				{
					AppRequestType r = jsonMapper.toJavaObject(br.getBody(), AppRequestType.class);
					if (r.getTo() != null)
					{
						for (String to : r.getTo())
						{
							ret.remove(to);
						}
					}
				}

			}
			catch (Exception e)
			{
				logger.error("Error decoding facebook response", e);
			}
		}
		return ret;
	}

	/**
	 * Sends a message from an app to a list of users specified by UID.
	 * 
	 * @param facebookUIDs List of facebook user IDs to send to
	 * @param appName Name of the facebook app
	 * @param message Message to send to facebook
	 * 
	 * @return List of facebook user IDs that FAILED to get the message
	 */
	public boolean sendNotification(String facebookUID, String appName, String message) throws FacebookManagerException
	{
		// Get the APP access token
		String token = getFacebookAppAccessToken(appName);

		if (token != null)
		{
			try
			{
				FacebookClient fbClient = new DefaultFacebookClient(token);
				NotificationType result = fbClient.publish(facebookUID + "/notifications",
				NotificationType.class,
				Parameter.with("template", message));
			}
			catch (Exception e)
			{
				logger.error("Error decoding facebook response", e);
                                return false;
			}
		}
		return true;
	}

	public static class AppRequestType
	{
		@Facebook
		private String error;
		
		@Facebook
		private String request;

		@Facebook
		private List<String> to;
		

		public String getRequest()
		{
			return request;
		}

		public void setRequest(String request)
		{
			this.request = request;
		}

		public List<String> getTo()
		{
			return to;
		}

		public void setTo(List<String> to)
		{
			this.to = to;
		}

		public String getError()
                {
                    return error;
                }

                public void setError(String error)
                {
                    this.error = error;
                }

	}
        
	public static class NotificationType
	{
		@Facebook
		private boolean success;
		
	}
}
