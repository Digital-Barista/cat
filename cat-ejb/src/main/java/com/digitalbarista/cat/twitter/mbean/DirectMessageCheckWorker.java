package com.digitalbarista.cat.twitter.mbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.twitter.bindings.DirectMessage;
import com.digitalbarista.cat.twitter.bindings.DirectMessageCollection;
import com.digitalbarista.cat.twitter.bindings.ErrorMessage;
import com.digitalbarista.cat.twitter.bindings.Tweeter;
import com.digitalbarista.cat.twitter.bindings.extras.DMComparator;

public class DirectMessageCheckWorker extends TwitterPollWorker<Integer> {

	public DirectMessageCheckWorker(TwitterAccountPollManager pollManager) {
		super(pollManager);
	}

	public Integer call()
	{
		DefaultHttpClient client=null;
		HttpGet get=null;
		
		try
		{
			JAXBContext context = JAXBContext.newInstance(DirectMessageCollection.class,DirectMessage.class,Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			TwitterAccountPollManager ps = getAccountPollManager();
			
			client = new DefaultHttpClient();
			get = new HttpGet("http://api.twitter.com/1/direct_messages.xml");
			get.getParams().setParameter("since_id",ps.getLowestReadMessage());
			
			if(ps.getCredentials().indexOf("|")==-1)
			{
				client.getCredentialsProvider().setCredentials(new AuthScope("api.twitter.com",AuthScope.ANY_PORT), new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
			} else {
				OAuthConsumer consumer = new CommonsHttpOAuthConsumer(TwitterPollCoordinator.APP_TOKEN,TwitterPollCoordinator.APP_SECRET);
				consumer.setTokenWithSecret(ps.getCredentials().split("\\|")[0], ps.getCredentials().split("\\|")[1]);
				consumer.sign(get);
			}
			
			HttpResponse result = client.execute(get);
			if(result.getStatusLine().getStatusCode()!=200)
			{
				updateRateLimitInfo(result, ps);
				throw new OperationFailedException("Could not check for direct messages.  response="+result);
			}

			updateRateLimitInfo(result, ps);

			Object ret = decoder.unmarshal(result.getEntity().getContent());
			if(ret instanceof ErrorMessage)
				throw new OperationFailedException("Could not check for direct messages.  "+((ErrorMessage)ret).getErrorMessage());
			DirectMessageCollection dmc = (DirectMessageCollection)ret;
			deleteMessages(registerMessages(dmc,ps),ps);
			
			getAccountPollManager().directMessageCheckSucceeded();
			
			if(dmc.getDirectMessages()==null)
				return 0;
			
			return dmc.getDirectMessages().size();
		}
		catch(Exception e)
		{
			log.error("Failed to retrieve messages.",e);
			getAccountPollManager().directMessageCheckFailed();
			return -1;
		}
	}
	
	private Long[] registerMessages(DirectMessageCollection messages,TwitterAccountPollManager stats)
	{
		List<Long> msgIDsToDelete = new ArrayList<Long>();
		if(messages.getDirectMessages()==null)
			return new Long[0];
		Collections.sort(messages.getDirectMessages(), new DMComparator());
		for(DirectMessage message : messages.getDirectMessages())
		{
			if(message.getId()<=stats.getLowestReadMessage())
				continue;
			
		      CATEvent e = CATEvent.buildIncomingTwitterDMEvent(
		    		  message.getSenderScreenName(),
		    		  message.getRecipientScreenName(), 
		    		  message.getText(),
		    		  ""+message.getSenderId());
	        
	    	javax.jms.Connection conn=null;
	    	Session sess=null;
	    	try {
	    		ConnectionFactory cf = (ConnectionFactory)getInitialContext().lookup(cfName);
	    		Destination dest = (Destination)getInitialContext().lookup(destName);
				conn = cf.createConnection();
				sess = conn.createSession(true, Session.SESSION_TRANSACTED);
				javax.jms.Message jmsMsg = sess.createObjectMessage(e);
				MessageProducer prod = sess.createProducer(dest);
				prod.send(jmsMsg);
				stats.setLowestReadMessage(message.getId());
				msgIDsToDelete.add(message.getId());
	    	} catch (Exception ex) {
				throw new RuntimeException("Error queueing up an event:"+ex.toString(),ex);
			}
	    	finally
	    	{
	    		try{if(sess!=null)sess.close();}catch(Exception ex){}
	    		try{if(conn!=null)conn.close();}catch(Exception ex){}
	    	}
		}
    	return msgIDsToDelete.toArray(new Long[msgIDsToDelete.size()]);
	}
	
	public void deleteMessages(Long[] ids, TwitterAccountPollManager ps) {
		try
		{
			DefaultHttpClient client = new DefaultHttpClient();
			
			for(long id : ids)
			{
				HttpDelete delete = new HttpDelete("http://api.twitter.com/1/direct_messages/destroy/"+id+".xml");

				if(ps.getCredentials().indexOf("|")==-1)
				{
					client.getCredentialsProvider().setCredentials(new AuthScope("api.twitter.com",AuthScope.ANY_PORT), new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
				} else {
					OAuthConsumer consumer = new CommonsHttpOAuthConsumer(TwitterPollCoordinator.APP_TOKEN,TwitterPollCoordinator.APP_SECRET);
					consumer.setTokenWithSecret(ps.getCredentials().split("\\|")[0], ps.getCredentials().split("\\|")[1]);
					consumer.sign(delete);
				}
				
				HttpResponse resp = null;
				
				try
				{
					resp = client.execute(delete);
				} catch (IOException e) {
					log.error("An error occurred while deleting DM id="+id,e);
				}
				
				if(resp!=null && resp.getStatusLine().getStatusCode()==200)
					ps.setHighestDeletedMessage(id);
			}
		} catch (Exception e) {
			log.error("Failed to delete a direct message.",e);
		}
		finally{}
	}
}
