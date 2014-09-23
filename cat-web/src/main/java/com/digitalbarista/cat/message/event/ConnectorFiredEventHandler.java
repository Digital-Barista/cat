package com.digitalbarista.cat.message.event;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import com.digitalbarista.cat.ejb.session.EventManager;
import com.digitalbarista.cat.message.event.connectorfire.ConnectorFireHandlerFactory;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConnectorFiredEventHandler implements CATEventHandler {

	private Logger log = LogManager.getLogger(ConnectorFiredEventHandler.class);

  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private CampaignManager campaignManager;
  
  @Autowired
  private EventManager eventManager;
  
  @Autowired
  private ConnectorFireHandlerFactory handler;
  
	@Override
        @Transactional
	public void processEvent(CATEvent e) {
		Integer version = campaignManager.getCurrentCampaignVersionForConnector(e.getSource())-1;
		Connector conn = campaignManager.getSpecificConnectorVersion(e.getSource(), version);
		if(conn==null)
		{
			log.warn("Connector "+e.getSource()+" version "+version+" no longer exists and was likely deleted on the last publish.");
			return;
		}
		if(conn.getType()==ConnectorType.Calendar)
		{
			if(e.getArgs().containsKey("version"))
			{
				Integer sentVersion = new Integer(e.getArgs().get("version"));
				if(sentVersion!=null && !sentVersion.equals(-1) && !sentVersion.equals(version))
				{
					log.info("Connector "+conn.getUid()+" fire event came from an old version: "+sentVersion+" current:"+version);
					return;
				}
			}
		}
		if(conn.getDestinationUID()==null)
			throw new IllegalStateException("ConnectorDO has no destination node. connector uid="+conn.getUid());
		Node dest = campaignManager.getSpecificNodeVersion(conn.getDestinationUID(), version);
		
		//validate fired connection
		if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
		{
			SubscriberDO s = (SubscriberDO)sf.getCurrentSession().get(SubscriberDO.class, new Long(e.getTarget()));
			CampaignDO camp = campaignManager.getSimpleCampaign(conn.getCampaignUID());
			if(s==null || camp==null)
			{
				log.warn("subscriber pk="+e.getTarget()+" was not subscribed to campaign UID="+conn.getCampaignUID()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
			CampaignSubscriberLinkDO csl=null;
			for(CampaignDO subCamp : s.getSubscriptions().keySet())
			{
				if(subCamp.getUID().equalsIgnoreCase(camp.getUID()))
				{
					csl=s.getSubscriptions().get(subCamp);
					break;
				}
			}
			if(csl==null)
			{
				log.warn("subscriber pk="+e.getTarget()+" was not subscribed to campaign UID="+conn.getCampaignUID()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
			sf.getCurrentSession().buildLockRequest(LockOptions.UPGRADE).lock(csl);
			sf.getCurrentSession().refresh(csl);
			Node source = campaignManager.getSpecificNodeVersion(conn.getSourceNodeUID(), version);
			String subscriberCurrent=csl.getLastHitNode().getUID();
			if(source==null || !source.getUid().equals(subscriberCurrent))
			{
				log.warn("subscriber pk="+e.getTarget()+" is not on node uid="+source.getUid()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
				return;
			}
		}
		
		if(e.getTargetType().equals(CATTargetType.SpecificSubscriber))
		{
			SubscriberDO s = (SubscriberDO)sf.getCurrentSession().get(SubscriberDO.class, new Long(e.getTarget()));
			handler.handle(dest.getType(), conn, dest, version, s, e);
		} else if(e.getTargetType().equals(CATTargetType.AllAppliedSubscribers)){
			Query q = sf.getCurrentSession().getNamedQuery("all.subscribers.on.node");
			q.setParameter("nodeUID", conn.getSourceNodeUID());
			List<SubscriberDO> subs = (List<SubscriberDO>)q.list();
      for(SubscriberDO s : subs)
      {
        eventManager.queueEvent(CATEvent.buildFireConnectorForSubscriberEvent(conn.getUid(),""+s.getPrimaryKey(),-1));
      }
//			for(SubscriberDO s : subs)
//			{
//				CampaignSubscriberLinkDO csl=null;
//				for(CampaignDO subCamp : s.getSubscriptions().keySet())
//				{
//					if(subCamp.getUID().equalsIgnoreCase(conn.getCampaignUID()))
//					{
//						csl=s.getSubscriptions().get(subCamp);
//						break;
//					}
//				}
//				if(csl==null)
//				{
//					log.warn("subscriber pk="+e.getTarget()+" was not subscribed to campaign UID="+conn.getCampaignUID()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
//					return;
//				}
//				getCampaignManager().getLastPublishedCampaign(csl.getCampaign().getUID());//Warming the cache.  Keeps deadlocks from happening. on the CSL lock!
//				getEntityManager().lock(csl, LockModeType.WRITE);
//				getEntityManager().refresh(csl);
//				Node source = getCampaignManager().getSpecificNodeVersion(conn.getSourceNodeUID(), version);
//				String subscriberCurrent=csl.getLastHitNode().getUID();
//				if(source==null || !source.getUid().equals(subscriberCurrent))
//				{
//					log.warn("subscriber pk="+csl.getSubscriber().getPrimaryKey()+" is not on node uid="+source.getUid()+".  ConnectorDO "+conn.getUid()+" will not be fired.");
//					return;
//				}
//				ConnectorFireHandler.getHandler(dest.getType())
//					.handle(getEntityManager(), getSessionContext(), conn, dest, version, s, e);
			}
		}
	}
