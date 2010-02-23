package com.digitalbarista.cat.message.event;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;

import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;

public abstract class CATEventHandler {

	private EntityManager em;
	private SessionContext sc;
	private EventManager eventManager;
	private CampaignManager campaignManager;
	private ContactManager contactManager;
	private EventTimerManager timer;
	
	protected CATEventHandler(EntityManager newEM, SessionContext newSC, EventManager newEventManager, CampaignManager newCampaignManager,ContactManager newContactManager, EventTimerManager newTimer)
	{
		em=newEM;
		sc=newSC;
		eventManager=newEventManager;
		campaignManager=newCampaignManager;
		contactManager=newContactManager;
		timer=newTimer;
	}
	
	public abstract void processEvent(CATEvent e);

	protected EntityManager getEntityManager() {
		return em;
	}

	protected SessionContext getSessionContext() {
		return sc;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public CampaignManager getCampaignManager() {
		return campaignManager;
	}

	public ContactManager getContactManager() {
		return contactManager;
	}

	public EventTimerManager getTimer()
	{
		return timer;
	}
}
