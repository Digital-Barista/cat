package com.digitalbarista.cat.twitter.mbean;

import java.io.IOException;
import java.io.InputStream;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.transaction.UserTransaction;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;

import flex.messaging.util.URLEncoder;

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
			MapMessage msg = (MapMessage)consumer.receive(1000);
			if(msg==null)
			{
				getAccountPollManager().directMessageSendSucceededNoMessages();
				return "No messages to send";
			}
			msg.acknowledge();
			consumer.close();
			
			producer = sess.createProducer(dest);

			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://api.twitter.com/1/direct_messages/new.xml?user_id="+msg.getString("target")+"&text="+URLEncoder.encode(msg.getString("message")));
			post.getParams().setParameter("user",msg.getString("target"));
			post.getParams().setParameter("text",URLEncoder.encode(msg.getString("message")));

			if(pm.getCredentials().indexOf("|")==-1)
			{
				client.getCredentialsProvider().setCredentials(new AuthScope("api.twitter.com",AuthScope.ANY_PORT), new UsernamePasswordCredentials(pm.getAccount(),pm.getCredentials()));
			} else {
				OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(TwitterPollCoordinator.APP_TOKEN,TwitterPollCoordinator.APP_SECRET);
				oAuthConsumer.setTokenWithSecret(pm.getCredentials().split("\\|")[0], pm.getCredentials().split("\\|")[1]);
				oAuthConsumer.sign(post);
			}
			
			int status = -1;			
			
			HttpResponse response;
			
			try
			{
				response = client.execute(post);
				if(response.getStatusLine().getStatusCode()==200)
				{
					getAccountPollManager().directMessageSendSucceeded();
					return "Success - "+msg.getString("source")+" : "+msg.getString("message");	
				} else {
					tx.rollback();
					InputStream in = response.getEntity().getContent();
					StringBuffer errmsg = new StringBuffer();
					byte[] buf = new byte[1024];
					int size=-1;
					do
					{
						size=in.read(buf);
						if(size==-1)
							continue;
						errmsg.append(new String(buf,0,size));
					}while(size>=0);
					log.error("Couldn't send a twitter message: status-"+response.getStatusLine().getStatusCode()+", message="+errmsg.toString());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			producer.send(msg);

			getAccountPollManager().directMessageSendFailed();
			
			return "Failure - status:"+status+" "+msg.getString("source")+" : "+msg.getString("message")+" -- requeued";
			
    	} catch (Throwable ex) {
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
