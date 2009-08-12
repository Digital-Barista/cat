package com.digitalbarista.cat.mail.folders;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import com.digitalbarista.cat.mail.MailboxConfig;

public class IMAPFolder extends MailFolder
{
  public IMAPFolder(MailboxConfig spec)
  {
    super(spec);
  }

  protected Message[] getMessages(Folder folder) throws MessagingException {
    if (folder.getUnreadMessageCount() > 0) {
      int newCount = folder.getUnreadMessageCount();
      int messageCount = folder.getMessageCount();

      return folder.getMessages(messageCount - newCount + 1, messageCount);
    }

    return new Message[0];
  }

  protected Store openStore(Session session) throws NoSuchProviderException
  {
    return session.getStore("imap");
  }

  protected void markMessageSeen(Message message) throws MessagingException {
    message.setFlag(Flags.Flag.SEEN, true);
  }

  protected void closeStore(boolean success, Store store, Folder folder) throws MessagingException {
    try {
      if ((folder != null) && (folder.isOpen()))
        folder.close(success);
    }
    finally {
      if ((store != null) && (store.isConnected()))
        store.close();
    }
  }
}