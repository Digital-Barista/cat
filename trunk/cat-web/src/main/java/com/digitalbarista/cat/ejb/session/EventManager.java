package com.digitalbarista.cat.ejb.session;

import java.util.Date;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;


import com.digitalbarista.cat.message.event.CATEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class EventManagerImpl
 */
@Component
@Lazy
@Transactional(propagation=Propagation.REQUIRED)
public class EventManager {

	@Resource(mappedName="cat/messaging/Events")
	private Destination eventQueue;

	@Resource(mappedName="java:/JmsXA")
	private ConnectionFactory cf;
	
  public void queueEvent(CATEvent e)
  {
    Connection conn=null;
    Session sess=null;
    try 
    {
      conn = cf.createConnection();
      sess = conn.createSession(true, Session.SESSION_TRANSACTED);
      Message message = sess.createObjectMessage(e);
      MessageProducer prod = sess.createProducer(eventQueue);
      /*switch(e.getType())
      {
        case ConnectorFired:
        case NodeOperationCompleted:
          prod.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        break;
      }*/
      prod.send(message);
    } catch (Exception e1) {
      throw new RuntimeException("Error queueing up an event:"+e.toString(),e1);
    }
    finally
    {
      try{if(sess!=null)sess.close();}catch(Exception e1){}
      try{if(conn!=null)conn.close();}catch(Exception e1){}
    }
  }

  public void queueEventForScheduledDelivery(CATEvent e, Date scheduledDate)
  {
    Connection conn=null;
    Session sess=null;
    try 
    {
      conn = cf.createConnection();
      sess = conn.createSession(true, Session.SESSION_TRANSACTED);
      Message message = sess.createObjectMessage(e);
      message.setLongProperty("JMS_JBOSS_SCHEDULED_DELIVERY",scheduledDate.getTime());
      MessageProducer prod = sess.createProducer(eventQueue);
      prod.send(message);
    } catch (Exception e1) {
      throw new RuntimeException("Error queueing up an event:"+e.toString(),e1);
    }
    finally
    {
      try{if(sess!=null)sess.close();}catch(Exception e1){}
      try{if(conn!=null)conn.close();}catch(Exception e1){}
    }
  }
}
