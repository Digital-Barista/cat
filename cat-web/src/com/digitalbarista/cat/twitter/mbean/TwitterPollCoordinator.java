package com.digitalbarista.cat.twitter.mbean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TwitterPollCoordinator implements TwitterPollCoordinatorMBean, ApplicationContextAware {

	private Map<String,TwitterAccountPollManager> accountManagers = new HashMap<String,TwitterAccountPollManager>();
	private String cfName = "java:/JmsXA";
	private String destName = "cat/messaging/Events";
	private String twitterSendDestName = "cat/messaging/TwitterOutgoing";
	private ApplicationContext ctx;
		
	private Logger log = LogManager.getLogger(TwitterPollCoordinator.class);
	
	@Override
	public int checkMessages(String account) {
		return new DirectMessageCheckWorker(ctx,accountManagers.get(account)).call();
	}

	@Override
	public String sendMessage(String account) {
		return new SendDirectMessageWorker(ctx,accountManagers.get(account)).call();
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		try {
			MBeanServer server = MBeanServerLocator.locateJBoss();
			try
			{
				server.registerMBean(this, new ObjectName("dbi.config:service=TwitterPollCoordinator"));
			}catch(InstanceAlreadyExistsException e)
			{
				server.unregisterMBean(new ObjectName("dbi.config:service=TwitterPollCoordinator"));
				server.registerMBean(this, new ObjectName("dbi.config:service=TwitterPollCoordinator"));
			}

			Map<String,String> accountList = new TwitterAccountRefresher(ctx).call();
			Set<String> toBeRemoved = new HashSet<String>(accountManagers.keySet());
			toBeRemoved.removeAll(accountList.keySet());
			for(String account : toBeRemoved)
				accountManagers.remove(account);
			for(Map.Entry<String, String> entry : accountList.entrySet())
			{
				if(!accountManagers.containsKey(entry.getKey()))
					accountManagers.put(entry.getKey(), new TwitterAccountPollManager(entry.getKey(),entry.getValue(),ctx));
			}
		} catch (Exception e)
		{
			log.error("Couldn't register Twitter Poll Coordinator MBean",e);
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
}
