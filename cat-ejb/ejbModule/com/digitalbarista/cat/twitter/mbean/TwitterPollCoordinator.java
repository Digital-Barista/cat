package com.digitalbarista.cat.twitter.mbean;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ejb.Remote;

@Remote
public interface TwitterPollCoordinator {

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
	
	public int checkMessages(String account);
	
	public void deleteMesage(String account, long id);
	
	public String sendMessage(String account);
	
	public boolean stopAllPolling(String account);
	
	public boolean startPolling(String account);
	
	public String refreshTwitterAccounts();
	
	public void start();

	public void manualStart();
	
	public void stop();
	
	public void startSingleton();
	
	public void stopSingleton();
}
