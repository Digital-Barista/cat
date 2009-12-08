package com.digitalbarista.cat.twitter.mbean;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.springframework.context.ApplicationContext;

public class TwitterAccountPollManager {

	Logger log = LogManager.getLogger("TwitterStats");
	
	public enum SubscribeType
	{
		Subscribe,
		Unsubscribe,
		NoAction
	}
	
	public class SubscribeAction
	{
		private SubscribeType action;
		private Long subscriberId;
	
		public SubscribeAction(SubscribeType act,Long subId)
		{
			action=act;
			subscriberId=subId;
		}

		public SubscribeType getAction() {
			return action;
		}

		public void setAction(SubscribeType action) {
			this.action = action;
		}

		public Long getSubscriberId() {
			return subscriberId;
		}

		public void setSubscriberId(Long subscriberId) {
			this.subscriberId = subscriberId;
		}
	}
	
	private String account;
	private String credentials;
	private Set<Long> friendList=Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
	private Set<Long> followerList=Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
	private Set<Long> needToSubscribe=Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
	private Set<Long> needToUnsubscribe=Collections.newSetFromMap(new ConcurrentHashMap<Long,Boolean>());
	private Date lastFollowChange;
	private Queue<Date> followChangeHistory = new ConcurrentLinkedQueue();
	private Date lastSentMessage;
	private Queue<Date> messageSendHistory = new ConcurrentLinkedQueue();
	private Date lastPollTime=new Date();
	private int remainingQueries=-1;
	private int maxQueries=-1;
	private Date resetTime=new Date();
	private long lowestReadMessage=-1;
	private long highestDeletedMessage=-1;
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
	private Future subscribeTask=null;
	private Future directMessageCheckTask=null;
	private Future sendDirectMessageTask=null;
	private Future friendCheckTask=null;
	private Future followerCheckTask=null;
	private boolean polling=false;
	private ApplicationContext applicationContext;
	
	private DateFormat df()
	{
		return new ISO8601DateFormat();
	}
	
	public TwitterAccountPollManager(String account, String credentials, ApplicationContext appCtx)
	{
		this.account=account;
		this.credentials=credentials;
		this.applicationContext=appCtx;
		log("Created Poll Manager");
		startPolling();
	}
	
	public boolean stopPolling()
	{
		log("Poll STOP requested");
		polling=false;
		if(friendCheckTask!=null && !friendCheckTask.isDone())
			friendCheckTask.cancel(false);
		if(followerCheckTask!=null && !followerCheckTask.isDone())
			followerCheckTask.cancel(false);
		if(directMessageCheckTask!=null && !directMessageCheckTask.isDone())
			directMessageCheckTask.cancel(false);
		if(sendDirectMessageTask!=null && !sendDirectMessageTask.isDone())
			sendDirectMessageTask.cancel(false);
		if(subscribeTask!=null && !subscribeTask.isDone())
			subscribeTask.cancel(false);
		return true;
	}
	
	public boolean startPolling()
	{
		log("poll START requested");
		polling=true;
		friendCheckTask = executor.submit(new FriendCheckWorker(applicationContext,this));
		followerCheckTask = executor.schedule(new FollowerCheckWorker(applicationContext,this), (long)10, TimeUnit.SECONDS);
		directMessageCheckTask = executor.submit(new DirectMessageCheckWorker(applicationContext,this));
		sendDirectMessageTask = executor.submit(new SendDirectMessageWorker(applicationContext,this));		
		return true;
	}
	
	public Date getLastPollTime() {
		return lastPollTime;
	}
	public void setLastPollTime(Date lastPollTime) {
		this.lastPollTime = lastPollTime;
	}
	public int getRemainingQueries() {
		return remainingQueries;
	}
	public void setRemainingQueries(int remainingQueries) {
		this.remainingQueries = remainingQueries;
	}
	public long getLowestReadMessage() {
		return lowestReadMessage;
	}
	public void setLowestReadMessage(long lowestReadMessage) {
		this.lowestReadMessage = lowestReadMessage;
	}
	public long getHighestDeletedMessage() {
		return highestDeletedMessage;
	}
	public void setHighestDeletedMessage(long highestDeletedMessage) {
		this.highestDeletedMessage = highestDeletedMessage;
	}
	public Date getResetTime() {
		return resetTime;
	}
	public void setResetTime(Date resetTime) {
		this.resetTime = resetTime;
	}
	public int getMaxQueries() {
		return maxQueries;
	}
	public void setMaxQueries(int maxQueries) {
		this.maxQueries = maxQueries;
	}
	public Date getLastFollowChange() {
		return lastFollowChange;
	}
	public void setLastFollowChange(Date lastFollowChange) {
		this.lastFollowChange = lastFollowChange;
	}
	public Date getLastSentMessage() {
		return lastSentMessage;
	}
	public void setLastSentMessage(Date lastSentMessage) {
		this.lastSentMessage = lastSentMessage;
	}
	public void addFollowChange(Date changeDate)
	{
		followChangeHistory.add(changeDate);
	}
	public void addMessageSend(Date sendDate)
	{
		messageSendHistory.add(sendDate);
	}
	public void bleedFollowChangeHistoryUntil(Date bleedDate)
	{
		while(!followChangeHistory.isEmpty() && followChangeHistory.peek().before(bleedDate))
			followChangeHistory.remove();
	}
	public void bleedMessageSendHistoryUntil(Date bleedDate)
	{
		while(!messageSendHistory.isEmpty() && messageSendHistory.peek().before(bleedDate))
			messageSendHistory.remove();
	}
	public int countFollowChangeHistory()
	{
		return followChangeHistory.size();
	}
	public int countMessageSendHistory()
	{
		return messageSendHistory.size();
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	String getCredentials() {
		return credentials;
	}
	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}
	public void registerFriendsList(Set<Long> newFriendList)
	{
		log("Friend List Retrieved");
		friendList.addAll(newFriendList);
		friendList.retainAll(newFriendList);
		needToSubscribe.removeAll(newFriendList);
		needToUnsubscribe.retainAll(newFriendList);
	}
	public void registerFollowerList(Set<Long> newFollowerList)
	{
		followerList.addAll(newFollowerList);
		followerList.retainAll(newFollowerList);
		Set<Long> temp = new HashSet<Long>();
		temp.addAll(followerList);
		temp.removeAll(friendList);
		needToSubscribe.addAll(temp);
		needToSubscribe.retainAll(temp);
		temp.clear();
		temp.addAll(friendList);
		temp.removeAll(followerList);
		needToUnsubscribe.addAll(temp);
		needToUnsubscribe.retainAll(temp);
		if(polling)
		{
			subscribeTask = executor.schedule(new ModifySubscriptionsWorker(applicationContext,this), 10, TimeUnit.SECONDS);
			log("Subscribe Task Scheduled:10s(registerFollowerList)");
		}
		if(polling)
		{
			followerCheckTask = executor.schedule(new FollowerCheckWorker(applicationContext,this), 1, TimeUnit.MINUTES);
			log("Follower Check Task Scheduled:1m(registerFollowerList)");
		}
	}
	public void followerCheckFailed()
	{
		if(polling)
		{
			followerCheckTask = executor.schedule(new FollowerCheckWorker(applicationContext,this), 10, TimeUnit.MINUTES);		
			log("Follower Check Task Scheduled:10m(followerCheckFailed)");
		}
	}
	public SubscribeAction getNextSubscribeAction()
	{
		if(((float)friendList.size())/((float)followerList.size())>=1.10 || needToSubscribe.size()==0)
		{
			if(needToUnsubscribe.size()==0)
				return new SubscribeAction(SubscribeType.NoAction,-1l);
			return new SubscribeAction(SubscribeType.Unsubscribe,needToUnsubscribe.iterator().next());
		} else {
			return new SubscribeAction(SubscribeType.Subscribe,needToSubscribe.iterator().next());
		}
	}
	public void registerSubscribeChange(SubscribeAction action)
	{
		if(action.getAction().equals(SubscribeType.Subscribe))
		{
			needToSubscribe.remove(action.getSubscriberId());
			friendList.add(action.getSubscriberId());
			log("FOLLOWED "+action.getSubscriberId());
		}else{
			needToUnsubscribe.remove(action.getSubscriberId());
			friendList.remove(action.getSubscriberId());
			log("UNFOLLOWED "+action.getSubscriberId());
		}
		if(polling && (subscribeTask==null || subscribeTask.isDone() || subscribeTask.isCancelled()))
		{
			subscribeTask = executor.schedule(new ModifySubscriptionsWorker(applicationContext,this), 10, TimeUnit.SECONDS);
			log("Modify Subscriptions Task Scheduled:1m(registerSubscribeChange)");
		}
	}
	public void subscribeChangeFailed()
	{
		if(polling)
		{
			subscribeTask = executor.schedule(new ModifySubscriptionsWorker(applicationContext,this), 10, TimeUnit.MINUTES);
			log("Modify Subscriptions Task Scheduled:10m(subscribeChangeFailed)");
		}
	}
	public void directMessageCheckSucceeded()
	{
		if(polling)
		{
			directMessageCheckTask = executor.schedule(new DirectMessageCheckWorker(applicationContext,this), 1, TimeUnit.MINUTES);
			log("Direct Message Check Task Scheduled:1m(directMessageCheckSucceeded)");
		}
	}
	public void directMessageCheckFailed()
	{
		if(polling)
		{
			directMessageCheckTask = executor.schedule(new DirectMessageCheckWorker(applicationContext,this), 10, TimeUnit.MINUTES);
			log("Direct Message Check Task Scheduled:10m(directMessageCheckFailed)");
		}
	}	
	public void directMessageSendSucceeded()
	{
		if(polling)
		{
			sendDirectMessageTask = executor.schedule(new SendDirectMessageWorker(applicationContext,this), 5, TimeUnit.SECONDS);
			log("Direct Message Send Task Scheduled:5s(directMessageSendSucceeded)");
		}
	}
	public void directMessageSendSucceededNoMessages()
	{
		if(polling)
		{
			sendDirectMessageTask = executor.schedule(new SendDirectMessageWorker(applicationContext,this), 5, TimeUnit.SECONDS);
			log("Direct Message Send Task Scheduled:5s(directMessageSendSucceededNoMessage)");
		}
	}
	public void directMessageSendFailed()
	{
		if(polling)
		{
			sendDirectMessageTask = executor.schedule(new SendDirectMessageWorker(applicationContext,this), 10, TimeUnit.MINUTES);
			log("Direct Message Send Task Scheduled:10m(directMessageSendFailed)");
		}
	}
	void log(String anyMessage)
	{
		log.info(account+","+anyMessage+","+remainingQueries+","+maxQueries+","+df().format(resetTime));
	}
}
