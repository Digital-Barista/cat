package com.digitalbarista.cat.ejb.message;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.SessionContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.mail.MessageSummary;
import com.digitalbarista.cat.message.event.CATEvent;

/**
 * Message-Driven Bean implementation class for: MailEcho
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType",propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination",propertyValue = "cat/messaging/MailEvents")
        })
@RunAsPrincipal(value="admin")
@RunAs("admin")
public class CATMailListener implements MessageListener{

	@EJB(name="ejb/cat/EventManager")
	private EventManager eventManager;
	
	@Resource
	SessionContext ctx;
	
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
          ctx.setRollbackOnly();
          ex.printStackTrace();
        }
    }
}
