package com.digitalbarista.cat.ejb.session;

import java.util.Date;
import java.util.Enumeration;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.message.event.CATEvent;

/**
 * Session Bean implementation class EventManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/EventManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class EventManagerImpl implements EventManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.

	@Resource(mappedName="cat/messaging/Events")
	private Destination eventQueue;

	@Resource(mappedName="java:/JmsXA")
	private ConnectionFactory cf;
	
    @RolesAllowed("admin")
    public void queueEvent(CATEvent e){
    	Connection conn=null;
    	Session sess=null;
    	try {
			conn = cf.createConnection();
			sess = conn.createSession(true, Session.SESSION_TRANSACTED);
			Message message = sess.createObjectMessage(e);
			message.setStringProperty("CATEventSource", e.getSource());
			MessageProducer prod = sess.createProducer(eventQueue);
			prod.send(message);
    	} catch (Exception e1) {
			ctx.setRollbackOnly();
			throw new RuntimeException("Error queueing up an event:"+e.toString(),e1);
		}
    	finally
    	{
    		try{if(sess!=null)sess.close();}catch(Exception e1){}
    		try{if(conn!=null)conn.close();}catch(Exception e1){}
    	}
    }

    @RolesAllowed("admin")
    public void queueEventForScheduledDelivery(CATEvent e, Date scheduledDate){
    	Connection conn=null;
    	Session sess=null;
    	try {
			conn = cf.createConnection();
			sess = conn.createSession(true, Session.SESSION_TRANSACTED);
			Message message = sess.createObjectMessage(e);
			message.setStringProperty("CATEventSource", e.getSource());
			message.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY",scheduledDate.getTime());
			MessageProducer prod = sess.createProducer(eventQueue);
			prod.send(message);
    	} catch (Exception e1) {
			ctx.setRollbackOnly();
			throw new RuntimeException("Error queueing up an event:"+e.toString(),e1);
		}
    	finally
    	{
    		try{if(sess!=null)sess.close();}catch(Exception e1){}
    		try{if(conn!=null)conn.close();}catch(Exception e1){}
    	}
    }

	@Override
	public void deQueueAllScheduledForConnector(String uid) {
		Connection conn=null;
		Session sess=null;
		try
		{
			conn = cf.createConnection();
			sess = conn.createSession(true, Session.SESSION_TRANSACTED);
			MessageConsumer consumer = sess.createConsumer(eventQueue,"CATEventSource="+uid);
			Message msg;
			do
			{
				msg = consumer.receiveNoWait();
				if(msg==null) break;
				msg.acknowledge();
			}while(true);
		} catch (Exception e)
		{
			ctx.setRollbackOnly();
			throw new RuntimeException("Error de-queueing events for connector:"+uid,e);
		}
		finally
		{
    		try{if(sess!=null)sess.close();}catch(Exception e1){}
    		try{if(conn!=null)conn.close();}catch(Exception e1){}
		}
	}

}
