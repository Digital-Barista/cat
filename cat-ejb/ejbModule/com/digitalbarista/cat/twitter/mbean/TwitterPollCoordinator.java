package com.digitalbarista.cat.twitter.mbean;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ejb.Remote;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Remote
@Path("/twitter")
@Produces("application/xml")
public interface TwitterPollCoordinator {

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
	
	public TokenPair acquireRequestToken(String callbackURL);
	
	public boolean retrieveAccessToken(String requestToken, String verifier);
}
