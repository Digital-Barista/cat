package com.digitalbarista.cat.mail.folders;

import java.util.Iterator;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import com.digitalbarista.cat.mail.MailboxConfig;

public abstract class MailFolder implements Iterator
{
	private Session session;
	private Store store;
	private Folder folder;
	private String mailServer;
	private String folderName;
	private String userName;
	private String password;
	private int port;
	private boolean debug;
	private Properties sessionProps;
	private Message[] msgs = new Message[0];
	private int messagePosition;
	
	public MailFolder(MailboxConfig config)
	{
	  this.mailServer = config.getHostName();
	  this.folderName = config.getFolder();
	  this.userName = config.getUsername();
	  this.password = config.getPassword();
	  this.port = config.getPort();
	
	  this.sessionProps = new Properties();
	  /*this.sessionProps.setProperty("mail.transport.protocol", "smtp");
	  this.sessionProps.setProperty("mail.smtp.host", this.mailServer);	
	  this.sessionProps.setProperty("mail.imap.starttls.enable", "true");*/
	}
	
	public void open() throws Exception
	{
	  this.session = Session.getInstance(this.sessionProps);
	
	  this.store = openStore(this.session);
	  if (this.port == 0)
	  {
	    this.store.connect(this.mailServer, this.userName, this.password);
	  }
	  else
	  {
	    this.store.connect(this.mailServer, this.port, this.userName, this.password);
	  }
	  this.folder = this.store.getFolder(this.folderName);
	
	  if ((this.folder == null) || (!(this.folder.exists())))
	  {
	    MessagingException e = new MessagingException("Failed to find folder: " + this.folderName);
	    throw e;
	  }
	
	  this.folder.open(2);
	  this.msgs = getMessages(this.folder);
	}
	
	protected abstract Store openStore(Session paramSession) throws NoSuchProviderException;
	
	protected abstract void closeStore(boolean paramBoolean, Store paramStore, Folder paramFolder) throws MessagingException;
	
	protected abstract Message[] getMessages(Folder paramFolder) throws MessagingException;
	
	protected abstract void markMessageSeen(Message paramMessage) throws MessagingException;
	
	public void close() throws MessagingException
	{
	  close(true);
	}
	
	public boolean hasNext() {
	  return (this.messagePosition < this.msgs.length);
	}
	
	public Object next() {
	  try {
	    Message m = this.msgs[(this.messagePosition++)];
	    markMessageSeen(m);
	    return m;
	  } catch (MessagingException e) {
	    close(false);
	    throw new RuntimeException(e);
	  }
	}
	
	public void remove() {
	  throw new UnsupportedOperationException();
	}
	
	protected void close(boolean checkSuccessful) {
	  try {
	    closeStore(checkSuccessful, this.store, this.folder);
	  } catch (MessagingException e) {
	    throw new RuntimeException("Error closing mail store", e);
	  }
	}
	
	public static MailFolder getInstance(MailboxConfig config) {
	  if (MailboxConfig.MailboxType.POP3.equals(config.getType()))
	    return new POP3Folder(config);
	  if (MailboxConfig.MailboxType.IMAP.equals(config.getType()))
	    return new IMAPFolder(config);
	
	  return null;
	}
}
