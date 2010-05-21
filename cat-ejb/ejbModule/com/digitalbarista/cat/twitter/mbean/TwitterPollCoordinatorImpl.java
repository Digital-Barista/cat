package com.digitalbarista.cat.twitter.mbean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
}
