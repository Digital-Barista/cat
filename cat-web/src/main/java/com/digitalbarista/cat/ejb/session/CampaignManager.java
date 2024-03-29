package com.digitalbarista.cat.ejb.session;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.digitalbarista.cat.business.*;
import com.digitalbarista.cat.util.CriteriaUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.criteria.ContactSearchCriteria;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.AddInMessageType;
import com.digitalbarista.cat.data.CampaignConnectorLinkDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignEntryPointDO;
import com.digitalbarista.cat.data.CampaignInfoDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.CampaignNodeLinkDO;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.CampaignSubscriberLinkDO;
import com.digitalbarista.cat.data.CampaignVersionDO;
import com.digitalbarista.cat.data.CampaignVersionStatus;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ConnectionPoint;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.ContactDO;
import com.digitalbarista.cat.data.ContactTagDO;
import com.digitalbarista.cat.data.CouponOfferDO;
import com.digitalbarista.cat.data.CouponRedemptionDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.LayoutInfoDO;
import com.digitalbarista.cat.data.NodeConnectorLinkDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.ejb.session.CacheAccessManager.CacheName;
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventType;
import com.digitalbarista.cat.util.MultiValueMap;
import com.digitalbarista.cat.util.SecurityUtil;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class CampaignManagerImpl
 */

@Component("CampaignManager")
@Lazy
@Transactional(propagation=Propagation.REQUIRED)
public class CampaignManager {

	Logger log = LogManager.getLogger(getClass());
		
  @Autowired
  private SessionFactory sf;

  @Autowired
  EventTimerManager timer;

  @Autowired
  EventManager eventManager;

  @Autowired
  UserManager userManager;

  @Autowired
  LayoutManager layoutManager;

  @Autowired
  ContactManager contactManager;

  @Autowired
  CacheAccessManager cacheAccessManager;

  @Autowired
  SubscriptionManager subscriptionManager;

  @Autowired
  SecurityUtil securityUtil;

  public ServiceResponse test()
  {
      ServiceResponse ret = new ServiceResponse();
      ret.setMetadata(new ServiceMetadata());
      ret.setErrors(Arrays.asList(new ServiceError[]{new ServiceError()}));
      return ret;
  }

	@SuppressWarnings("unchecked")
  public List<Campaign> getAllCampaigns() 
	{
		return getCampaigns(null);
	}

	@SuppressWarnings("unchecked")
  public List<Campaign> getAllTemplates() 
	{
		return getCampaignTemplates(null);
	}

  public List<Campaign> getCampaigns(List<Long> clientIDs)
	{
		List<Campaign> ret = new ArrayList<Campaign>();
		Campaign c;
		
		List<Long> allowedClientIDs = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIDs);
		
		if (allowedClientIDs.size() > 0)
		{
			Criteria crit = sf.getCurrentSession().createCriteria(CampaignDO.class);
			crit.add(Restrictions.eq("status", CampaignStatus.Active));
			crit.add(Restrictions.eq("mode", CampaignMode.Normal));
			crit.add(Restrictions.in("client.id", allowedClientIDs));
			
	
			List<CampaignDO> campList = (List<CampaignDO>)crit.list();
			Map<Long,Integer> campCounts = new HashMap<Long,Integer>();
			if(campList!=null && campList.size()>1)
			{
				List<Long> campIDList = new ArrayList<Long>();
				for(CampaignDO camp : campList)
					campIDList.add(camp.getPrimaryKey());
				Criteria countCrit = sf.getCurrentSession().createCriteria(CampaignSubscriberLinkDO.class, "csl");
				countCrit.add(Restrictions.in("campaign.id", campIDList));
				countCrit.add(Restrictions.eq("active",true));
				ProjectionList pList = Projections.projectionList();
				pList.add(Projections.count("id"));
				pList.add(Projections.property("campaign.id"));
				pList.add(Projections.groupProperty("campaign.id"));
				countCrit.setProjection(pList);
				List<Object[]> result = (List<Object[]>)countCrit.list();
				for(Object[] row : result)
					campCounts.put((Long)row[1], ((Long)row[0]).intValue());
			}
			
				
			for(CampaignDO cmp : campList)
			{
				c = new Campaign();
				c.copyFrom(cmp);
				if(campCounts.containsKey(cmp.getPrimaryKey()))
					c.setSubscriberCount(campCounts.get(cmp.getPrimaryKey()));
				ret.add(c);
			}
		}
		
		return ret;
	}

    public ServiceResponse getBroadcastCampaigns(List<Long> clientIDs, ServiceMetadata metadata)
    {
        ServiceMetadata meta = metadata != null ? metadata : new ServiceMetadata();
        List<BroadcastInfo> infos = new ArrayList<BroadcastInfo>();
        BroadcastInfo c;

        List<Long> allowedClientIDs = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIDs);

        if (allowedClientIDs.size() > 0)
        {
            Criteria crit = sf.getCurrentSession().createCriteria(CampaignDO.class);
            crit.add(Restrictions.eq("status", CampaignStatus.Active));
            crit.add(Restrictions.eq("mode", CampaignMode.Broadcast));
            crit.add(Restrictions.in("client.id", allowedClientIDs));

            meta.setTotal(CriteriaUtil.getTotalResultCount(crit));
            CriteriaUtil.applyPagingInfo(crit, meta);

            List<CampaignDO> campList = (List<CampaignDO>)crit.list();
            Map<Long,Integer> campCounts = new HashMap<Long,Integer>();
            if(campList!=null && campList.size()>1)
            {
                List<Long> campIDList = new ArrayList<Long>();
                for(CampaignDO camp : campList)
                    campIDList.add(camp.getPrimaryKey());
                Criteria countCrit = sf.getCurrentSession().createCriteria(CampaignSubscriberLinkDO.class, "csl");
                countCrit.add(Restrictions.in("campaign.id", campIDList));
                countCrit.add(Restrictions.eq("active",true));
                ProjectionList pList = Projections.projectionList();
                pList.add(Projections.count("id"));
                pList.add(Projections.property("campaign.id"));
                pList.add(Projections.groupProperty("campaign.id"));
                countCrit.setProjection(pList);
                List<Object[]> result = (List<Object[]>)countCrit.list();
                for(Object[] row : result)
                    campCounts.put((Long)row[1], ((Long)row[0]).intValue());
            }

            for(CampaignDO cmp : campList)
            {
                c = new BroadcastInfo();
                c.copyFrom(cmp);
                if(campCounts.containsKey(cmp.getPrimaryKey()))
                    c.setSubscriberCount(campCounts.get(cmp.getPrimaryKey()));
                if(c.getIsCoupon())
                {
                    Criteria couponCount = sf.getCurrentSession().createCriteria(CouponRedemptionDO.class, "cr");
                    couponCount.createAlias("couponResponse", "resp");
                    couponCount.add(Restrictions.eq("resp.couponOffer.id", c.getCouponId()));
                    couponCount.setProjection(Projections.rowCount());
                    c.setCouponRedemptionCount(((Long)couponCount.uniqueResult()).intValue());
                }
                infos.add(c);
            }
        }

        ServiceResponse ret = new ServiceResponse();
        ret.setResult(infos);
        ret.setMetadata(meta);

        return ret;
    }

	public List<BroadcastInfo> getBroadcastCampaigns(List<Long> clientIDs)
	{
    return (List<BroadcastInfo>)getBroadcastCampaigns(clientIDs,null).getResult();
	}
	
	public List<Campaign> getCampaignTemplates(List<Long> clientIDs)
	{
		List<Campaign> ret = new ArrayList<Campaign>();
		Campaign c;
		List<Long> allowedClientIDs = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIDs);
		
		if (allowedClientIDs.size() > 0)
		{
			Criteria crit = sf.getCurrentSession().createCriteria(CampaignDO.class);
			crit.add(Restrictions.eq("status", CampaignStatus.Active));
			crit.add(Restrictions.eq("mode", CampaignMode.Template));
			crit.add(Restrictions.in("client.id", allowedClientIDs));
			
			for(CampaignDO cmp : (List<CampaignDO>)crit.list())
			{
				c = new Campaign();
				c.copyFrom(cmp);
				ret.add(c);
			}
		}
		
		return ret;
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	public ConnectorDO getSimpleConnector(String connectorUUID)
	{
			Criteria crit = sf.getCurrentSession().createCriteria(ConnectorDO.class);
			crit.add(Restrictions.eq("UID", connectorUUID));
			crit.setCacheable(true);
			crit.setCacheRegion("query/simpleConnector");
			ConnectorDO ret = (ConnectorDO)crit.uniqueResult();
			
			if(ret==null)
				return null;
			
			if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), ret.getCampaign().getClient().getPrimaryKey()))
				throw new SecurityException("Current user is not allowed to access the specified connector.");
			
			return ret;
	}
	
	public Connector getConnector(String connectorUUID) {
		ConnectorDO conn = getSimpleConnector(connectorUUID);
		Connector ret = Connector.createConnectorBO(conn);
		ret.copyFrom(conn);
		return ret;
	}

	public Connector getSpecificConnectorVersion(String connectorUUID, Integer version) {
		String key = connectorUUID+"/"+version;
		Connector ret = (Connector)cacheAccessManager.getCachedObject(CacheName.ConnectorCache, key);
		if(ret!=null)
			return ret;
		
		ConnectorDO conn = getSimpleConnector(connectorUUID);
		ret = Connector.createConnectorBO(conn);
		ret.copyFrom(conn,version);
		
		if(version < conn.getCampaign().getCurrentVersion() && !cacheAccessManager.cacheContainsKey(CacheName.ConnectorCache, key))
			cacheAccessManager.cacheObject(CacheName.ConnectorCache, key, ret);
		return ret;
	}
	
	public CampaignDO getSimpleCampaign(String campaignUUID)
	{
		return getSimpleCampaign(campaignUUID, false);
	}
	
	public CampaignDO getSimpleCampaign(String campaignUUID, boolean eagerFetch)
	{
    Criteria crit = sf.getCurrentSession().createCriteria(CampaignDO.class);
    crit.add(Restrictions.eq("UID", campaignUUID));
    crit.setCacheable(true);
    crit.setCacheRegion("query/simpleCampaign");
    if(eagerFetch)
    {
      crit.setFetchMode("connectors", FetchMode.SELECT);
      crit.setFetchMode("nodes", FetchMode.SELECT);
    }
    CampaignDO ret = (CampaignDO)crit.uniqueResult();

    if(ret==null)
      return null;

    if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), ret.getClient().getPrimaryKey()))
      throw new SecurityException("Current user is not allowed to access the specified campaign.");

    return ret;
	}
	
	@Transactional(propagation= Propagation.REQUIRED)
	public Campaign getDetailedCampaign(String campaignUUID) {
		Campaign campaign = new Campaign();
		CampaignDO dataCPN = getSimpleCampaign(campaignUUID,true);
		campaign.copyFrom(dataCPN);
		
		Node tempNode;
		for(CampaignNodeLinkDO nodeLink : dataCPN.getNodes())
		{
			if(!nodeLink.getVersion().equals(campaign.getCurrentVersion()))
				continue;
			campaign.getNodes().add(getSpecificNodeVersion(nodeLink.getNode().getUID(),nodeLink.getVersion()));
		}
		
		Connector tempConn;
		for(CampaignConnectorLinkDO connLink : dataCPN.getConnectors())
		{
			if(!connLink.getVersion().equals(campaign.getCurrentVersion()))
				continue;
			campaign.getConnectors().add(getSpecificConnectorVersion(connLink.getConnector().getUID(),connLink.getVersion()));
		}
		
		return campaign;
	}

	public NodeDO getSimpleNode(String nodeUUID)
	{
    Criteria crit = sf.getCurrentSession().createCriteria(NodeDO.class);
    crit.add(Restrictions.eq("UID", nodeUUID));
    crit.setCacheable(true);
    crit.setCacheRegion("query/SimpleNode");
    NodeDO ret = (NodeDO)crit.uniqueResult();

    if(ret==null)
      return null;

    if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), ret.getCampaign().getClient().getPrimaryKey()))
      throw new SecurityException("Current user is not allowed to access the specified node.");

    return ret;
	}
	
	public Node getNode(String nodeUUID) {
		NodeDO node = getSimpleNode(nodeUUID);
		return getSpecificNodeVersion(nodeUUID,node.getCampaign().getCurrentVersion());
	}

	public Node getSpecificNodeVersion(String nodeUUID, Integer versionNumber)
	{
		String key = nodeUUID+"/"+versionNumber;
		Node ret = (Node)cacheAccessManager.getCachedObject(CacheName.NodeCache, key);
		if(ret!=null)
			return ret;
		NodeDO node = getSimpleNode(nodeUUID);
		ret = Node.createNodeBO(node);
		ret.copyFrom(node,versionNumber);
		if(ret.getType()==NodeType.Tagging) //We have to fill in contact tags separately.
		{
			TaggingNode tNode = (TaggingNode)ret;
			if (tNode.getTags() == null)
				tNode.setTags(new ArrayList<ContactTag>());
			tNode.getTags().clear();
			ContactTag ct;
			for(NodeInfoDO ni : node.getNodeInfo())
			{
				if(!ni.getVersion().equals(versionNumber))
					continue;
							
				if(ni.getName().startsWith(TaggingNode.INFO_PROPERTY_TAG+"["))
				{
					Matcher r = Pattern.compile(TaggingNode.INFO_PROPERTY_TAG+"\\[([\\d]+)\\]").matcher(ni.getName());
					r.matches();
					ct = new ContactTag();
					ct.copyFrom((ContactTagDO)sf.getCurrentSession().get(ContactTagDO.class, new Long(ni.getValue())));
					int pos = new Integer(r.group(1));
					while(tNode.getTags().size()<pos+1)
						tNode.getTags().add(null);
					tNode.getTags().set(pos, ct);
				}
			}
		}
		if(versionNumber<node.getCampaign().getCurrentVersion() && !cacheAccessManager.cacheContainsKey(CacheName.NodeCache,key))
			cacheAccessManager.cacheObject(CacheName.NodeCache, key, ret);
		return ret;
	}
	
	public Campaign getSpecificCampaignVersion(String campaignUUID, int version) {
		String key = campaignUUID+"/"+version;
		Campaign campaign = (Campaign)cacheAccessManager.getCachedObject(CacheName.CampaignCache, key);
		if(campaign!=null)
			return campaign;

		campaign = new Campaign();
		CampaignDO dataCPN = getSimpleCampaign(campaignUUID);
		campaign.copyFrom(dataCPN,version);
		
		Node tempNode;
		for(CampaignNodeLinkDO nodeLink : dataCPN.getNodes())
		{
			if(!nodeLink.getVersion().equals(version))
				continue;
			campaign.getNodes().add(getSpecificNodeVersion(nodeLink.getNode().getUID(),nodeLink.getVersion()));
		}
		
		Connector tempConn;
		for(CampaignConnectorLinkDO connLink : dataCPN.getConnectors())
		{
			if(!connLink.getVersion().equals(version))
				continue;
			campaign.getConnectors().add(getSpecificConnectorVersion(connLink.getConnector().getUID(),connLink.getVersion()));
		}
		
		if(version < dataCPN.getCurrentVersion() && !cacheAccessManager.cacheContainsKey(CacheName.CampaignCache,key))
			cacheAccessManager.cacheObject(CacheName.CampaignCache, key, campaign);
		
		campaign.setCurrentVersion(version);
		
		return campaign;
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.PublishCampaign)
	public void publish(String campaignUUID) {
		try
		{
			CampaignDO camp = getSimpleCampaign(campaignUUID);
			if(camp==null)
				throw new IllegalArgumentException("Campaign '"+campaignUUID+"' could not be found.");
			if(camp.getMode().equals(CampaignMode.Template))
				throw new IllegalArgumentException("Cannot publish a campaign template!");
			if(camp.getMode().equals(CampaignMode.Broadcast) && camp.getCurrentVersion()>1)
				throw new IllegalArgumentException("Cannot publish a broadcast campaign more than once!");
			
			Integer version = camp.getCurrentVersion();
			
			ConnectorDO conn;
			ConnectorInfoDO newCI;
			NodeDO node;
			NodeInfoDO newNI;
			CampaignNodeLinkDO newCNL;
			CampaignConnectorLinkDO newCCL;
			NodeConnectorLinkDO newNCL;
			
			Set<String> newVersionConnectors=new HashSet<String>();
			Set<String> previousVersionConnectors=new HashSet<String>();
			Set<String> newVersionNodes=new HashSet<String>();
			Set<String> previousVersionNodes=new HashSet<String>();
			
			Map<String,Map<String,ConnectorType>> nodeCatalog=new HashMap<String,Map<String,ConnectorType>>();
			
			while(camp.getVersions().size() < camp.getCurrentVersion())
			{
				CampaignVersionDO cVersion = new CampaignVersionDO();
				cVersion.setCampaign(camp);
				cVersion.setPublishedDate(new Date());
				cVersion.setStatus(CampaignVersionStatus.Published);
				camp.getVersions().add(cVersion);
			}

			camp.getVersions().get(camp.getCurrentVersion()-1).setStatus(CampaignVersionStatus.Published);
			
			CampaignVersionDO cVersion = new CampaignVersionDO();
			cVersion.setCampaign(camp);
			cVersion.setPublishedDate(new Date());
			cVersion.setStatus(CampaignVersionStatus.Working);
			camp.getVersions().add(cVersion);

			//Increment version for all connectors
			for(CampaignConnectorLinkDO ccl : camp.getConnectors())
			{
				//first, find the correct link table entry for the current version
				//increment the version and save it.
				if(!ccl.getVersion().equals(version))
				{
					if(ccl.getVersion().equals(version-1))
						previousVersionConnectors.add(ccl.getConnector().getUID());
					continue;
				}
				newVersionConnectors.add(ccl.getConnector().getUID());
				newCCL = new CampaignConnectorLinkDO();
				newCCL.setCampaign(ccl.getCampaign());
				newCCL.setConnector(ccl.getConnector());
				newCCL.setVersion(version+1);
				sf.getCurrentSession().persist(newCCL);
				
				conn = ccl.getConnector();
				for(ConnectorInfoDO ci : conn.getConnectorInfo())
				{
					if(!ci.getVersion().equals(version))
						continue;
					newCI = new ConnectorInfoDO();
					newCI.setConnector(ci.getConnector());
					newCI.setName(ci.getName());
					newCI.setValue(ci.getValue());
					newCI.setVersion(version+1);
					sf.getCurrentSession().persist(newCI);
				}
				
				for(NodeConnectorLinkDO ncl : ccl.getConnector().getConnections())
				{
					if(!ncl.getVersion().equals(version))
						continue;

					if(ncl.getConnectionPoint().equals(ConnectionPoint.Source))
					{
						if(!nodeCatalog.containsKey(ncl.getNode().getUID()))
							nodeCatalog.put(ncl.getNode().getUID(), new HashMap<String,ConnectorType>());
						nodeCatalog.get(ncl.getNode().getUID()).put(ncl.getConnector().getUID(), ncl.getConnector().getType());
					}
					
					newNCL = new NodeConnectorLinkDO();
					newNCL.setConnectionPoint(ncl.getConnectionPoint());
					newNCL.setConnector(ncl.getConnector());
					newNCL.setNode(ncl.getNode());
					newNCL.setVersion(version+1);
					sf.getCurrentSession().persist(newNCL);
				}
			}
			
			for(CampaignNodeLinkDO cnl : camp.getNodes())
			{
				if(!cnl.getVersion().equals(version))
				{
					if(cnl.getVersion().equals(version-1))
						previousVersionNodes.add(cnl.getNode().getUID());
					continue;
				}
				newVersionNodes.add(cnl.getNode().getUID());
				newCNL = new CampaignNodeLinkDO();
				newCNL.setCampaign(cnl.getCampaign());
				newCNL.setNode(cnl.getNode());
				newCNL.setVersion(version+1);

				//If this is a coupon node, we need to do a few things.
				if(cnl.getNode().getType().equals(NodeType.Coupon))
				{
					CouponNode cNode = new CouponNode();
					cNode.copyFrom(cnl.getNode());
					
					CouponOfferDO offer;
					if(cNode.getCouponId()==null)
						offer = new CouponOfferDO();
					else
						offer = (CouponOfferDO)sf.getCurrentSession().get(CouponOfferDO.class, cNode.getCouponId());
					offer.setMaxCoupons(cNode.getMaxCoupons());
					offer.setOfferUnavailableDate(cNode.getUnavailableDate());
					offer.setNodeUID(cNode.getUid());
					offer.setMaxRedemptions(cNode.getMaxRedemptions());
					offer.setCouponName(cNode.getName());
					offer.setCampaign(cnl.getCampaign());
					offer.setCouponExpirationDate(cNode.getExpireDate());
					offer.setCouponExpirationDays(cNode.getExpireDays());
					offer.setOfferCode(cNode.getOfferCode());
					sf.getCurrentSession().persist(offer);
					
					cNode.associateCouponOffer(offer);
					cNode.copyTo(cnl.getNode());
				}
				
				sf.getCurrentSession().persist(newCNL);
				
				node = cnl.getNode();
				for(NodeInfoDO ni : node.getNodeInfo())
				{
					if(!ni.getVersion().equals(version))
						continue;
					newNI = new NodeInfoDO();
					newNI.setNode(ni.getNode());
					newNI.setName(ni.getName());
					newNI.setValue(ni.getValue());
					newNI.setVersion(version+1);
					sf.getCurrentSession().persist(newNI);
				}
			}
			
			Query q = sf.getCurrentSession().getNamedQuery("layouts.by.campaign.version");
			q.setParameter("campaignPK", camp.getPrimaryKey());
			q.setParameter("version",version);
			LayoutInfoDO newLI;
			for(LayoutInfoDO li : (List<LayoutInfoDO>)q.list())
			{
				newLI=new LayoutInfoDO();
				newLI.setCampaign(li.getCampaign());
				newLI.setUID(li.getUID());
				newLI.setXLoc(li.getXLoc());
				newLI.setYLoc(li.getYLoc());
				newLI.setVersion(version+1);
				sf.getCurrentSession().persist(newLI);
			}

			Node tempNode;
			Connector tempConnector;

			for(Map.Entry<String,Map<String,ConnectorType>> nodeEntry : nodeCatalog.entrySet())
			{
				if(nodeEntry.getValue().size()>1 && nodeEntry.getValue().values().contains(ConnectorType.Immediate))
				{
					throw new IllegalStateException("Cannot have a node with an immediate connector as well as others attached.  Publish failed on node UID "+nodeEntry.getKey());
				}
			}
			
			Set<String> diffSet;
			diffSet = new HashSet<String>(previousVersionNodes);
			diffSet.removeAll(newVersionNodes);

			//All deleted nodes . . . 
			for(String nodeUID : diffSet)
			{
				tempNode=getSpecificNodeVersion(nodeUID,version-1);
				q=sf.getCurrentSession().getNamedQuery("subscription.count.for.node");
				q.setParameter("nodeUID", nodeUID);
				if(((Long)q.uniqueResult())>0)
					throw new IllegalStateException("Can't delete node "+nodeUID+" since it would strand a subscriber.");
			}

			diffSet = new HashSet<String>(previousVersionConnectors);
			diffSet.removeAll(newVersionConnectors);
			
			//All deleted connectors . . . 
			for(String connUID : diffSet)
			{
				tempConnector=getSpecificConnectorVersion(connUID,version-1);
				switch(tempConnector.getType())
				{
					case Calendar:
					case Interval:
						q=sf.getCurrentSession().getNamedQuery("clear.tasks.for.connector");
						q.setParameter("sourceUID", connUID);
						q.executeUpdate();				}
			}
			
//			diffSet = new HashSet<String>(newVersionConnectors);
//			diffSet.removeAll(previousVersionConnectors);
			
			//All connectors . . . 
			for(String connUID : newVersionConnectors)
			{
				tempConnector=getSpecificConnectorVersion(connUID,version);
				switch(tempConnector.getType())
				{
					case Calendar:
						timer.setTimer(connUID, null, version, CATEventType.ConnectorFired, ((CalendarConnector)tempConnector).getTargetDate());
					break;
					
					case Immediate:
						eventManager.queueEvent(CATEvent.buildFireConnectorForAllSubscribersEvent(connUID,version));
					break;
					
					case Interval:
						if(previousVersionConnectors.contains(connUID))
							continue; //Don't do anything if this was in a previous version . . . changes will get caught on a case by case basis.
						q = sf.getCurrentSession().getNamedQuery("all.subscribers.on.node");
						q.setParameter("nodeUID", tempConnector.getSourceNodeUID());
						List<SubscriberDO> subs = q.list();
						Calendar c = Calendar.getInstance();
						IntervalConnector iConn = (IntervalConnector)tempConnector;
						switch(iConn.getIntervalType())
						{
							case Minutes:
								c.add(Calendar.MINUTE, iConn.getInterval().intValue());
								break;
							case Hours:
								c.add(Calendar.HOUR, iConn.getInterval().intValue());
								break;
							case Days:
								c.add(Calendar.DATE, iConn.getInterval().intValue());
								break;
							case Weeks:
								c.add(Calendar.WEEK_OF_YEAR, iConn.getInterval().intValue());
								break;
							case Months:
								c.add(Calendar.MONTH, iConn.getInterval().intValue());
								break;
						}
						for(SubscriberDO sub : subs)
						{
							timer.setTimer(iConn.getUid(), sub.getPrimaryKey().toString(), version, CATEventType.ConnectorFired, c.getTime());
						}
					break;
				}
			}
			
			List<CampaignEntryPointDO> cepToDelete = new ArrayList<CampaignEntryPointDO>();
			
			for(CampaignEntryPointDO cep : camp.getEntryPoints())
			{
				if(cep.getQuantity().intValue()<=0)
				{
					cep.setPublished(false);
					cepToDelete.add(cep);
					sf.getCurrentSession().delete(cep);
				}else{
					cep.setPublished(true);
				}
			}
			
			camp.getConnectors().removeAll(cepToDelete);

			//Potentially could UPDATE connectors, too.
			
			camp.setCurrentVersion(version+1);
		}
		catch(Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.SaveCampaign)
	public Campaign save(Campaign campaign) {
		CampaignDO camp = getSimpleCampaign(campaign.getUid());
		if(camp==null && campaign.getClientPK()==null)
			throw new IllegalArgumentException("Cannot create a new campaign without a valid client PK.");
		if(camp!=null && !camp.getClient().getPrimaryKey().equals(campaign.getClientPK()))
			throw new IllegalArgumentException("Cannot change the client ID associated with the campaign.");
		if(camp==null)
		{
			if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), campaign.getClientPK()))
				throw new SecurityException("Current user is not allowed to create campaigns for the specified client.");	
			
			camp = new CampaignDO();
			camp.setClient((ClientDO)sf.getCurrentSession().get(ClientDO.class, campaign.getClientPK()));
			camp.setUID(campaign.getUid());
			camp.setMode(campaign.getMode());
			if(camp.getUID()==null || camp.getUID().trim().length()==0)
				camp.setUID(UUID.randomUUID().toString());
		}
		campaign.copyTo(camp);
		
		// Save campaign
		sf.getCurrentSession().persist(camp);
		
		// Update list of addin messages
		if (campaign.getAddInMessages() != null)
		{
			for (AddInMessage add : campaign.getAddInMessages())
			{
					
				// Get existing or new data object
				AddInMessageDO addDO = null;
				if (add.getAddInMessageId() != null)
					addDO = (AddInMessageDO)sf.getCurrentSession().get(AddInMessageDO.class, add.getAddInMessageId());
				if (addDO == null)
					addDO = new AddInMessageDO();

				// Campaigns should only have CLIENT addin messages
				if ( addDO.getAddInMessageId() == null &&
					add.getType() != AddInMessageType.CLIENT)
					throw new IllegalArgumentException("Campaings should not have this type of addin message assigned");
				
				// Update and persist object
				addDO.setCampaign(camp);
				add.copyTo(addDO);
				sf.getCurrentSession().persist(addDO);
				
				// Add message to client list
				if (camp.getAddInMessages() == null)
					camp.setAddInMessages(new HashSet<AddInMessageDO>());
				camp.getAddInMessages().add(addDO);
			}
		}
		
		// Update list of campaign info
		if (campaign.getCampaignInfos() != null)
		{
			for (CampaignInfo ci : campaign.getCampaignInfos())
			{
					
				// Get existing or new data object
				CampaignInfoDO ciDO = null;
				if (ci.getCampaignInfoId() != null)
					ciDO = (CampaignInfoDO)sf.getCurrentSession().get(CampaignInfoDO.class, ci.getCampaignInfoId());
				else if (ciDO == null)
				{
					Query q = sf.getCurrentSession().getNamedQuery("autostart.info.by.unique.key");
					q.setParameter("campaignId", camp.getPrimaryKey());
					q.setParameter("entryType", ci.getEntryType());
          ciDO = (CampaignInfoDO)q.uniqueResult();
          if(ciDO==null)
					{
            ciDO = new CampaignInfoDO();
          }
				}
					

				
				// Update and persist object
				ciDO.setCampaign(camp);
				ci.copyTo(ciDO);
				if(ci.getValue()!=null)
				{
					EntryNode eNode = (EntryNode)getNode(ciDO.getValue());
					for(EntryData entryData : eNode.getEntryData())
					{
						if(entryData.getEntryType()==EntryPointType.Twitter || entryData.getEntryType()==EntryPointType.Facebook)
							ciDO.setEntryAddress(entryData.getEntryPoint());
					}
					sf.getCurrentSession().persist(ciDO);
					// Add info to list
					if (camp.getCampaignInfos() == null)
						camp.setCampaignInfos(new HashSet<CampaignInfoDO>());
					camp.getCampaignInfos().add(ciDO);
				} else {
					camp.getCampaignInfos().remove(ciDO);
					sf.getCurrentSession().delete(ciDO);
				}
			}
		}
		
		sf.getCurrentSession().flush();
		
		// Return updated campaign
		Campaign ret = new Campaign();
		ret.copyFrom(camp);
		return ret;
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
  @AuditEvent(AuditType.CreateCampaignFromTemplate)
	public Campaign createFromTemplate(Campaign campaign, String campaignTemplateUUID)
	{
		Campaign template = getDetailedCampaign(campaignTemplateUUID);
		if(template==null)
			throw new IllegalArgumentException("Template '"+campaignTemplateUUID+"' could not be found.");
		if(template.getMode().equals(CampaignMode.Normal))
			throw new IllegalArgumentException("Specified UUID is not a template!");
		
		Map<String,String> oldNewUIDMap = new HashMap<String,String>();
		
    if (campaign.getConnectors() != null)
    {
    	campaign.getConnectors().clear();
    }
    
    if (campaign.getNodes() != null)
    {
    	campaign.getNodes().clear();
    }
    
    if (campaign.getCampaignInfos() != null)
    {
    	campaign.getCampaignInfos().clear();
    }
    
    if (campaign.getAddInMessages() != null)
    {
    	campaign.getAddInMessages().clear();
    }
    
		campaign = save(campaign);
		Node newNode;
		for(Node oldNode : template.getNodes())
		{
			newNode = copyNode(oldNode);
			newNode.setCampaignUID(campaign.getUid());
			save(newNode);
			oldNewUIDMap.put(oldNode.getUid(), newNode.getUid());
		}
		
		Connector newConnector;
		for(Connector oldConnector : template.getConnectors())
		{
			newConnector = copyConnector(oldConnector);
			newConnector.setCampaignUID(campaign.getUid());
			newConnector.setDestinationUID(oldNewUIDMap.get(oldConnector.getDestinationUID()));
			newConnector.setSourceNodeUID(oldNewUIDMap.get(oldConnector.getSourceNodeUID()));
			save(newConnector);
			oldNewUIDMap.put(oldConnector.getUid(), newConnector.getUid());
		}
		
		LayoutInfo newLO;
		for(LayoutInfo info : layoutManager.getLayoutsByCampaign(campaignTemplateUUID))
		{
			newLO = new LayoutInfo();
			newLO.setCampaignUUID(campaign.getUid());
			newLO.setUUID(oldNewUIDMap.get(info.getUUID()));
			newLO.setX(info.getX());
			newLO.setY(info.getY());
			newLO.setVersion(1);
			layoutManager.save(newLO);
		}
		return campaign;
	}

	private Connector copyConnector(Connector from)
	{
		switch(from.getType())
		{
			case Calendar:
			{
				CalendarConnector ret = new CalendarConnector();
				CalendarConnector source = (CalendarConnector)from;
				ret.setName(source.getName());
				ret.setTargetDate(source.getTargetDate());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			
			case Immediate:
			{
				ImmediateConnector ret = new ImmediateConnector();
				ImmediateConnector source = (ImmediateConnector)from;
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			
			case Interval:
			{
				IntervalConnector ret = new IntervalConnector();
				IntervalConnector source = (IntervalConnector)from;
				ret.setInterval(source.getInterval());
				ret.setIntervalType(source.getIntervalType());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			
			case Response:
			{
				ResponseConnector ret = new ResponseConnector();
				ResponseConnector source = (ResponseConnector)from;
				ret.setEntryData(source.getEntryData());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			
			default:
				throw new IllegalStateException("Cannot copy campaign, since one of the connector types cannot be copied.");
		}
	}
	
	private Node copyNode(Node from)
	{
		switch(from.getType())
		{
			case Coupon:
			{
				CouponNode ret = new CouponNode();
				CouponNode source = (CouponNode)from;
				ret.setAvailableMessage(source.getAvailableMessage());
				ret.setCouponCode(source.getCouponCode());
				ret.setMaxCoupons(source.getMaxCoupons());
				ret.setMaxRedemptions(source.getMaxRedemptions());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				ret.setUnavailableDate(source.getUnavailableDate());
				ret.setUnavailableMessage(source.getUnavailableMessage());
				return ret;
			}
			
			case OutgoingEntry:
			{
				OutgoingEntryNode ret = new OutgoingEntryNode();
				EntryNode source = (OutgoingEntryNode)from;
				ret.setEntryData(source.getEntryData());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}

			case Entry:
			{
				EntryNode ret = new EntryNode();
				EntryNode source = (EntryNode)from;
				ret.setEntryData(source.getEntryData());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			case Message:
			{
				MessageNode ret = new MessageNode();
				MessageNode source = new MessageNode();
				ret.setMessage(source.getMessage());
				ret.setMessageType(source.getMessageType());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			case Tagging:
			{
				TaggingNode ret = new TaggingNode();
				TaggingNode source = new TaggingNode();
				ret.setTags(source.getTags());
				ret.setName(source.getName());
				ret.setUid(UUID.randomUUID().toString());
				return ret;
			}
			
			default:
				throw new IllegalStateException("Cannot copy campaign, since one of the node types cannot be copied.");
		}
	}
	
	protected CampaignEntryPointDO getSpecificEntryPoint(String entryPoint, EntryPointType type, String keyword)
	{
		Query q = sf.getCurrentSession().getNamedQuery("find.matching.entry.point");
		q.setParameter("entryPoint", entryPoint);
		q.setParameter("keyword", keyword);
		q.setParameter("type", type);
    return (CampaignEntryPointDO)q.uniqueResult();
	}
	
	protected boolean isEntryPointValid(String campaignUID, String entryPoint, EntryPointType type, String keyword)
	{
		CampaignEntryPointDO cep=getSpecificEntryPoint(entryPoint,type,keyword);
		if(cep==null)
			return true;
		if(cep.getCampaign().getUID().equals(campaignUID))
			return true;
		return false;
	}
	
	protected boolean isEntryPointDefinable(String entryPoint, String keyword, EntryPointType type)
	{
		if(entryPoint==null || 
			entryPoint.trim().length()==0 || 
			keyword==null || 
			keyword.trim().length()==0 ||
			type==null)
			return false;
		return true;
	}
	
  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.SaveNode)
	public Node save(Node node) {
		CampaignDO camp = getSimpleCampaign(node.getCampaignUID());
		if(node.getUid()==null)
			node.setUid(UUID.randomUUID().toString());
		NodeDO n = getSimpleNode(node.getUid());
		if(n==null)
		{
			if(camp==null)
				throw new IllegalArgumentException("Cannot create a node without a valid campaign.");

			n = new NodeDO();
			CampaignNodeLinkDO cnl = new CampaignNodeLinkDO();
			cnl.setCampaign(camp);
			cnl.setNode(n);
			cnl.setVersion(camp.getCurrentVersion());
			n.getVersionedNodes().put(camp.getCurrentVersion(), cnl);
			n.setCampaign(camp);
			n.setUID(node.getUid());
			if(n.getUID()==null || n.getUID().trim().length()==0)
				n.setUID(UUID.randomUUID().toString());
						
			node.copyTo(n);
			
			sf.getCurrentSession().persist(n);
			sf.getCurrentSession().persist(cnl);
			
			node.copyFrom(n);

		} else {
			if(camp!=null && !camp.getUID().equals(node.getCampaignUID()))
				throw new IllegalArgumentException("Cannot change the campaign a node is associated with.");

			if(node.getType().equals(NodeType.Entry))
				recordEntryPointChanges(node,camp);

			node.copyTo(n);
			sf.getCurrentSession().persist(n);
			node.copyFrom(n);
		}
		return node;
	}

	private void recordEntryPointChanges(Node node, CampaignDO camp)
	{
		EntryNode eNode = (EntryNode)node;
		EntryNode oldNode = (EntryNode)getNode(eNode.getUid());
		
		MultiValueMap<EntryPointType,String,String> oldEntries = new MultiValueMap<EntryPointType,String,String>();
		MultiValueMap<EntryPointType,String,String> newEntries = new MultiValueMap<EntryPointType,String,String>();

		for(int loop=0; loop<eNode.getEntryData().size(); loop++)
		{
			if(eNode.getEntryData().get(loop).getEntryPoint()==null ||
			   eNode.getEntryData().get(loop).getEntryType()==null ||
			   eNode.getEntryData().get(loop).getKeyword()==null)
				continue;
			newEntries.put(eNode.getEntryData().get(loop).getEntryType(), eNode.getEntryData().get(loop).getEntryPoint(), eNode.getEntryData().get(loop).getKeyword());
		}

		for(int loop=0; loop<oldNode.getEntryData().size(); loop++)
		{
			if(oldNode.getEntryData().get(loop).getEntryPoint()==null ||
				oldNode.getEntryData().get(loop).getEntryType()==null ||
				oldNode.getEntryData().get(loop).getKeyword()==null)
				continue;
			oldEntries.put(oldNode.getEntryData().get(loop).getEntryType(), oldNode.getEntryData().get(loop).getEntryPoint(), oldNode.getEntryData().get(loop).getKeyword());
		}

		for(EntryPointType type : newEntries.keySet())
		{
			if(type==null)
				continue;
			if(oldNode!=null &&
			   ((newEntries.getValue1(type)==null && oldEntries.getValue1(type)==null)
			   || newEntries.getValue1(type).equals(oldEntries.getValue1(type))) &&
			   ((newEntries.getValue2(type)==null && oldEntries.getValue2(type)==null)
			   || newEntries.getValue2(type).equals(oldEntries.getValue2(type))))
			{
				oldEntries.remove(type);
				continue;
			}
			
			if(!isEntryPointValid(camp.getUID(),newEntries.getValue1(type),type,newEntries.getValue2(type)))
				throw new IllegalArgumentException("Entry point is already in use by another campaign.");
	
			CampaignEntryPointDO cep = getSpecificEntryPoint(oldEntries.getValue1(type),type,oldEntries.getValue2(type));
			if(cep==null)
			{
				log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
			} else {
				if(cep.getCampaign()!=camp)
					throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
				cep.setQuantity(cep.getQuantity()-1);
				if(cep.getQuantity()==0 && !cep.isPublished())
					sf.getCurrentSession().delete(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
			}
	
			cep = getSpecificEntryPoint(newEntries.getValue1(type),type,newEntries.getValue2(type));
			if(cep==null && isEntryPointDefinable(newEntries.getValue1(type),newEntries.getValue2(type),type))
			{
				cep = new CampaignEntryPointDO();
				cep.setCampaign(camp);
				cep.setEntryPoint(newEntries.getValue1(type));
				cep.setType(type);
				cep.setKeyword(newEntries.getValue2(type));
			}
			if(cep!=null)
			{
				cep.setQuantity(cep.getQuantity()+1);
				sf.getCurrentSession().persist(cep);
			}
			oldEntries.remove(type);
		}		
		
		for(EntryPointType type : oldEntries.keySet())
		{
			CampaignEntryPointDO cep = getSpecificEntryPoint(oldEntries.getValue1(type),type,oldEntries.getValue2(type));
			if(cep==null)
			{
				log.warn("entry point removed without an appropriate CampaignEntryPointDO entry already existing.");
			} else {
				if(cep.getCampaign()!=camp)
					throw new IllegalStateException("Trying to remove the entry point belonging to a different campaign.");
				cep.setQuantity(cep.getQuantity()-1);
				if(cep.getQuantity()==0 && !cep.isPublished())
					sf.getCurrentSession().delete(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
			}
		}
	}
	
	private void recordEntryPointChanges(Connector conn, CampaignDO camp)
	{
		ResponseConnector rConn = (ResponseConnector)conn;
		ResponseConnector oldConn = (ResponseConnector)getConnector(rConn.getUid());
		
		MultiValueMap<EntryPointType,String,String> oldEntries = new MultiValueMap<EntryPointType,String,String>();
		MultiValueMap<EntryPointType,String,String> newEntries = new MultiValueMap<EntryPointType,String,String>();

		for(int loop=0; loop<rConn.getEntryData().size(); loop++)
		{
			if(rConn.getEntryData().get(loop).getEntryPoint()==null ||
					rConn.getEntryData().get(loop).getEntryType()==null ||
					rConn.getEntryData().get(loop).getKeyword()==null)
				continue;
			newEntries.put(rConn.getEntryData().get(loop).getEntryType(), rConn.getEntryData().get(loop).getEntryPoint(), rConn.getEntryData().get(loop).getKeyword());
		}

		for(int loop=0; loop<oldConn.getEntryData().size(); loop++)
		{
			if(oldConn.getEntryData().get(loop).getEntryPoint()==null ||
					oldConn.getEntryData().get(loop).getEntryType()==null ||
					oldConn.getEntryData().get(loop).getKeyword()==null)
				continue;
			oldEntries.put(oldConn.getEntryData().get(loop).getEntryType(), oldConn.getEntryData().get(loop).getEntryPoint(), oldConn.getEntryData().get(loop).getKeyword());
		}

		for(EntryPointType type : newEntries.keySet())
		{
			if(type==null)
				continue;
			if(oldConn!=null &&
			   ((newEntries.getValue1(type)==null && oldEntries.getValue1(type)==null)
			   || newEntries.getValue1(type).equals(oldEntries.getValue1(type))) &&
			   ((newEntries.getValue2(type)==null && oldEntries.getValue2(type)==null)
			   || newEntries.getValue2(type).equals(oldEntries.getValue2(type))))
			{
				oldEntries.remove(type);
				continue;
			}
			
			if(!isEntryPointValid(camp.getUID(),newEntries.getValue1(type),type,newEntries.getValue2(type)))
				throw new IllegalArgumentException("Entry point is already in use by another campaign.");
	
			CampaignEntryPointDO cep = getSpecificEntryPoint(oldEntries.getValue1(type),type,oldEntries.getValue2(type));
			if(cep==null)
			{
				log.warn("response connector modified without an appropriate CampaignEntryPointDO entry already existing.");
			} else {
				if(cep.getCampaign()!=camp)
					throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
				cep.setQuantity(cep.getQuantity()-1);
				if(cep.getQuantity()==0 && !cep.isPublished())
					sf.getCurrentSession().delete(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
			}
	
			cep = getSpecificEntryPoint(newEntries.getValue1(type),type,newEntries.getValue2(type));
			if(cep==null && isEntryPointDefinable(newEntries.getValue1(type),newEntries.getValue2(type),type))
			{
				cep = new CampaignEntryPointDO();
				cep.setCampaign(camp);
				cep.setEntryPoint(newEntries.getValue1(type));
				cep.setType(type);
				cep.setKeyword(newEntries.getValue2(type));
			}
			if(cep!=null)
			{
				cep.setQuantity(cep.getQuantity()+1);
				sf.getCurrentSession().persist(cep);
			}
			oldEntries.remove(type);
		}		
		
		for(EntryPointType type : oldEntries.keySet())
		{
			CampaignEntryPointDO cep = getSpecificEntryPoint(oldEntries.getValue1(type),type,oldEntries.getValue2(type));
			if(cep==null)
			{
				log.warn("entry point removed without an appropriate CampaignEntryPointDO entry already existing.");
			} else {
				if(cep.getCampaign()!=camp)
					throw new IllegalStateException("Trying to remove the entry point belonging to a different campaign.");
				cep.setQuantity(cep.getQuantity()-1);
				if(cep.getQuantity()==0 && !cep.isPublished())
					sf.getCurrentSession().delete(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
			}
		}
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.SaveConnection)
	public Connector save(Connector connector) {
		CampaignDO camp = getSimpleCampaign(connector.getCampaignUID());
		if(connector.getUid()==null)
			connector.setUid(UUID.randomUUID().toString());
		ConnectorDO c = getSimpleConnector(connector.getUid());
		if(c==null)
		{
			if(camp==null)
				throw new IllegalArgumentException("Cannot create a connector without a valid campaign.");

			c = new ConnectorDO();
			CampaignConnectorLinkDO ccl = new CampaignConnectorLinkDO();
			ccl.setCampaign(camp);
			ccl.setConnector(c);
			ccl.setVersion(camp.getCurrentVersion());
			c.getVersionedConnectors().put(camp.getCurrentVersion(), ccl);
			c.setUID(connector.getUid());
			if(c.getUID()==null || c.getUID().trim().length()==0)
				c.setUID(UUID.randomUUID().toString());
			
			
			c.setCampaign(camp);
			connector.copyTo(c);
			
			NodeConnectorLinkDO source=null;
			NodeConnectorLinkDO dest=null;
			
			sf.getCurrentSession().persist(c);
			sf.getCurrentSession().persist(ccl);

			if(connector.getSourceNodeUID()!=null)
			{
				source = new NodeConnectorLinkDO();
				source.setVersion(camp.getCurrentVersion());
				source.setNode(getSimpleNode(connector.getSourceNodeUID()));
				source.setConnectionPoint(ConnectionPoint.Source);
				if(source.getNode()==null)
					throw new IllegalArgumentException("Source NodeDO could not be found.");
				source.setConnector(c);
				sf.getCurrentSession().persist(source);
			}
			
			if(connector.getDestinationUID()!=null)
			{
				dest = new NodeConnectorLinkDO();
				dest.setVersion(camp.getCurrentVersion());
				dest.setNode(getSimpleNode(connector.getDestinationUID()));
				dest.setConnectionPoint(ConnectionPoint.Destination);
				if(dest.getNode()==null)
					throw new IllegalArgumentException("Destination NodeDO could not be found.");
				dest.setConnector(c);
				sf.getCurrentSession().persist(dest);
			}
			
			connector.copyFrom(c);
			
		} else {
			if(camp!=null && !camp.getUID().equals(connector.getCampaignUID()))
				throw new IllegalArgumentException("Cannot change the campaign a node is associated with.");

			NodeConnectorLinkDO source=null;
			NodeConnectorLinkDO dest=null;

			if(connector.getType().equals(ConnectorType.Response))
				recordEntryPointChanges(connector,camp);

			for(NodeConnectorLinkDO ncl : c.getConnections())
			{
				if(!ncl.getVersion().equals(camp.getCurrentVersion()))
					continue;
				if(ncl.getConnectionPoint().equals(ConnectionPoint.Source))
					source = ncl;
				else
					dest = ncl;
			}
			
			if(connector.getSourceNodeUID()!=null)
			{
				if (source == null)
					source = new NodeConnectorLinkDO();
				source.setNode(getSimpleNode(connector.getSourceNodeUID()));
				if(source.getNode()==null)
					throw new IllegalArgumentException("Source NodeDO could not be found.");
				source.setConnector(c);
				source.setVersion(camp.getCurrentVersion());
				source.setConnectionPoint(ConnectionPoint.Source);
				sf.getCurrentSession().persist(source);
			}
			
			if(source!=null && connector.getSourceNodeUID()==null)
			{
				sf.getCurrentSession().delete(source);
			}
			
			if(connector.getDestinationUID()!=null)
			{
				if (dest == null)
					dest = new NodeConnectorLinkDO();
				dest.setNode(getSimpleNode(connector.getDestinationUID()));
				if(dest.getNode()==null)
					throw new IllegalArgumentException("Destination NodeDO could not be found.");
				dest.setConnector(c);
				dest.setVersion(camp.getCurrentVersion());
				dest.setConnectionPoint(ConnectionPoint.Destination);
				sf.getCurrentSession().persist(dest);
			}
			
			if(dest!=null && connector.getDestinationUID()==null)
			{
				sf.getCurrentSession().delete(dest);
			}
			
			connector.copyTo(c);
			sf.getCurrentSession().persist(c);
			connector.copyFrom(c);
		}
		
		return connector;
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.DeleteCampaign)
	public void delete(Campaign campaign) {
		if(campaign==null || campaign.getUid()==null)
			throw new IllegalArgumentException("Cannot delete a non-existant campaign.");
		
		CampaignDO camp = getSimpleCampaign(campaign.getUid());
		if(camp==null)
			return;
		
		Query q = sf.getCurrentSession().getNamedQuery("delete.campaign.entry.points");
		q.setParameter("campaignID", camp.getPrimaryKey());
		q.executeUpdate();
		
		camp.setStatus(CampaignStatus.Deleted);
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.DeleteNode)
	public void delete(Node node) {
		if(node==null || node.getUid()==null)
			throw new IllegalArgumentException("Cannot delete a non-existant node.");
		
		if(node.getType().equals(NodeType.Entry))
		{
			EntryNode eNode = (EntryNode)node;
			CampaignEntryPointDO cep;
			for(EntryData data : eNode.getEntryData())
			{
				cep = getSpecificEntryPoint(data.getEntryPoint(),data.getEntryType(),data.getKeyword());
				if(cep==null)
				{
					log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
				} else {
					if(!cep.getCampaign().getUID().equals(node.getCampaignUID()))
						throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
					cep.setQuantity(cep.getQuantity()-1);
					if(cep.getQuantity()==0 && !cep.isPublished())
						sf.getCurrentSession().delete(cep);
					else if(cep.getQuantity()<0)
						log.warn("more entry points removed than have been initially catalogued");
				}
			}
		}
		
		NodeDO n = getSimpleNode(node.getUid());
		if(n==null)
			return;
		
		Integer currentVersion = n.getCampaign().getCurrentVersion();
		
		if(n.getConnections().size()>0)
		{
			for(NodeConnectorLinkDO link : n.getConnections())
			{
				if(link.getVersion().equals(currentVersion))
					sf.getCurrentSession().delete(link);
			}
		}
		
		Set<NodeInfoDO> removalNI = new HashSet<NodeInfoDO>();
		if(n.getNodeInfo().size()>0)
		{
			for(NodeInfoDO ni : n.getNodeInfo())
			{
				if(ni.getVersion().equals(currentVersion))
				{
					removalNI.add(ni);
					sf.getCurrentSession().delete(ni);
				}
			}
		}
		n.getNodeInfo().removeAll(removalNI);
		
		sf.getCurrentSession().delete(n.getVersionedNodes().get(currentVersion));
		n.getVersionedNodes().remove(currentVersion);
		if(n.getVersionedNodes().size()==0)
			sf.getCurrentSession().delete(n);
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	@AuditEvent(AuditType.DeleteConnection)
	public void delete(Connector connector) {
		if(connector==null || connector.getUid()==null)
			throw new IllegalArgumentException("Cannot delete a non-existant connector.");
		
		if(connector.getType().equals(ConnectorType.Response))
		{
			ResponseConnector rConn = (ResponseConnector)connector;
			CampaignEntryPointDO cep;
			for(EntryData data : rConn.getEntryData())
			{
				cep= getSpecificEntryPoint(data.getEntryPoint(),data.getEntryType(),data.getKeyword());
				if(cep==null)
				{
					log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
				} else {
					if(!cep.getCampaign().getUID().equals(connector.getCampaignUID()))
						throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
					cep.setQuantity(cep.getQuantity()-1);
					if(cep.getQuantity()==0 && !cep.isPublished())
						sf.getCurrentSession().delete(cep);
					else if(cep.getQuantity()<0)
						log.warn("more entry points removed than have been initially catalogued");
				}
			}
		}

		ConnectorDO c = getSimpleConnector(connector.getUid());
		if(c==null)
			return;
		
		Integer currentVersion = c.getCampaign().getCurrentVersion();
		
		if(c.getConnections().size()>0)
		{
			for(NodeConnectorLinkDO link : c.getConnections())
			{
				if(link.getVersion().equals(currentVersion))
					sf.getCurrentSession().delete(link);
			}
		}
		
		Set<ConnectorInfoDO> removalCI=new HashSet<ConnectorInfoDO>();
		if(c.getConnectorInfo().size()>0)
		{
			for(ConnectorInfoDO ci : c.getConnectorInfo())
			{
				if(ci.getVersion().equals(currentVersion))
				{
					removalCI.add(ci);
					sf.getCurrentSession().delete(ci);
				}
			}
		}
		c.getConnectorInfo().removeAll(removalCI);
		
		sf.getCurrentSession().delete(c.getVersionedConnectors().get(currentVersion));
		c.getVersionedConnectors().remove(currentVersion);
		if(c.getVersionedConnectors().size()==0)
			sf.getCurrentSession().delete(c);
	}

	public Campaign getLastPublishedCampaign(String campaignUUID) {
		CampaignDO camp = getSimpleCampaign(campaignUUID);
		if(camp==null)
			return null;
		if(camp.getCurrentVersion()==1)
			return null;
		Campaign ret = getSpecificCampaignVersion(campaignUUID,camp.getCurrentVersion()-1);
		ret.setCurrentVersion(camp.getCurrentVersion()-1);
		return ret;
	}

	public Map<String, Long> getNodeSubscriberCount(String campaignUUID) {
		getSimpleCampaign(campaignUUID); // Do nothing with this except invoke security checks.
		
		String queryString="select csl.lastHitNode.UID, count(csl.lastHitNode.UID) from CampaignSubscriberLinkDO csl where csl.campaign.UID=:campaignUID and csl.active=1 group by csl.lastHitNode.UID";
		Query q = sf.getCurrentSession().createQuery(queryString);
		q.setParameter("campaignUID", campaignUUID);
		List<Object[]> result = q.list();
		HashMap<String,Long> ret = new HashMap<String,Long>();
		for(Object[] row : result)
			ret.put((String)row[0], (Long)row[1]);
		return ret;
	}

	public void deleteCampaign(String uid) {
		delete(getDetailedCampaign(uid));
	}

  public void deleteConnector(String uid) {
		delete(getConnector(uid));
	}

  public void deleteNode(String uid) {
		delete(getNode(uid));
	}
	
	public CampaignDO getCampaignForNode(String uid) {
		String query = "select c from NodeDO n join n.campaign c where n.UID=:uid";
		return (CampaignDO)sf.getCurrentSession().createQuery(query).setParameter("uid", uid).uniqueResult();
	}

	public CampaignDO getCampaignForConnector(String uid) {
		String query = "select c from ConnectorDO con join con.campaign c where con.UID=:uid";
		return (CampaignDO)sf.getCurrentSession().createQuery(query).setParameter("uid", uid).uniqueResult();
	}

	public Integer getCurrentCampaignVersionForNode(String uid) {
		String query = "select c.currentVersion from NodeDO n join n.campaign c where n.UID=:uid";
		return (Integer)sf.getCurrentSession().createQuery(query).setParameter("uid", uid).uniqueResult();
	}

	public Integer getCurrentCampaignVersionForConnector(String uid) {
		String query = "select c.currentVersion from ConnectorDO con join con.campaign c where con.UID=:uid";
		return (Integer)sf.getCurrentSession().createQuery(query).setParameter("uid", uid).uniqueResult();
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	public void broadcastMessage(Long clientPK, List<EntryData> entryPoints, MessageNode message, List<Contact> contacts) {
		if(message==null || message.getCampaignUID()==null)
			throw new IllegalArgumentException("Cannot publish a broadcast message without a valid message, and campaign UID");
		CampaignDO campaignCheck = getSimpleCampaign(message.getCampaignUID(),false);
		if(campaignCheck!=null)
			throw new IllegalArgumentException("Cannot broadcast a message using a campaign ID that already exists.");
		
		String name = "Broadcast ";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		name += dateFormat.format(new Date());
		
		Campaign campaign = new Campaign();
		campaign.setName(name);
		campaign.setUid(message.getCampaignUID());
		campaign.setMode(CampaignMode.Broadcast);
		campaign.setClientPK(clientPK);
		save(campaign);
		
		message = (MessageNode)save(message);
		
		OutgoingEntryNode entry = new OutgoingEntryNode();
		entry.setCampaignUID(campaign.getUid());
		entry.setEntryData(entryPoints);
		
		entry = (OutgoingEntryNode)save(entry);
		
		ImmediateConnector connector = new ImmediateConnector();
		connector.setCampaignUID(campaign.getUid());
		connector.setSourceNodeUID(entry.getUid());
		connector.setDestinationUID(message.getUid());

    sf.getCurrentSession().flush();
    sf.getCurrentSession().clear();
    
		connector = (ImmediateConnector)save(connector);

		sf.getCurrentSession().flush();
		sf.getCurrentSession().clear();

		publish(campaign.getUid());
		
		sf.getCurrentSession().flush();
		sf.getCurrentSession().clear();
		
		subscriptionManager.subscribeContactsToEntryPoint(contacts, entry.getUid());
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	public void broadcastMessageSearch(Long clientPK, List<EntryData> entryPoints, MessageNode message, ContactSearchCriteria search) {
		List<Contact> contacts=new ArrayList<Contact>();
		for(EntryData entry : entryPoints)
		{
			if(entry.getMaxMessages()!=null && entry.getMaxMessages().intValue()>0)
			{
				Criteria crit = sf.getCurrentSession().createCriteria(ContactDO.class,"c");
				crit.add(Restrictions.eq("c.client.id",clientPK));
				crit.add(Restrictions.eq("c.type",entry.getEntryType()));
				crit.addOrder(new Order("rand",false){
					
					@Override
					public String toSqlString(Criteria arg0, CriteriaQuery arg1)
							throws HibernateException {
						return "rand()";
					}

					@Override
					public String toString() {
						return "rand()";
					}
				});
				crit.setMaxResults(entry.getMaxMessages());
				for(ContactDO contact : (List<ContactDO>)crit.list())
				{
					Contact c = new Contact();
					c.copyFrom(contact);
					contacts.add(c);
				}
			}
		}
		if(contacts.size()==0)
			contacts = contactManager.getContacts(search, null).getResults();
		broadcastMessage(clientPK, entryPoints, message, contacts);
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	public void broadcastCoupon(Long clientPK, List<EntryData> entryPoints, CouponNode coupon, List<Contact> contacts) {
		if(coupon==null || coupon.getCampaignUID()==null)
			throw new IllegalArgumentException("Cannot publish a broadcast coupon without a valid message, and campaign UID");
		CampaignDO campaignCheck = getSimpleCampaign(coupon.getCampaignUID(),false);
		if(campaignCheck!=null)
			throw new IllegalArgumentException("Cannot broadcast a coupon using a campaign ID that already exists.");

		String name = "Broadcast ";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		name += dateFormat.format(new Date());
		
		Campaign campaign = new Campaign();
		campaign.setName(name);
		campaign.setUid(coupon.getCampaignUID());
		campaign.setMode(CampaignMode.Broadcast);
		campaign.setClientPK(clientPK);
		save(campaign);
		
		coupon = (CouponNode)save(coupon);
		
		OutgoingEntryNode entry = new OutgoingEntryNode();
		entry.setCampaignUID(campaign.getUid());
		entry.setEntryData(entryPoints);
		
		entry = (OutgoingEntryNode)save(entry);
		
		ImmediateConnector connector = new ImmediateConnector();
		connector.setCampaignUID(campaign.getUid());
		connector.setSourceNodeUID(entry.getUid());
		connector.setDestinationUID(coupon.getUid());
		
    sf.getCurrentSession().flush();
    sf.getCurrentSession().clear();
    
		connector = (ImmediateConnector)save(connector);

		sf.getCurrentSession().flush();
		sf.getCurrentSession().clear();
		
		publish(campaign.getUid());
		
		sf.getCurrentSession().flush();
		sf.getCurrentSession().clear();
		
		subscriptionManager.subscribeContactsToEntryPoint(contacts, entry.getUid());
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
	public void broadcastCouponSearch(Long clientPK, List<EntryData> entryPoints, CouponNode coupon, ContactSearchCriteria search) {
		List<Contact> contacts=new ArrayList<Contact>();
		for(EntryData entry : entryPoints)
		{
			if(entry.getMaxMessages()!=null && entry.getMaxMessages().intValue()>0)
			{
				Criteria crit = sf.getCurrentSession().createCriteria(ContactDO.class,"c");
				crit.add(Restrictions.eq("c.client.id",clientPK));
				crit.add(Restrictions.eq("c.type",entry.getEntryType()));
				crit.addOrder(new Order("rand",false){
					
					@Override
					public String toSqlString(Criteria arg0, CriteriaQuery arg1)
							throws HibernateException {
						return "rand()";
					}

					@Override
					public String toString() {
						return "rand()";
					}
				});
				crit.setMaxResults(entry.getMaxMessages());
				for(ContactDO contact : (List<ContactDO>)crit.list())
				{
					Contact c = new Contact();
					c.copyFrom(contact);
					contacts.add(c);
				}
			}
		}
		if(contacts.size()==0)
			contacts = contactManager.getContacts(search, null).getResults();
		broadcastCoupon(clientPK, entryPoints, coupon, contacts);
	}

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
  public CampaignEntryMessage loadEntryCampaign()
  {
		Set<Long> clientIds = securityUtil.extractClientIds(sf.getCurrentSession());
		if(clientIds.size()!=1)
			throw new IllegalArgumentException("Unable to determine which client's Entry Campaign to load.");
		Query q = sf.getCurrentSession().getNamedQuery("entry.campaign");
		q.setParameter("clientId", clientIds.iterator().next());
    CampaignEntryMessage ret = new CampaignEntryMessage();
    CampaignDO camp = (CampaignDO)q.uniqueResult();
    if(camp==null)
      return ret;
    if(camp.getCampaignInfos()!=null && camp.getCampaignInfos().size()>0)
      ret.setActive(true);
    String currentMessageNodeUID = null;
    for(CampaignConnectorLinkDO connectorLink : camp.getConnectors())
    {
      if(connectorLink.getVersion().intValue()!=camp.getCurrentVersion())
        continue;
      for(NodeConnectorLinkDO connection : connectorLink.getConnector().getConnections())
      {
        if(connection.getVersion().intValue()!=camp.getCurrentVersion())
          continue;
        if(connection.getConnectionPoint()==ConnectionPoint.Destination)
          currentMessageNodeUID=connection.getNode().getUID();
      }
    }
    for(CampaignNodeLinkDO nodeLink : camp.getNodes())
    {
      if(nodeLink.getVersion().intValue()!=camp.getCurrentVersion())
        continue;
      if(nodeLink.getNode().getType()==NodeType.OutgoingEntry)
      {
        OutgoingEntryNode entryNode = new OutgoingEntryNode();
        entryNode.copyFrom(nodeLink.getNode());
        ret.setEntryData(entryNode.getEntryData());
      }
      if(nodeLink.getNode().getType()==NodeType.Coupon && nodeLink.getNode().getUID().equalsIgnoreCase(currentMessageNodeUID))
      {
        CouponNode messageNode = new CouponNode();
        messageNode.copyFrom(nodeLink.getNode());
        ret.setMessageNode(messageNode);
      }
      if(nodeLink.getNode().getType()==NodeType.Message && nodeLink.getNode().getUID().equalsIgnoreCase(currentMessageNodeUID))
      {
        MessageNode messageNode = new MessageNode();
        messageNode.copyFrom(nodeLink.getNode());
        ret.setMessageNode(messageNode);
      }
    }
    return ret;
  }

  @PreAuthorize("hasRole('ROLE_client') or hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
  public CampaignEntryMessage saveEntryCampaign(CampaignEntryMessage campaignMessage)
  {
		Set<Long> clientIds = securityUtil.extractClientIds(sf.getCurrentSession());
		if(clientIds.size()!=1)
			throw new IllegalArgumentException("Unable to determine which client's Entry Campaign to create.");
		Query q = sf.getCurrentSession().getNamedQuery("entry.campaign");
		q.setParameter("clientId", clientIds.iterator().next());
    CampaignEntryMessage ret = new CampaignEntryMessage();
    CampaignDO camp = (CampaignDO)q.uniqueResult();
    if(camp!=null)
    {
			String entryUID=null;
			
			Campaign existingCamp = getDetailedCampaign(camp.getUID());
                        MessageNode existingMessageNode = null;
                        CouponNode existingCouponNode = null;
			for(Node node : existingCamp.getNodes())
			{
				if(node.getType()==NodeType.OutgoingEntry)
				{
					((OutgoingEntryNode)node).setEntryData(campaignMessage.getEntryData());
					((ImmediateConnector)existingCamp.getConnectors().iterator().next()).setSourceNodeUID(save(node).getUid());
					entryUID=node.getUid();
				}
				if(node.getType()==NodeType.Message)
				{
                                    existingMessageNode = (MessageNode)node;
				}
				if(node.getType()==NodeType.Coupon)
				{
                                    existingCouponNode = (CouponNode)node;
				}
			}
      if(campaignMessage.getMessageNode().getType()==NodeType.Message)
      {
          if(existingMessageNode==null)
          {
              campaignMessage.getMessageNode().setCampaignUID(existingCamp.getUid());
              existingMessageNode = (MessageNode)save(campaignMessage.getMessageNode());
          } else {
              existingMessageNode.setMessage(((MessageNode)campaignMessage.getMessageNode()).getMessage());
              existingMessageNode.setMessages(((MessageNode)campaignMessage.getMessageNode()).getMessages());
              campaignMessage.setMessageNode(save(existingMessageNode));
          }
          ((ImmediateConnector)existingCamp.getConnectors().iterator().next()).setDestinationUID(existingMessageNode.getUid());
      } else {
          if(existingCouponNode==null)
          {
              campaignMessage.getMessageNode().setCampaignUID(existingCamp.getUid());
              existingCouponNode = (CouponNode)save(campaignMessage.getMessageNode());
          } else {
              CouponNode newCNode = (CouponNode)campaignMessage.getMessageNode();
              existingCouponNode.setAvailableMessage(newCNode.getAvailableMessage());
              existingCouponNode.setAvailableMessages(newCNode.getAvailableMessages());
              existingCouponNode.setCouponCode(newCNode.getCouponCode());
              existingCouponNode.setExpireDate(newCNode.getExpireDate());
              existingCouponNode.setExpireDays(newCNode.getExpireDays());
              existingCouponNode.setMaxCoupons(newCNode.getMaxCoupons());
              existingCouponNode.setMaxRedemptions(newCNode.getMaxRedemptions());
              existingCouponNode.setOfferCode(newCNode.getOfferCode());
              existingCouponNode.setUnavailableDate(newCNode.getUnavailableDate());
              existingCouponNode.setUnavailableMessage(newCNode.getUnavailableMessage());
              existingCouponNode.setUnavailableMessages(newCNode.getUnavailableMessages());
              campaignMessage.setMessageNode(save(existingCouponNode));
          }
          ((ImmediateConnector)existingCamp.getConnectors().iterator().next()).setDestinationUID(existingCouponNode.getUid());
      }
			sf.getCurrentSession().flush();
			sf.getCurrentSession().clear();
			save(existingCamp.getConnectors().iterator().next());
			for(CampaignInfo ci : existingCamp.getCampaignInfos())
				if(ci.getName().equals("autoStartNodeUID"))
					ci.setValue(null);
			for(EntryData entryData : campaignMessage.getEntryData())
			{
				CampaignInfo selectedCI = null;
				for(CampaignInfo ci : existingCamp.getCampaignInfos())
				{
					if(ci.getEntryType()==entryData.getEntryType() && ci.getName().equals("autoStartNodeUID"))
					{
						selectedCI = ci;
						break;
					}
				}
				if(selectedCI==null)
				{
					selectedCI = new CampaignInfo();
					selectedCI.setCampaignId(existingCamp.getPrimaryKey());
					selectedCI.setEntryType(entryData.getEntryType());
					selectedCI.setName("autoStartNodeUID");
					existingCamp.getCampaignInfos().add(selectedCI);

				}
				if(campaignMessage.isActive()) //This will keep autostart entries from being made if the welcome message is deactivated.
					selectedCI.setValue(entryUID);
			}
			existingCamp = save(existingCamp);
			publish(existingCamp.getUid());
		} else {
			Campaign entryCamp = new Campaign();
			entryCamp.setClientPK(clientIds.iterator().next());
			entryCamp.setName("Client "+entryCamp.getClientPK()+" Entry Message");
			entryCamp.setMode(CampaignMode.Entry);
			entryCamp = save(entryCamp);
			
			OutgoingEntryNode entry = new OutgoingEntryNode();
			entry.setCampaignUID(entryCamp.getUid());
			entry.setEntryData(campaignMessage.getEntryData());
			entry = (OutgoingEntryNode)save(entry);
			
			campaignMessage.getMessageNode().setCampaignUID(entryCamp.getUid());
			Node message = save(campaignMessage.getMessageNode());
			
			ImmediateConnector connector = new ImmediateConnector();
			connector.setCampaignUID(entryCamp.getUid());
			connector.setSourceNodeUID(entry.getUid());
			connector.setDestinationUID(message.getUid());
			connector = (ImmediateConnector)save(connector);
			
			sf.getCurrentSession().clear();
			
			for(EntryData entryData : campaignMessage.getEntryData())
			{
				CampaignInfo ci = new CampaignInfo();
				ci.setEntryType(entryData.getEntryType());
				ci.setName("autoStartNodeUID");
				ci.setValue(entry.getUid());
				entryCamp.getCampaignInfos().add(ci);
			}
			save(entryCamp);
			publish(entryCamp.getUid());
		}
		return campaignMessage;
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
