package com.digitalbarista.cat.mail.folders;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import com.digitalbarista.cat.mail.MailboxConfig;

public class POP3Folder extends MailFolder
{
  private boolean flush=true;

  public POP3Folder(MailboxConfig spec)
  {
    super(spec);
    //this.flush = spec.isFlush();
  }

  protected Message[] getMessages(Folder folder) throws MessagingException {
    return folder.getMessages();
  }

  protected Store openStore(Session session) throws NoSuchProviderException {
    return session.getStore("pop3");
  }

  protected void markMessageSeen(Message message) throws MessagingException {
    message.setFlag(Flags.Flag.DELETED, true);
  }

  protected void closeStore(boolean success, Store store, Folder folder) throws MessagingException {
    try {
      if ((folder != null) && (folder.isOpen()))
        folder.close((success) && (this.flush));
    }
    finally {
      if ((store != null) && (store.isConnected()))
        store.close();
    }
  }
}