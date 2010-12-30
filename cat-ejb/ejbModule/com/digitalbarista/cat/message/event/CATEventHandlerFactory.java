package com.digitalbarista.cat.message.event;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;

@Stateless
@LocalBinding(jndiBinding = "ejb/cat/events/EventHandlerFactory")
@RunAs("admin")
@RunAsPrincipal("admin")
public class CATEventHandlerFactory implements CATEventHandlerFactoryInterface{

	@EJB(name="ejb/cat/EventManager")
	private EventManager emi;
	
	@EJB(name="ejb/cat/CampaignManager")
	private CampaignManager cm;
	
	@EJB(name="ejb/cat/ContactManager")
	private ContactManager conMan;
	
	@EJB(name="ejb/cat/EventTimerManager")
	private EventTimerManager timer;

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;

	
	public CATEventHandler getHandler(CATEventType t)
	{
		switch(t)
		{
			case IncomingMessage:
				return new IncomingMessageEventHandler(em,ctx);
			case NodeOperationCompleted:
				return new NodeOperationCompletedEventHandler(em,ctx);
			case ConnectorFired:
				return new ConnectorFiredEventHandler(em,ctx);
			case MessageSendRequested:
				return new MessageSendRequestEventHandler(em,ctx);
			default:
				throw new IllegalArgumentException("Unknown event type:  No configured factory: "+t.toString());
		}
	}
	
}
