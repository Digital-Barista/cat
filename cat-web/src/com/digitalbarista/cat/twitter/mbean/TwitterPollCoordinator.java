package com.digitalbarista.cat.twitter.mbean;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.mx.util.MBeanServerLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.twitter.bindings.DirectMessage;
import com.digitalbarista.cat.twitter.bindings.DirectMessageCollection;
import com.digitalbarista.cat.twitter.bindings.Tweeter;
import com.digitalbarista.cat.twitter.bindings.extras.DMComparator;


public class TwitterPollCoordinator implements TwitterPollCoordinatorMBean, ApplicationContextAware {

	private Map<String,PollStats> accountPollStats = new HashMap<String,PollStats>();
	private String cfName = "java:/JmsXA";
	private String destName = "cat/messaging/Events";
	private String twitterSendDestName = "cat/messaging/TwitterOutgoing";
	
	@Override
	public int checkMessages(String account) {
		int messageCount=0;
		try
		{
			InitialContext ic = new InitialContext();
			
			DataSource ds = (DataSource)ic.lookup("java:/jdbc/campaignAdminDS");
			String credentials=null;
			
			Connection conn=null;
			PreparedStatement stmt=null;
			ResultSet rs=null;
			
			try
			{
				conn = ds.getConnection();
				stmt = conn.prepareStatement("select credentials from entry_points where entry_point=? and entry_type=?");
				stmt.setString(1, account);
				stmt.setString(2, "Twitter");
				rs = stmt.executeQuery();
				rs.next();
				credentials=rs.getString(1);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try{rs.close();}catch(Exception e){}
				try{stmt.close();}catch(Exception e){}
				try{conn.close();}catch(Exception e){}
			}
			
			JAXBContext context = JAXBContext.newInstance(DirectMessageCollection.class,DirectMessage.class,Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			PollStats ps = accountPollStats.get(account);
			if(ps==null)
			{
				ps=new PollStats();
				accountPollStats.put(account, ps);				
			}

			HttpClient client = new HttpClient();
			GetMethod get = new GetMethod("http://www.twitter.com/direct_messages.xml");
			get.setQueryString("since_id="+ps.getLowestReadMessage());
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(account,credentials));
			
			client.executeMethod(get);
			
			ps.setLastPollTime(new Date());
			Integer maxQueries = null;
			Integer remainingQueries = null;
			try
			{
				maxQueries = new Integer(get.getResponseHeader("X-experimental-RLS-maxvalue").getValue());
				if(!maxQueries.equals(ps.getMaxQueries()))
					ps.setMaxQueries(maxQueries);
			}catch(Exception e){}
			try
			{
				remainingQueries = new Integer(get.getResponseHeader("X-RateLimit-Remaining").getValue());
				if(!remainingQueries.equals(ps.getRemainingQueries()))
					ps.setRemainingQueries(remainingQueries);
			}catch(Exception e){}
			try
			{
				if(ps.getResetTime()==null)
					ps.setResetTime(new Date());
				ps.getResetTime().setTime(new Long(get.getResponseHeader("X-RateLimit-Reset").getValue())*1000);
			}catch(Exception e){}

			DirectMessageCollection dmc = (DirectMessageCollection)decoder.unmarshal(new StringReader(get.getResponseBodyAsString()));
			deleteMessages(account,registerMessages(dmc,ps),ps);
			
			if(dmc.getDirectMessages()==null)
				return 0;
			
			return dmc.getDirectMessages().size();
		}catch(JAXBException e)
		{
			e.printStackTrace();
			return -1;
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return messageCount;
	}

	private Long[] registerMessages(DirectMessageCollection messages,PollStats stats)
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
		    		  message.getText());
	        
	    	javax.jms.Connection conn=null;
	    	Session sess=null;
	    	try {
	    		InitialContext ic = new InitialContext();
	    		ConnectionFactory cf = (ConnectionFactory)ic.lookup(cfName);
	    		Destination dest = (Destination)ic.lookup(destName);
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
	
	@Override
	public String sendMessage() {
    	javax.jms.Connection conn=null;
    	Session sess=null;
    	UserTransaction tx=null;
    	MessageProducer producer=null;
    	try {
    		InitialContext ic = new InitialContext();
    		tx = (UserTransaction)ic.lookup("java:comp/UserTransaction");
    		tx.begin();
    		ConnectionFactory cf = (ConnectionFactory)ic.lookup(cfName);
    		Destination dest = (Destination)ic.lookup(twitterSendDestName);
			conn = cf.createConnection();
			sess = conn.createSession(true, Session.SESSION_TRANSACTED);
			QueueBrowser browser = sess.createBrowser((Queue)dest);
			Enumeration messageList = browser.getEnumeration();
			if(!messageList.hasMoreElements())
				return "No messages to send";
			MapMessage msg = (MapMessage)messageList.nextElement();
			browser.close();
			
			producer = sess.createProducer(dest);

			DataSource ds = (DataSource)ic.lookup("java:/jdbc/campaignAdminDS");
			String credentials=null;
			
			Connection dbConn=null;
			PreparedStatement stmt=null;
			ResultSet rs=null;
			
			try
			{
				dbConn = ds.getConnection();
				stmt = dbConn.prepareStatement("select credentials from entry_points where entry_point=? and entry_type=?");
				stmt.setString(1, msg.getString("source"));
				stmt.setString(2, "Twitter");
				rs = stmt.executeQuery();
				rs.next();
				credentials=rs.getString(1);
			} catch (SQLException ex) {
				ex.printStackTrace();
				producer.send(msg);
				return "Unable to retrieve credentials for the provided account";
			}
			finally
			{
				try{rs.close();}catch(Exception ex){}
				try{stmt.close();}catch(Exception ex){}
				try{conn.close();}catch(Exception ex){}
			}

			HttpClient client = new HttpClient();
			PostMethod post = new PostMethod("http://www.twitter.com/direct_messages/new.xml");
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(msg.getString("source"),credentials));
			
			post.setQueryString(new NameValuePair[]{new NameValuePair("screen_name",msg.getString("target")),new NameValuePair("text",msg.getString("message"))});
			int status = -1;			
			
			try
			{
				status = client.executeMethod(post);
				if(status==200)
					return "Success - "+msg.getString("source")+" : "+msg.getString("message");	
			} catch (HttpException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			producer.send(msg);
			
			return "Failure - status:"+status+" "+msg.getString("source")+" : "+msg.getString("message")+" -- requeued";
			
    	} catch (Exception ex) {
    		ex.printStackTrace();
			throw new RuntimeException("Error de-queueing a twitter message:"+ex.toString(),ex);
    	}
    	finally
    	{
    		try{if(producer!=null)producer.close();}catch(Exception ex){}
    		try{if(tx!=null)tx.commit();}catch(Exception ex){}
    		try{if(sess!=null)sess.close();}catch(Exception ex){}
    		try{if(conn!=null)conn.close();}catch(Exception ex){}
    	}
	}

	public void deleteMessages(String account, Long[] ids, PollStats ps) {
		try
		{
			InitialContext ic = new InitialContext();
			
			DataSource ds = (DataSource)ic.lookup("java:/jdbc/campaignAdminDS");
			String credentials=null;
			
			Connection conn=null;
			PreparedStatement stmt=null;
			ResultSet rs=null;
			
			try
			{
				conn = ds.getConnection();
				stmt = conn.prepareStatement("select credentials from entry_points where entry_point=? and entry_type=?");
				stmt.setString(1, account);
				stmt.setString(2, "Twitter");
				rs = stmt.executeQuery();
				rs.next();
				credentials=rs.getString(1);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try{rs.close();}catch(Exception e){}
				try{stmt.close();}catch(Exception e){}
				try{conn.close();}catch(Exception e){}
			}
			
			HttpClient client = new HttpClient();
			
			for(long id : ids)
			{
				DeleteMethod delete = new DeleteMethod("http://www.twitter.com/direct_messages/destroy/"+id+".xml");
				client.getParams().setAuthenticationPreemptive(true);
				client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(account,credentials));
				
				try
				{
					client.executeMethod(delete);
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(delete.getStatusCode()==200)
					ps.setHighestDeletedMessage(id);
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{}
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		try {
			MBeanServer server = MBeanServerLocator.locateJBoss();
			server.registerMBean(this, new ObjectName("dbi.config:service=TwitterPollCoordinator"));
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		PollStats ps = accountPollStats.get(account);
		if(ps==null)
		{
			ps=new PollStats();
			accountPollStats.put(account, ps);				
		}
		
		deleteMessages(account,new Long[]{id},ps);
	}

	public String getTwitterSendDestName() {
		return twitterSendDestName;
	}

	public void setTwitterSendDestName(String twitterSendDestName) {
		this.twitterSendDestName = twitterSendDestName;
	}
}
