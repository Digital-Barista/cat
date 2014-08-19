package com.digitalbarista.cat.message.event.connectorfire;

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
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MessageNodeFireHandler implements ConnectorFireHandler {

  @Autowired
  private MessageManager mMan;
  
  @Autowired
  private SubscriptionManager sMan;
  
  @Autowired
  private CampaignManager cMan;
  
  @Autowired
  private EventManager eMan;

  @Autowired
  private SessionFactory sf;
  
	@Override
	public void handle(Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) 
	{
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
		sf.getCurrentSession().buildLockRequest(LockOptions.UPGRADE).lock(csl);
		sf.getCurrentSession().refresh(csl);
		String fromAddress = csl.getLastHitEntryPoint();
		EntryPointType fromType = csl.getLastHitEntryType();

		if(sMan.isSubscriberBlacklisted(s.getAddress(), fromType, fromAddress, csl.getCampaign().getClient().getPrimaryKey()))
			return;

		CampaignMessagePart messagePart = mMan.getMessagePart(cMan.getLastPublishedCampaign(mNode.getCampaignUID()), fromType, mNode.getMessageForType(fromType));
		for(String actualMessage : messagePart.getMessages())
		{
			sendMessageEvent = CATEvent.buildSendMessageRequestedEvent(fromAddress, fromType, s.getAddress(), actualMessage, mNode.getName(),mNode.getUid(),version);
			eMan.queueEvent(sendMessageEvent);
		}
			
		csl.setLastHitNode(simpleNode);
		eMan.queueEvent(CATEvent.buildNodeOperationCompletedEvent(dest.getUid(), ""+s.getPrimaryKey()));
	}

}
