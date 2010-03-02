package com.digitalbarista.cat.twitter.mbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.context.ApplicationContext;

import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.twitter.bindings.DirectMessage;
import com.digitalbarista.cat.twitter.bindings.DirectMessageCollection;
import com.digitalbarista.cat.twitter.bindings.ErrorMessage;
import com.digitalbarista.cat.twitter.bindings.Tweeter;
import com.digitalbarista.cat.twitter.bindings.extras.DMComparator;

public class DirectMessageCheckWorker extends TwitterPollWorker<Integer> {

	public DirectMessageCheckWorker(ApplicationContext ctx, TwitterAccountPollManager pollManager) {
		super(ctx,pollManager);
	}

	public Integer call()
	{
		HttpClient client=null;
		GetMethod get=null;
		
		try
		{
			JAXBContext context = JAXBContext.newInstance(DirectMessageCollection.class,DirectMessage.class,Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			TwitterAccountPollManager ps = getAccountPollManager();
			
			client = new HttpClient();
			get = new GetMethod("http://www.twitter.com/direct_messages.xml");
			get.setQueryString("since_id="+ps.getLowestReadMessage());
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
			
			int result = client.executeMethod(get);
			if(result!=200)
			{
				updateRateLimitInfo(get, ps);
				throw new OperationFailedException("Could not check for direct messages.  response="+result);
			}

			updateRateLimitInfo(get, ps);

			Object ret = decoder.unmarshal(get.getResponseBodyAsStream());
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
		finally
		{
			try{if(get!=null)get.releaseConnection();}catch(Exception e){}
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
			HttpClient client = new HttpClient();
			
			for(long id : ids)
			{
				DeleteMethod delete = new DeleteMethod("http://www.twitter.com/direct_messages/destroy/"+id+".xml");
				client.getParams().setAuthenticationPreemptive(true);
				client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getAccountPollManager().getAccount(),getAccountPollManager().getCredentials()));
				
				try
				{
					client.executeMethod(delete);
				} catch (HttpException e) {
					log.error("An error occurred while deleting DM id="+id,e);
				} catch (IOException e) {
					log.error("An error occurred while deleting DM id="+id,e);
				}
				
				if(delete.getStatusCode()==200)
					ps.setHighestDeletedMessage(id);
			}
		} catch (Exception e) {
			log.error("Failed to delete a direct message.",e);
		}
		finally{}
	}
}
