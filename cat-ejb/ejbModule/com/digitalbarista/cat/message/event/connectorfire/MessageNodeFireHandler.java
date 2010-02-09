package com.digitalbarista.cat.message.event.connectorfire;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.digitalbarista.cat.business.CampaignMessagePart;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.MessageManager;
import com.digitalbarista.cat.message.event.CATEvent;

public class MessageNodeFireHandler extends ConnectorFireHandler {

	@Override
	public void handle(EntityManager em, CampaignManager cMan, EventManager eMan, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) 
	{
		MessageNode mNode = (MessageNode)dest;
		CATEvent sendMessageEvent=null;
		NodeDO simpleNode=cMan.getSimpleNode(mNode.getUid());
		
		MessageManager mMan = null;
		
		try
		{
			InitialContext ic = new InitialContext();
			mMan = (MessageManager)ic.lookup("ejb/cat/MessageManager");
		}catch(NamingException ex)
		{
			throw new RuntimeException("Unable to retrieve the message manager.",ex);
		}
		
		List<CampaignMessagePart> messageParts = mMan.getMessageParts(cMan.getDetailedCampaign(mNode.getCampaignUID()), mNode.getMessage());
		
		String fromAddress = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryPoint();
		EntryPointType fromType = s.getSubscriptions().get(simpleNode.getCampaign()).getLastHitEntryType();
		for(CampaignMessagePart messagePart : messageParts)
		{
			if(messagePart.getEntryType()!=fromType)
				continue;
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
						sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getTwitterUsername(), actualMessage, mNode.getName(),mNode.getUid(),version);
						break;
						
					default:
						throw new IllegalStateException("NodeDO must be either Email or SMS . . . mixed or other types are not supported.");
				}
				eMan.queueEvent(sendMessageEvent);
			}
		}
		s.getSubscriptions().get(simpleNode.getCampaign()).setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}

}
