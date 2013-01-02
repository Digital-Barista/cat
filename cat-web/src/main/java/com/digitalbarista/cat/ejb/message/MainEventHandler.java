package com.digitalbarista.cat.ejb.message;


import javax.annotation.security.RunAs;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;


import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventHandlerFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Message-Driven Bean implementation class for: MainEventHandler
 *
 */
@Component("MainEventHandler")
@RunAs("admin")
@Transactional(propagation=Propagation.REQUIRED)
public class MainEventHandler implements MessageListener {

  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private CATEventHandlerFactory handlerFactory;
	
    public void onMessage(Message message) {
    	ObjectMessage om = (ObjectMessage)message;
    	try {
			CATEvent e = (CATEvent)om.getObject();
			if(om.propertyExists("JMS_JBOSS_SCHEDULED_DELIVERY"))
			{
				e.getArgs().put("scheduledDate", ""+om.getLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY"));
			}
			handlerFactory.processEvent(e);
		} catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    }

}
