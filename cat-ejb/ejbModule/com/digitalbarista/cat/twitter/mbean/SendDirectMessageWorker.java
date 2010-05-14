package com.digitalbarista.cat.twitter.mbean;

import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.transaction.UserTransaction;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.context.ApplicationContext;

public class SendDirectMessageWorker extends TwitterPollWorker<String> {

	public SendDirectMessageWorker(TwitterAccountPollManager pm) {
		super(pm);
	}
	
	@Override
	public String call() {
    	javax.jms.Connection conn=null;
    	Session sess=null;
    	UserTransaction tx=null;
    	MessageProducer producer=null;
    	try {
    		TwitterAccountPollManager pm=getAccountPollManager();
    		
    		tx = (UserTransaction)getInitialContext().lookup("UserTransaction");
    		tx.begin();
    		ConnectionFactory cf = (ConnectionFactory)getInitialContext().lookup(cfName);
    		Destination dest = (Destination)getInitialContext().lookup(twitterSendDestName);
			conn = cf.createConnection();
			sess = conn.createSession(true, Session.SESSION_TRANSACTED);
			MessageConsumer consumer = sess.createConsumer(dest, "source='"+pm.getAccount()+"'");
			conn.start();
			MapMessage msg = (MapMessage)consumer.receiveNoWait();
			if(msg==null)
			{
				getAccountPollManager().directMessageSendSucceededNoMessages();
				return "No messages to send";
			}
			msg.acknowledge();
			consumer.close();
			
			producer = sess.createProducer(dest);

			HttpClient client = new HttpClient();
			PostMethod post = new PostMethod("http://www.twitter.com/direct_messages/new.xml");
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(pm.getAccount(),pm.getCredentials()));
			
			post.setQueryString(new NameValuePair[]{new NameValuePair("user_id",msg.getString("target")),new NameValuePair("text",msg.getString("message"))});
			int status = -1;			
			
			try
			{
				status = client.executeMethod(post);
				if(status==200)
				{
					getAccountPollManager().directMessageSendSucceeded();
					return "Success - "+msg.getString("source")+" : "+msg.getString("message");	
				}
			} catch (HttpException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			producer.send(msg);

			getAccountPollManager().directMessageSendFailed();
			
			return "Failure - status:"+status+" "+msg.getString("source")+" : "+msg.getString("message")+" -- requeued";
			
    	} catch (Exception ex) {
    		log.error("Couldn't de-queue a twitter message.",ex);
    		return "Failed to send twitter message: Please check logs.";
    	}
    	finally
    	{
    		try{if(producer!=null)producer.close();}catch(Exception ex){}
    		try{if(tx!=null)tx.commit();}catch(Exception ex){}
    		try{if(sess!=null)sess.close();}catch(Exception ex){}
    		try{if(conn!=null)conn.close();}catch(Exception ex){}
    	}
	}

}
