package com.digitalbarista.cat.twitter.mbean;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.http.HttpParameters;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;

@Service(objectName="dbi.config:service=DBITwitterPollerService")
@Management(TwitterPollCoordinator.class)
public class TwitterPollCoordinatorImpl implements TwitterPollCoordinator {

	private Map<String,TwitterAccountPollManager> accountManagers = new HashMap<String,TwitterAccountPollManager>();
	private String cfName = "java:/JmsXA";
	private String destName = "cat/messaging/Events";
	private String twitterSendDestName = "cat/messaging/TwitterOutgoing";
		
	private Logger log = LogManager.getLogger(TwitterPollCoordinatorImpl.class);
	
	@Override
	public int checkMessages(String account) {
		return new DirectMessageCheckWorker(accountManagers.get(account)).call();
	}

	@Override
	public String sendMessage(String account) {
		return new SendDirectMessageWorker(accountManagers.get(account)).call();
	}

	@Override
	public void startSingleton() {
		log.info("Starting Twitter Poller as HA-Singleton");
		refreshTwitterAccounts();
	}

	@Override
	public void stopSingleton() {
		log.info("Stopping Twitter Poller as HA-Singleton");
		stop();
	}

	@Override
	public void manualStart()
	{
		log.info("Manually Starting Twitter Poller");
		refreshTwitterAccounts();
	}

	@Override
	public void start()
	{
		log.info("Automated Twitter Poller start.  Waiting for singleton startup or manual startup.");
	}

	@Override
	public void stop()
	{
		log.info("Stopping Twitter Poller");
		for(TwitterAccountPollManager manager : accountManagers.values())
		{
			manager.stopPolling();
		}
	}
	
	public String getCfName() {
		return cfName;
	}

	public void setCfName(String cfName) {
		this.cfName = cfName;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}

	@Override
	public void deleteMesage(String account, long id) {
	}

	public String getTwitterSendDestName() {
		return twitterSendDestName;
	}

	public void setTwitterSendDestName(String twitterSendDestName) {
		this.twitterSendDestName = twitterSendDestName;
	}

	@Override
	public boolean startPolling(String account) {
		if(accountManagers.containsKey(account))
		{
			return accountManagers.get(account).startPolling();
		} else {
			return false;
		}
	}

	@Override
	public boolean stopAllPolling(String account) {
		if(accountManagers.containsKey(account))
		{
			return accountManagers.get(account).stopPolling();
		} else {
			return false;
		}
	}

	@Override
	public String refreshTwitterAccounts() {
		try
		{
			Map<String,String> accountList = new TwitterAccountRefresher().call();
			Set<String> toBeRemoved = new HashSet<String>(accountManagers.keySet());
			toBeRemoved.removeAll(accountList.keySet());
			int added=0;
			for(String account : toBeRemoved)
			{
				accountManagers.remove(account).stopPolling();
			}
			for(Map.Entry<String, String> entry : accountList.entrySet())
			{
				if(!accountManagers.containsKey(entry.getKey()))
				{
					accountManagers.put(entry.getKey(), new TwitterAccountPollManager(entry.getKey(),entry.getValue()));
					added++;
				}
			}
			return "Success - removed "+toBeRemoved.size()+", added "+added;
		}
		catch(Exception e)
		{
			return "Unable to refresh twitter accounts: "+e.getMessage()+": check logs.";
		}
	}

	@Override
	public String acquireRequestToken() {
		
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(APP_TOKEN,APP_SECRET);
		
		DefaultHttpClient client = null;
		HttpGet get = null;
		
		try
		{
			client = new DefaultHttpClient();
			
			get = new HttpGet("http://twitter.com/oauth/request_token");

//			Map<String,String> params = new HashMap<String,String>();
//			params.put("oauth_consumer_key", appKey);
//			params.put("oauth_signature_method","HMAC-SHA1");
//			params.put("oauth_timestamp", ""+(System.currentTimeMillis()/1000));
//			params.put("oauth_nonce", ""+System.nanoTime());			
//			params.put("oauth_signature", OAuthHasher.hashMe("GET", "http://twitter.com/oauth/request_token", params, appSecret, null));			
//			
//			StringBuffer authHeader = new StringBuffer();
//			authHeader.append("OAuth realm=\"http://twitter.com/\"");
//			for(Map.Entry<String, String> entry : params.entrySet())
//				authHeader.append(","+entry.getKey()+"=\""+OAuthHasher.percentEncode(entry.getValue())+"\"");
//			
//			get.addRequestHeader("Authorization", authHeader.toString());

			consumer.sign(get);
			
			HttpResponse response = client.execute(get);
			
			StringBuffer ret = new StringBuffer();
			ret.append("<h1>Status&nbsp;:&nbsp;"+response.getStatusLine().getStatusCode()+"<br/><br/>");
			ret.append("<h1>Headers</h1><br/>");
			for(Header header : response.getAllHeaders())
				ret.append("<b>"+header.getName()+"</b>&nbsp;:&nbsp;"+header.getValue()+"<br/>");
			ret.append("<br/><br/><h1>Body</h1><br/>");
			InputStream in = response.getEntity().getContent();
			byte[] buf = new byte[1024];
			int size=-1;
			do
			{
				size = in.read(buf);
				if(size==-1) continue;
				ret.append(new String(buf,0,size));
			}while(size>=0);
			ret.append("<br/><br/>");
			return ret.toString();
			
		}
		catch(Exception e)
		{
			log.error("Trouble requesting a request token.",e);
			return null;
		}
	}

	@Override
	public String retrieveAccessToken(String requestToken, String requestSecret, String pin) {
		
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(APP_TOKEN,APP_SECRET);
		consumer.setTokenWithSecret(requestToken, requestSecret);
		HttpParameters params = new HttpParameters();
		params.put("oauth_verifier", pin);
		consumer.setAdditionalParameters(params);
		
		DefaultHttpClient client = null;
		HttpGet get = null;
		
		try
		{
			client = new DefaultHttpClient();
			
			get = new HttpGet("http://twitter.com/oauth/access_token");
			
//			Map<String,String> params = new HashMap<String,String>();
//			params.put("oauth_consumer_key", appKey);
//			params.put("oauth_token",requestToken);
//			params.put("oauth_signature_method","HMAC-SHA1");
//			params.put("oauth_timestamp", ""+(System.currentTimeMillis()/1000));
//			params.put("oauth_nonce", ""+System.nanoTime());
//			params.put("oauth_verifier", pin);
//			params.put("oauth_signature", OAuthHasher.hashMe("GET", "http://twitter.com/oauth/access_token", params, appSecret, null));			
//			
//			StringBuffer authHeader = new StringBuffer();
//			authHeader.append("OAuth realm=\"http://twitter.com/\"");
//			for(Map.Entry<String, String> entry : params.entrySet())
//				authHeader.append(","+entry.getKey()+"=\""+OAuthHasher.percentEncode(entry.getValue())+"\"");
//			
//			get.addRequestHeader("Authorization", authHeader.toString());

			consumer.sign(get);
			HttpResponse response = client.execute(get);
			
			StringBuffer ret = new StringBuffer();
			ret.append("<h1>Status&nbsp;:&nbsp;"+response+"<br/><br/>");
			ret.append("<h1>Headers</h1><br/>");
			for(Header header : response.getAllHeaders())
				ret.append("<b>"+header.getName()+"</b>&nbsp;:&nbsp;"+header.getValue()+"<br/>");
			ret.append("<br/><br/><h1>Body</h1><br/>");
			InputStream in = response.getEntity().getContent();
			byte[] buf = new byte[1024];
			int size=-1;
			do
			{
				size = in.read(buf);
				if(size==-1) continue;
				ret.append(new String(buf,0,size));
			}while(size>=0);
			ret.append("<br/><br/>");
			return ret.toString();
			
		}
		catch(Exception e)
		{
			log.error("Trouble requesting a request token.",e);
			return null;
		}
	}
}
