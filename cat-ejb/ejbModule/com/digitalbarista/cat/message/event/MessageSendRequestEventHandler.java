package com.digitalbarista.cat.message.event;

import java.util.Date;

import javax.ejb.SessionContext;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;

import com.digitalbarista.cat.audit.OutgoingMessageEntryDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;

public class MessageSendRequestEventHandler extends CATEventHandler {
	
	protected MessageSendRequestEventHandler(EntityManager newEM,
			SessionContext newSC, 
			EventManager newEventManager,
			CampaignManager newCampaignManager,
			EventTimerManager timer) {
		super(newEM, newSC, newEventManager, newCampaignManager,timer);
	}

	@Override
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
		}
		getEntityManager().persist(outAudit);
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
			HttpClient client=new HttpClient();
			GetMethod method = new GetMethod("http://206.72.101.130:13013/cgi-bin/sendsms");
			try
			{
				NameValuePair[] params = new NameValuePair[6];
				params[0]=new NameValuePair("username", "dbi");
				params[1]=new NameValuePair("password", "Hondaf4");
				params[2]=new NameValuePair("to", e.getTarget());
				params[3]=new NameValuePair("from",e.getSource());
				params[4]=new NameValuePair("text", e.getArgs().get("message"));
				params[5]=new NameValuePair("smsc", e.getSource());
				method.setQueryString(params);
				int result = client.executeMethod(method);
				if(result!=202)
				{
					throw new RuntimeException("Could not send message.  code="+result+", response='"+method.getResponseBodyAsString()+"'");
				}
			}catch(Exception ex)
			{
				throw new RuntimeException("Could not deliver the requested message!",ex);
			}finally
			{
				method.releaseConnection();
			}
		} else {
			throw new IllegalArgumentException("Cannot process the specified message type.");
		}
	}

}
