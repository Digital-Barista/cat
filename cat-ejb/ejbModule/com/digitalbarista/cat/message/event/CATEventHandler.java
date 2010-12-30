package com.digitalbarista.cat.message.event;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;

import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;
import com.digitalbarista.cat.ejb.session.SubscriptionManager;

public abstract class CATEventHandler {

	private EntityManager em;
	private SessionContext sc;
	private EventManager eventManager;
	private CampaignManager campaignManager;
	private ContactManager contactManager;
	private SubscriptionManager subscriptionManager;
	private EventTimerManager timer;
	
	protected CATEventHandler(EntityManager newEM, SessionContext newSC)
	{
		em=newEM;
		sc=newSC;
	}
	
	public abstract void processEvent(CATEvent e);

	protected EntityManager getEntityManager() {
		return em;
	}

	protected SessionContext getSessionContext() {
		return sc;
	}

	public EventManager getEventManager() {
		if(eventManager==null)
			eventManager = (EventManager)getSessionContext().lookup("ejb/cat/EventManager");
		return eventManager;
	}

	public CampaignManager getCampaignManager() {
		if(campaignManager==null)
			campaignManager = (CampaignManager)getSessionContext().lookup("ejb/cat/CampaignManager");
		return campaignManager;
	}

	public ContactManager getContactManager() {
		if(contactManager==null)
			contactManager = (ContactManager)getSessionContext().lookup("ejb/cat/ContactManager");
		return contactManager;
	}

	public SubscriptionManager getSubscriptionManager()
	{
		if(subscriptionManager==null)
			subscriptionManager = (SubscriptionManager)getSessionContext().lookup("ejb/cat/SubscriptionManager");
		return subscriptionManager;
	}
	
	public EventTimerManager getTimer()
	{
		if(timer==null)
			timer = (EventTimerManager)getSessionContext().lookup("ejb/cat/EventTimerManager");
		return timer;
	}
}
