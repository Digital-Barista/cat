package com.digitalbarista.cat.message.event;

import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;
import com.digitalbarista.cat.message.event.connectorfire.ConnectorFireHandler;

public class ConnectorFiredEventHandler extends CATEventHandler {

	private Logger log = LogManager.getLogger(ConnectorFiredEventHandler.class);
	
	public ConnectorFiredEventHandler(EntityManager newEM,
			SessionContext newSC, 
			EventManager newEventManager,
			CampaignManager newCampaignManager,
			ContactManager newContactManager,
			EventTimerManager timer) {
		super(newEM, newSC, newEventManager, newCampaignManager, newContactManager, timer);
	}

	@Override
	public void processEvent(CATEvent e) {
		Integer version = getCampaignManager().getSimpleConnector(e.getSource()).getCampaign().getCurrentVersion()-1;
		Connector conn = getCampaignManager().getSpecificConnectorVersion(e.getSource(), version);
		if(conn.getDestinationUID()==null)
			throw new IllegalStateException("ConnectorDO has no destination node. connector uid="+conn.getUid());
		Node dest = getCampaignManager().getSpecificNodeVersion(conn.getDestinationUID(), version);
		
		//validate fired connection
		if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
		{
			SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
			CampaignDO camp = getCampaignManager().getSimpleCampaign(conn.getCampaignUID());
			if(s==null || !s.getSubscriptions().containsKey(camp))
			{
				log.warn("subscriber pk="+e.getTarget()+" was not subscribed to campaign UID="+conn.getCampaignUID()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
			Node source = getCampaignManager().getSpecificNodeVersion(conn.getSourceNodeUID(), version);
			String subscriberCurrent=s.getSubscriptions().get(camp).getLastHitNode().getUID();
			if(source==null || !source.getUid().equals(subscriberCurrent))
			{
				log.warn("subscriber pk="+e.getTarget()+" is not on node uid="+source.getUid()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
		}
		
		if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
		{
			SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
			ConnectorFireHandler.getHandler(dest.getType())
				.handle(getEntityManager(), getSessionContext(), conn, dest, version, s, e);
		} else if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers)){
			Query q = getEntityManager().createNamedQuery("all.subscribers.on.node");
			q.setParameter("nodeUID", conn.getSourceNodeUID());
			List<SubscriberDO> subs = (List<SubscriberDO>)q.getResultList();
			for(SubscriberDO s : subs)
			{
				ConnectorFireHandler.getHandler(dest.getType())
					.handle(getEntityManager(), getSessionContext(), conn, dest, version, s, e);
			}
		}
	}

}
