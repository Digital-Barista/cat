package com.digitalbarista.cat.mail;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.mail.Message;
import javax.mail.Flags.Flag;
import javax.naming.InitialContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.mail.folders.MailFolder;

public class MailboxPollWorker implements Runnable {

	private MailboxConfig mailbox;
	private Logger log = LogManager.getLogger(MailboxPollWorker.class);
	private String cfName;
	private String destName;
	
	public MailboxPollWorker(MailboxConfig newMailbox, String destination, String connFact)
	{
		mailbox=newMailbox;
		cfName=connFact;
		destName=destination;
	}
	
	@Override
	public void run() {
	    try
	    {
	      MailFolder mailFolder = MailFolder.getInstance(mailbox);
	      mailFolder.open();
	      while (mailFolder.hasNext())
	      {
	        Message msg = (Message)mailFolder.next();
	        MessageSummary message = new MessageSummary();
	        message.setContent(msg.getContent());
	        message.setContentType(msg.getContentType());
	        message.setFrom(msg.getFrom());
	        message.setReceivedDate(msg.getReceivedDate());
	        message.setRecipients(msg.getAllRecipients());
	        message.setReplyTo(msg.getReplyTo());
	        message.setSentDate(msg.getSentDate());
	        message.setSize(msg.getSize());
	        message.setSubject(msg.getSubject());
	        
	    	Connection conn=null;
	    	Session sess=null;
	    	try {
	    		InitialContext ic = new InitialContext();
	    		ConnectionFactory cf = (ConnectionFactory)ic.lookup(cfName);
	    		Destination dest = (Destination)ic.lookup(destName);
				conn = cf.createConnection();
				sess = conn.createSession(true, Session.SESSION_TRANSACTED);
				javax.jms.Message jmsMsg = sess.createObjectMessage(message);
				MessageProducer prod = sess.createProducer(dest);
				prod.send(jmsMsg);
				msg.setFlag(Flag.DELETED, true);
	    	} catch (Exception ex) {
	    		msg.setFlag(Flag.SEEN, false);
				throw new RuntimeException("Error queueing up an event:"+ex.toString(),ex);
			}
	    	finally
	    	{
	    		try{if(sess!=null)sess.close();}catch(Exception ex){}
	    		try{if(conn!=null)conn.close();}catch(Exception ex){}
	    	}
	      }
	      mailFolder.close();
	    }
	    catch (Exception e)
	    {
	      log.error("Failed to execute folder check, spec=" + this.mailbox,e);
	    }
	}

	public MailboxConfig getMailbox() {
		return mailbox;
	}

}
