package com.digitalbarista.cat.message.event.connectorfire;

import javax.persistence.EntityManager;

import org.jboss.util.NotImplementedException;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.message.event.CATEvent;

public abstract class ConnectorFireHandler {

	protected ConnectorFireHandler(){}
	
	public static ConnectorFireHandler getHandler(NodeType type)
	{
		switch(type)
		{
			case Entry:
			case OutgoingEntry:
				return new ConnectorFireHandler()
				{
					public void handle(EntityManager em, CampaignManager cMan, EventManager eMan, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) {
						throw new IllegalStateException("ConnectorDO cannot have a entry node for a destination.");
					}
				};
			
			case Termination:
				return new ConnectorFireHandler()
				{
					public void handle(EntityManager em, CampaignManager cMan, EventManager eMan, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e) {
						throw new NotImplementedException("Haven't built this type of node yet.");
					}
				};
				
			case Message:
				return new MessageNodeFireHandler();
				
			case Tagging:
				return new TaggingNodeFireHandler();
				
			case Coupon:
				return new CouponNodeFireHandler();
				
			default:
				throw new IllegalArgumentException("Invalid Node type for connector fired event: "+type.toString());
		}
	}
	
	public abstract void handle(EntityManager em, CampaignManager cMan, EventManager eMan, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e);
}
