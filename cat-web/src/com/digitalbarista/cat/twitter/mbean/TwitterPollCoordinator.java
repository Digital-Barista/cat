package com.digitalbarista.cat.twitter.mbean;

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

	private String cfName = "java:/JmsXA";
	private String destName = "cat/messaging/Events";
	private String twitterSendDestName = "cat/messaging/TwitterOutgoing";
	private ApplicationContext ctx;
		
	private Logger log = LogManager.getLogger(TwitterPollCoordinator.class);
	
	@Override
	public int checkMessages(String account) {
		return new DirectMessageCheckWorker(ctx,account).call();
	}

	@Override
	public String sendMessage() {
		return new SendDirectMessageWorker(ctx).call();
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
		} catch (Exception e)
		{
			log.error("Couldn't register Twitter Poll Coordinator MBean");
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
}
