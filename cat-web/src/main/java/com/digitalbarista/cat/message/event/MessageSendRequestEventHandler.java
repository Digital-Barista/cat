package com.digitalbarista.cat.message.event;

import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.audit.OutgoingMessageEntryDO;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.EntryData;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.ResponseConnector;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.FacebookAppDO;
import com.digitalbarista.cat.data.FacebookMessageDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.FacebookManager;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MessageSendRequestEventHandler implements CATEventHandler {
	
	private String cfName = "java:/JmsXA";
	private String twitterSendDestName = "cat/messaging/TwitterOutgoing";
	
	private Logger log = LogManager.getLogger(MessageSendRequestEventHandler.class);
	private String dateFormat = "MM/dd/yyyy";

  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private CampaignManager cMan;
  
  @Autowired
  private FacebookManager fbMan;
  
  @Autowired
  private EventManager eMan;
  
	@Override
        @Transactional
	public void processEvent(CATEvent e) {
		OutgoingMessageEntryDO outAudit = new OutgoingMessageEntryDO();
		outAudit.setDateSent(new Date());
		outAudit.setDestination(e.getTarget());
		outAudit.setNodeUID(e.getArgs().get("nodeUID"));
		outAudit.setNodeVersion(new Integer(e.getArgs().get("nodeVersion")));
		outAudit.setSubjectOrMessage(e.getArgs().get("message"));
		switch(e.getSourceType())
		{
			case EmailEndpoint:
				outAudit.setMessageType(EntryPointType.Email);
				break;
			case SMSEndpoint:
				outAudit.setMessageType(EntryPointType.SMS);
				break;
			case TwitterEndpoint:
				outAudit.setMessageType(EntryPointType.Twitter);
				break;
			case FacebookEndpoint:
				outAudit.setMessageType(EntryPointType.Facebook);
				break;
		}
		sf.getCurrentSession().persist(outAudit);
		if(e.getSourceType().equals(CATEventSource.EmailEndpoint))
		{
			outAudit.setMessageType(EntryPointType.Email);
			try
			{
				InitialContext ic = new InitialContext();
				Session mailSession = (Session)ic.lookup("java:/Mail");
		        Message msg = new MimeMessage(mailSession);
		        msg.setFrom(new InternetAddress(e.getSource()));
		        msg.setRecipients(Message.RecipientType.TO,
		            InternetAddress.parse(e.getTarget(), false));
		        msg.setSubject(e.getArgs().get("subject"));
		        msg.setText(e.getArgs().get("message"));
		        msg.setSentDate(new Date());
		        Transport.send(msg);
			}catch(Exception ex)
			{
				throw new RuntimeException("Could not deliver the requested message!",ex);
			}
		} else if(e.getSourceType().equals(CATEventSource.SMSEndpoint)){
			outAudit.setMessageType(EntryPointType.SMS);
			outAudit.setSubjectOrMessage(e.getArgs().get("message"));
			DefaultHttpClient client=new DefaultHttpClient();
			StringBuffer queryString = new StringBuffer();
			
			queryString.append("username=dbi&password=Hondaf4");
			queryString.append("&to=").append(URLEncoder.encode(e.getTarget()));
			queryString.append("&from=").append(URLEncoder.encode(e.getSource()));
			queryString.append("&text=").append(URLEncoder.encode(e.getArgs().get("message")));
			queryString.append("&smsc=").append(URLEncoder.encode(e.getSource()));

			HttpGet method = new HttpGet("http://206.72.101.130:13013/cgi-bin/sendsms?"+queryString);
			try
			{
				
				HttpResponse result = client.execute(method);
				if(result==null || result.getStatusLine().getStatusCode()!=202)
				{
					throw new RuntimeException("Could not send message.  code="+result);
				}
			}catch(Exception ex)
			{
				throw new RuntimeException("Could not deliver the requested message!",ex);
			}
		} else if(e.getSourceType().equals(CATEventSource.TwitterEndpoint)){
			outAudit.setMessageType(EntryPointType.Twitter);
			outAudit.setSubjectOrMessage(e.getArgs().get("message"));
			javax.jms.Connection conn=null;
			javax.jms.Session sess=null;
			try
			{
	    		InitialContext ic = new InitialContext();
	    		ConnectionFactory cf = (ConnectionFactory)ic.lookup(cfName);
	    		Destination dest = (Destination)ic.lookup(twitterSendDestName);
				conn = cf.createConnection();
				sess = conn.createSession(true, javax.jms.Session.SESSION_TRANSACTED);
				MessageProducer producer = sess.createProducer(dest);
				MapMessage message = sess.createMapMessage();
				message.setString("source", e.getSource());
				message.setStringProperty("source", e.getSource());
				message.setString("target", e.getTarget());
				message.setString("message", e.getArgs().get("message"));
				producer.send(message);
			}catch(Exception ex)
			{
				throw new RuntimeException("Could not deliver the requested message!",ex);
			}finally
			{
	    		try{if(sess!=null)sess.close();}catch(Exception ex){}
	    		try{if(conn!=null)conn.close();}catch(Exception ex){}
			}
		} else if(e.getSourceType().equals(CATEventSource.FacebookEndpoint)){
			outAudit.setMessageType(EntryPointType.Facebook);
			outAudit.setSubjectOrMessage(e.getArgs().get("message"));
			javax.jms.Connection conn=null;
			javax.jms.Session sess=null;
			try
			{
				FacebookMessageDO fbMessage = new FacebookMessageDO();
				fbMessage.setBody(e.getArgs().get("message"));
				fbMessage.setCreateDate(new GregorianCalendar());
				fbMessage.setFacebookUID(e.getTarget());
				fbMessage.setFacebookAppName(e.getSource());
				fbMessage.setTitle(e.getArgs().get("subject"));
				
				String nodeUID = e.getArgs().get("nodeUID");
				Node fromNode = cMan.getNode(nodeUID);
				Set<String> keywordSet = new HashSet<String>(); 
				for(String downstreamUID : fromNode.getDownstreamConnections())
				{
					Connector dConn = cMan.getConnector(downstreamUID);
					if(dConn.getType()==ConnectorType.Response)
					{
						for(EntryData entry : ((ResponseConnector)dConn).getEntryData())
						{
							if(entry.getEntryType()!=EntryPointType.Facebook) continue;
							if(!entry.getEntryPoint().equals(fbMessage.getFacebookAppName())) continue;
							keywordSet.add(entry.getKeyword());
						}
					}
				}
				StringBuffer sb = new StringBuffer();
				for(String keyword : keywordSet)
					sb.append((sb.length()!=0)?",":"").append(keyword);
				fbMessage.setMetadata(sb.toString());
				sf.getCurrentSession().persist(fbMessage);
				sf.getCurrentSession().flush();
                                FacebookAppDO applicationInfo = (FacebookAppDO)sf.getCurrentSession().get(FacebookAppDO.class, e.getSource());
                                if(applicationInfo!=null && applicationInfo.isSendNotifications())
                                    eMan.queueEvent(CATEvent.buildNotificationRequestedEvent(e.getSource(), e.getTarget()));
                        }catch(Exception ex)
			{
				throw new RuntimeException("Could not deliver the requested message!",ex);
			}finally
			{
	    		try{if(sess!=null)sess.close();}catch(Exception ex){}
	    		try{if(conn!=null)conn.close();}catch(Exception ex){}
			}
		} else {
			throw new IllegalArgumentException("Cannot process the specified message type.");
		}
	}

}
