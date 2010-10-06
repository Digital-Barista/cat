package com.digitalbarista.cat.message.event.connectorfire;

import java.util.List;

import javax.ejb.SessionContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

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
		String fromAddress = csl.getLastHitEntryPoint();
		EntryPointType fromType = csl.getLastHitEntryType();

		if(sMan.isSubscriberBlacklisted(s.getPrimaryKey(), fromAddress, fromType))
			return;

		CampaignMessagePart messagePart = mMan.getMessagePart(cMan.getDetailedCampaign(mNode.getCampaignUID()), fromType, mNode.getMessage());
		for(String actualMessage : messagePart.getMessages())
		{
			switch(fromType)
			{
				
				case Email:
					sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getEmail(), actualMessage, mNode.getName(),mNode.getUid(),version);
					break;
				
				case SMS:
					sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getPhoneNumber(), actualMessage, mNode.getName(),mNode.getUid(),version);
					break;
					
				case Twitter:
					sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterID(), actualMessage, mNode.getName(),mNode.getUid(),version);
					break;
					
				case Facebook:
					sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getFacebookID(), actualMessage, mNode.getName(),mNode.getUid(),version);
					break;
					
				default:
					throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
			}
			eMan.queueEvent(sendMessageEvent);
		}
			
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}

}
