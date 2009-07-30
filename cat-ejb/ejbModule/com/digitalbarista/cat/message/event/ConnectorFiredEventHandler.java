package com.digitalbarista.cat.message.event;

import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.util.NotImplementedException;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;

public class ConnectorFiredEventHandler extends CATEventHandler {

	private Logger log = LogManager.getLogger(ConnectorFiredEventHandler.class);
	
	public ConnectorFiredEventHandler(EntityManager newEM,
			SessionContext newSC, 
			EventManager newEventManager,
			CampaignManager newCampaignManager,
			EventTimerManager timer) {
		super(newEM, newSC, newEventManager, newCampaignManager, timer);
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
		
		//At this point, fired connection should be valid, so do it.
		switch(dest.getType())
		{
			case Entry:
				throw new IllegalStateException("ConnectorDO cannot have a entry node for a destination.");

			case Termination:
				throw new NotImplementedException("Haven't built this type of node yet.");
				
			case Message:
			{
				MessageNode mNode = (MessageNode)dest;
				CATEvent sendMessageEvent=null;
				NodeDO simpleNode=getCampaignManager().getSimpleNode(mNode.getUid());
				String fromAddress = simpleNode.getCampaign().getDefaultFrom();
				if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
				{
					SubscriberDO s = getEntityManager().find(SubscriberDO.class, new Long(e.getTarget()));
					String actualMessage = mNode.getMessage();
					String campaignAddIn = simpleNode.getCampaign().getAddInMessage();
					String clientAddIn = simpleNode.getCampaign().getClient().getUserAddInMessage();
					String adminAddIn = simpleNode.getCampaign().getClient().getAdminAddInMessage();
					
					if(campaignAddIn!=null && campaignAddIn.trim().length()>0)
						actualMessage+=campaignAddIn;
					else if(clientAddIn!=null && clientAddIn.trim().length()>0)
						actualMessage+=clientAddIn;
					
					if(adminAddIn!=null && adminAddIn.trim().length()>0)
						actualMessage+=adminAddIn;
					
					switch(simpleNode.getCampaign().getCampaignType())
					{
						
						case Email:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, simpleNode.getCampaign().getCampaignType(), s.getEmail(), actualMessage, mNode.getName(),mNode.getUid(),version);
							break;
						
						case SMS:
							sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, simpleNode.getCampaign().getCampaignType(), s.getPhoneNumber(), actualMessage, mNode.getName(),mNode.getUid(),version);
							break;
							
						default:
							throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
					}
					s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
					getEventManager().queueEvent(sendMessageEvent);
					getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), e.getTarget()));
				} else if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers)){
					Query q = getEntityManager().createNamedQuery("all.subscribers.on.node");
					q.setParameter("nodeUID", conn.getSourceNodeUID());
					List<SubscriberDO> subs = (List<SubscriberDO>)q.getResultList();
					for(SubscriberDO s : subs)
					{
						String actualMessage = mNode.getMessage();
						String campaignAddIn = simpleNode.getCampaign().getAddInMessage();
						String clientAddIn = simpleNode.getCampaign().getClient().getUserAddInMessage();
						String adminAddIn = simpleNode.getCampaign().getClient().getAdminAddInMessage();
						
						if(campaignAddIn!=null && campaignAddIn.trim().length()>0)
							actualMessage+=campaignAddIn;
						else if(clientAddIn!=null && clientAddIn.trim().length()>0)
							actualMessage+=clientAddIn;
						
						if(adminAddIn!=null && adminAddIn.trim().length()>0)
							actualMessage+=adminAddIn;
						
						switch(simpleNode.getCampaign().getCampaignType())
						{
							case Email:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, simpleNode.getCampaign().getCampaignType(), s.getEmail(), actualMessage, mNode.getName(), mNode.getUid(), version);
								break;
							
							case SMS:
								sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, simpleNode.getCampaign().getCampaignType(), s.getPhoneNumber(), actualMessage, mNode.getName(), mNode.getUid(), version);
						
							default:
								throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
						}
						s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
						getEntityManager().persist(simpleNode);
						getEventManager().queueEvent(sendMessageEvent);
						getEventManager().queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), e.getTarget()));
					}
				}
			}
			break; //End Message node type
			
			default:
				throw new IllegalArgumentException("Invalid target type for a ConnectorFired event. --"+e.getTargetType());
		}
	}

}
