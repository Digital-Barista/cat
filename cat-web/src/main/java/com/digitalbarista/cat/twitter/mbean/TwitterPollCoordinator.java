package com.digitalbarista.cat.twitter.mbean;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.http.HttpParameters;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.EntryPointType;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/twitter",
                produces="application/xml")
public class TwitterPollCoordinator {

	public static final String APP_TOKEN="ypeczxLakmzTBNIDAjNXg";
	public static final String APP_SECRET="s6aGMDy2JfiMQuZWi0NU4EOpg3L7WkVm9oHOv6EPvkw";
	
	public class QueueInfo
	{
		public int remainingQueries=-1;
		public int remainingUpdates=-1;
		public long lastIncomingDM=-1;
		public long lastIncomingRT=-1;
		public Date queryReset = new Date();
		public Date updateReset = new Date();
		public Queue<String> dmQueue = new ConcurrentLinkedQueue<String>();
		public Queue<String> rtQueue = new ConcurrentLinkedQueue<String>();
	}
	
	private Map<String,String> requestTokens = new HashMap<String,String>();
	
	private Map<String,TwitterAccountPollManager> accountManagers = new HashMap<String,TwitterAccountPollManager>();
	private String cfName = "java:/JmsXA";
	private String destName = "cat/messaging/Events";
	private String twitterSendDestName = "cat/messaging/TwitterOutgoing";

  @Autowired
  private SessionFactory sf;
  
	private Logger log = LogManager.getLogger(TwitterPollCoordinator.class);
	
	public int checkMessages(String account) {
		return new DirectMessageCheckWorker(accountManagers.get(account)).call();
	}

	public String sendMessage(String account) {
		return new SendDirectMessageWorker(accountManagers.get(account)).call();
	}

	public void startSingleton() {
		log.info("Starting Twitter Poller as HA-Singleton");
		refreshTwitterAccounts();
	}

	public void stopSingleton() {
		log.info("Stopping Twitter Poller as HA-Singleton");
		stop();
	}

	public void manualStart()
	{
		log.info("Manually Starting Twitter Poller");
		refreshTwitterAccounts();
	}

	public void start()
	{
		log.info("Automated Twitter Poller start.  Waiting for singleton startup or manual startup.");
	}

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

	public void deleteMesage(String account, long id) {
	}

	public String getTwitterSendDestName() {
		return twitterSendDestName;
	}

	public void setTwitterSendDestName(String twitterSendDestName) {
		this.twitterSendDestName = twitterSendDestName;
	}

	public boolean startPolling(String account) {
		if(accountManagers.containsKey(account))
		{
			return accountManagers.get(account).startPolling();
		} else {
			return false;
		}
	}

	public boolean stopAllPolling(String account) {
		if(accountManagers.containsKey(account))
		{
			return accountManagers.get(account).stopPolling();
		} else {
			return false;
		}
	}

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

	public TokenPair acquireRequestToken(String callbackURL) {
		
		OAuthConsumer consumer = new DefaultOAuthConsumer(APP_TOKEN,APP_SECRET);
		//OAuthConsumer consumer = new CommonsHttpOAuthConsumer(APP_TOKEN,APP_SECRET);
		OAuthProvider provider =  new DefaultOAuthProvider(
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize");
		
		DefaultHttpClient client = null;
		HttpGet get = null;
		
		try
		{
			provider.retrieveRequestToken(consumer, callbackURL);
			TokenPair ret = new TokenPair();
			ret.setToken(consumer.getToken());
			ret.setSecret(consumer.getTokenSecret());
//			client = new DefaultHttpClient();
//			
//			get = new HttpGet("http://api.twitter.com/oauth/request_token");
//
//			if(callbackURL !=null)
//			{
//				HttpParameters params = new HttpParameters();
//				params.put("oauth_callback", callbackURL);
//				consumer.setAdditionalParameters(params);
//			}
//
//			consumer.sign(get);
//			
//			HttpResponse response = client.execute(get);
//			
//			StringBuffer resp = new StringBuffer();
//
//			InputStream in = response.getEntity().getContent();
//			byte[] buf = new byte[1024];
//			int size=-1;
//			do
//			{
//				size = in.read(buf);
//				if(size==-1) continue;
//				resp.append(new String(buf,0,size));
//			}while(size>=0);
//
//			if(response.getStatusLine().getStatusCode()!=200)
//				throw new IllegalStateException("Twitter returned non-okay status code.  code:"+response.getStatusLine().getStatusCode()+" content:"+resp);
//
//			TokenPair ret = new TokenPair();
//			
//			for(String param : resp.toString().split("&"))
//			{
//				String[] kv = param.split("=");
//				if(kv[0].equals("oauth_token"))
//					ret.setToken(kv[1]);
//				else
//					ret.setSecret(kv[1]);
//			}
//			
			requestTokens.put(ret.getToken(), ret.getSecret());
			ret.setSecret(null);
			
			return ret;
		}
		catch(Exception e)
		{
			log.error("Trouble requesting a request token.",e);
			return null;
		}
	}

	public boolean retrieveAccessToken(String requestToken, String verifier) {
		if(!requestTokens.containsKey(requestToken))
			return false;
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(APP_TOKEN,APP_SECRET);
		consumer.setTokenWithSecret(requestToken, requestTokens.get(requestToken));
		HttpParameters params = new HttpParameters();
		params.put("oauth_verifier", verifier);
		consumer.setAdditionalParameters(params);
		
		DefaultHttpClient client = null;
		HttpGet get = null;
		
		try
		{
			client = new DefaultHttpClient();
			
			get = new HttpGet("http://api.twitter.com/oauth/access_token");
			
			consumer.sign(get);
			HttpResponse response = client.execute(get);
			
			StringBuffer resp = new StringBuffer();

			InputStream in = response.getEntity().getContent();
			byte[] buf = new byte[1024];
			int size=-1;
			do
			{
				size = in.read(buf);
				if(size==-1) continue;
				resp.append(new String(buf,0,size));
			}while(size>=0);

			if(response.getStatusLine().getStatusCode()!=200)
				throw new IllegalStateException("Twitter returned non-okay status code.  code:"+response.getStatusLine().getStatusCode()+" content:"+resp);

			TokenPair ret = new TokenPair();
			
			String userId=null;
			String screenName=null;
			
			for(String param : resp.toString().split("&"))
			{
				String[] kv = param.split("=");
				if(kv[0].equals("oauth_token"))
					ret.setToken(kv[1]);
				else if(kv[0].equals("oauth_token_secret"))
					ret.setSecret(kv[1]);
				else if(kv[0].equals("user_id"))
					userId=kv[1];
				else if(kv[0].equals("screen_name"))
					screenName=kv[1];
			}
			
			Criteria crit = sf.getCurrentSession().createCriteria(EntryPointDO.class);
			crit.add(Restrictions.eq("type", EntryPointType.Twitter));
			crit.add(Restrictions.eq("value", screenName));
			EntryPointDO entry = (EntryPointDO)crit.uniqueResult();
			
			entry.setCredentials(ret.getToken()+"|"+ret.getSecret());
			
			return true;
		}
		catch(Exception e)
		{
			log.error("Trouble requesting a request token.",e);
			return false;
		}
	}
}
