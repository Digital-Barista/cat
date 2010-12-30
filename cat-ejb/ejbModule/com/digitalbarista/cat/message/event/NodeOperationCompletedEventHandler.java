package com.digitalbarista.cat.message.event;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;

import com.digitalbarista.cat.business.CalendarConnector;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.ImmediateConnector;
import com.digitalbarista.cat.business.IntervalConnector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.ContactManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.ejb.session.EventTimerManager;

public class NodeOperationCompletedEventHandler extends CATEventHandler {

	public NodeOperationCompletedEventHandler(EntityManager newEM,
			SessionContext newSC) {
		super(newEM, newSC);
	}

	@Override
	public void processEvent(CATEvent e) {
		Integer version = getCampaignManager().getSimpleNode(e.getSource()).getCampaign().getCurrentVersion()-1;
		Node publishedNode = getCampaignManager().getSpecificNodeVersion(e.getSource(), version);
		Connector connector;
		ImmediateConnector ic=null;
		CalendarConnector pastDue=null;
		Set<IntervalConnector> intervals=new HashSet<IntervalConnector>();
		for(String connUID : publishedNode.getDownstreamConnections())
		{
			connector = getCampaignManager().getSpecificConnectorVersion(connUID, version);
			if(connector==null)
				continue;
			if(connector.getType().equals(ConnectorType.Immediate))
			{
				if(ic!=null)
					throw new IllegalStateException("Multiple Immediate Connectors attached to a node! - NodeUID="+publishedNode.getUid());
				ic=(ImmediateConnector)connector;
				continue;
			}
			if(connector.getType().equals(ConnectorType.Interval))
			{
				intervals.add((IntervalConnector)connector);
				continue;
			}
			if(connector.getType().equals(ConnectorType.Calendar))
			{
				if(((CalendarConnector)connector).getTargetDate().before(new Date()))
				{
					if(pastDue==null || ((CalendarConnector)connector).getTargetDate().after(pastDue.getTargetDate()))
						pastDue=(CalendarConnector)connector;
					continue;
				}				
			}
		}
		if(ic!=null)
		{
			CATEvent connectorFiredEvent;
			if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers))
				connectorFiredEvent=CATEvent.buildFireConnectorForAllSubscribersEvent(ic.getUid(),-1);
			else
				connectorFiredEvent=CATEvent.buildFireConnectorForSubscriberEvent(ic.getUid(), e.getTarget(),-1);
			getEventManager().queueEvent(connectorFiredEvent);
		} else if (pastDue!=null) {
			CATEvent connectorFiredEvent;
			if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers))
				connectorFiredEvent=CATEvent.buildFireConnectorForAllSubscribersEvent(pastDue.getUid(),-1);
			else
				connectorFiredEvent=CATEvent.buildFireConnectorForSubscriberEvent(pastDue.getUid(), e.getTarget(),-1);
			getEventManager().queueEvent(connectorFiredEvent);
		}
		Calendar c = Calendar.getInstance();
		Date currentTime = new Date();
		for(IntervalConnector conn : intervals)
		{
			c.setTime(currentTime);
			switch(conn.getIntervalType())
			{
				case Minutes:
					c.add(Calendar.MINUTE, conn.getInterval().intValue());
					break;
				case Hours:
					c.add(Calendar.HOUR, conn.getInterval().intValue());
					break;
				case Days:
					c.add(Calendar.DATE, conn.getInterval().intValue());
					break;
				case Weeks:
					c.add(Calendar.WEEK_OF_YEAR, conn.getInterval().intValue());
					break;
				case Months:
					c.add(Calendar.MONTH, conn.getInterval().intValue());
					break;
			}
			getTimer().setTimer(conn.getUid(), e.getTarget(), -1, CATEventType.ConnectorFired, c.getTime());
		}
	}

}
