package com.digitalbarista.cat.ejb.message;

import java.util.Date;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventHandlerFactoryInterface;

/**
 * Message-Driven Bean implementation class for: MainEventHandler
 *
 */
@MessageDriven(activationConfig={
		   @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
		   @ActivationConfigProperty(propertyName="destination", propertyValue="cat/messaging/Events"),
	        @ActivationConfigProperty(propertyName="user", propertyValue="admin"),
	        @ActivationConfigProperty(propertyName="DLQUser", propertyValue="admin"),
			@ActivationConfigProperty(propertyName="password", propertyValue="admin"),
		    @ActivationConfigProperty(propertyName="DLQPassword", propertyValue="admin")
		})
@RunAsPrincipal("admin")
@RunAs("admin")
public class MainEventHandler implements MessageListener {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@EJB(name="ejb/cat/events/EventHandlerFactory")
	private CATEventHandlerFactoryInterface handlerFactory;
	
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {
    	ObjectMessage om = (ObjectMessage)message;
    	try {
    		if(om.getBooleanProperty("ignoreMe"))
    			return;
			CATEvent e = (CATEvent)om.getObject();
			if(om.propertyExists("JMS_JBOSS_SCHEDULED_DELIVERY"))
			{
				e.getArgs().put("scheduledDate", ""+om.getLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY"));
			}
			handlerFactory.getHandler(e.getType()).processEvent(e);
		} catch (Exception ex) {
			ctx.setRollbackOnly();
			ex.printStackTrace();
		}
    }

}
