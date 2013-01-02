package com.digitalbarista.cat.ejb.message;

import javax.annotation.security.RunAs;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.mail.MessageSummary;
import com.digitalbarista.cat.message.event.CATEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Message-Driven Bean implementation class for: MailEcho
 *
 */
@Component("CATMailListener")
@RunAs("admin")
@Transactional(propagation=Propagation.REQUIRED)
public class CATMailListener implements MessageListener{

  @Autowired
  private EventManager eventManager;
	
    public void onMessage(Message message) {
        try
        {
	      ObjectMessage om = (ObjectMessage)message;
	      MessageSummary ms = (MessageSummary)om.getObject();
	      CATEvent e = CATEvent.buildIncomingEmailEvent(
	    		  ms.getReplyTo()[0].toString(),
	    		  ms.getRecipients()[0].toString(), 
	    		  ms.getSubject());
	      eventManager.queueEvent(e);
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
    }
}
