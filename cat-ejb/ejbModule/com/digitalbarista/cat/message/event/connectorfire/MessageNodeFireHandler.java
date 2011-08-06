package com.digitalbarista.cat.message.event.connectorfire;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.MessageManager;
import com.digitalbarista.cat.ejb.session.SubscriptionManager;
import com.digitalbarista.cat.message.event.CATEvent;

public class MessageNodeFireHandler extends ConnectorFireHandler {

	@Override
	public void handle(EntityManager em, SessionContext ctx, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) 
	{
		MessageManager mMan = (MessageManager)ctx.lookup("ejb/cat/MessageManager");
		SubscriptionManager sMan = (SubscriptionManager)ctx.lookup("ejb/cat/SubscriptionManager");
		CampaignManager cMan = (CampaignManager)ctx.lookup("ejb/cat/CampaignManager");
		EventManager eMan = (EventManager)ctx.lookup("ejb/cat/EventManager");

		MessageNode mNode = (MessageNode)dest;
		CATEvent sendMessageEvent=null;
		NodeDO simpleNode=cMan.getSimpleNode(mNode.getUid());
		
		
		CampaignSubscriberLinkDO csl=null;
		for(CampaignDO subCamp : s.getSubscriptions().keySet())
		{
			if(subCamp.getUID().equalsIgnoreCase(simpleNode.getCampaign().getUID()))
			{
				csl=s.getSubscriptions().get(subCamp);
				break;
			}
		}
		em.lock(csl, LockModeType.WRITE);
		em.refresh(csl);
		String fromAddress = csl.getLastHitEntryPoint();
		EntryPointType fromType = csl.getLastHitEntryType();

		if(sMan.isSubscriberBlacklisted(s.getAddress(), fromType, fromAddress, csl.getCampaign().getClient().getPrimaryKey()))
			return;

		CampaignMessagePart messagePart = mMan.getMessagePart(cMan.getDetailedCampaign(mNode.getCampaignUID()), fromType, mNode.getMessageForType(fromType));
		for(String actualMessage : messagePart.getMessages())
		{
			sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getAddress(), actualMessage, mNode.getName(),mNode.getUid(),version);
			eMan.queueEvent(sendMessageEvent);
		}
			
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}

}
