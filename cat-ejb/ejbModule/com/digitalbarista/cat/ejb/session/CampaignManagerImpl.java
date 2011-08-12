package com.digitalbarista.cat.ejb.session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditInterceptor;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.AddInMessage;
import com.digitalbarista.cat.business.BroadcastInfo;
import com.digitalbarista.cat.business.CalendarConnector;
import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.CampaignEntryMessage;
import com.digitalbarista.cat.business.CampaignInfo;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Contact;
import com.digitalbarista.cat.business.ContactTag;
import com.digitalbarista.cat.business.CouponNode;
import com.digitalbarista.cat.business.EntryData;
import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.ImmediateConnector;
import com.digitalbarista.cat.business.IntervalConnector;
import com.digitalbarista.cat.business.LayoutInfo;
import com.digitalbarista.cat.business.MessageNode;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.OutgoingEntryNode;
import com.digitalbarista.cat.business.ResponseConnector;
import com.digitalbarista.cat.business.TaggingNode;
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

/**
 * Session Bean implementation class CampaignManagerImpl
 */

@Stateless
@LocalBinding(jndiBinding = "ejb/cat/CampaignManager")
@Interceptors({AuditInterceptor.class})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CampaignManagerImpl implements CampaignManager {

	Logger log = LogManager.getLogger(getClass());
	
	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@EJB(name="ejb/cat/EventTimerManager")
	EventTimerManager timer;
	
	@EJB(name="ejb/cat/EventManager")
	EventManager eventManager;
	
	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
	@EJB(name="ejb/cat/LayoutManager")
	LayoutManager layoutManager;
	
	@EJB(name="ejb/cat/ContactManager")
	ContactManager contactManager;
	
	@EJB(name="ejb/cat/CacheAccessManager")
	CacheAccessManager cacheAccessManager;
	
	@Override
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<Campaign> getAllCampaigns() 
	{
		return getCampaigns(null);
	}

	@Override
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<Campaign> getAllTemplates() 
	{
		return getCampaignTemplates(null);
	}

	public List<Campaign> getCampaigns(List<Long> clientIDs)
	{
		List<Campaign> ret = new ArrayList<Campaign>();
		Campaign c;
		
		List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIDs);
		
		if (allowedClientIDs.size() > 0)
		{
			Criteria crit = session.createCriteria(CampaignDO.class);
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
				Criteria countCrit = session.createCriteria(CampaignSubscriberLinkDO.class, "csl");
				countCrit.add(Restrictions.in("campaign.id", campIDList));
				countCrit.add(Restrictions.eq("active",true));
				ProjectionList pList = Projections.projectionList();
				pList.add(Projections.count("id"));
				pList.add(Projections.property("campaign.id"));
				pList.add(Projections.groupProperty("campaign.id"));
				countCrit.setProjection(pList);
				List<Object[]> result = (List<Object[]>)countCrit.list();
				for(Object[] row : result)
					campCounts.put((Long)row[1], (Integer)row[0]);
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

	public List<BroadcastInfo> getBroadcastCampaigns(List<Long> clientIDs)
	{
		List<BroadcastInfo> ret = new ArrayList<BroadcastInfo>();
		BroadcastInfo c;
		
		List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIDs);
		
		if (allowedClientIDs.size() > 0)
		{
			Criteria crit = session.createCriteria(CampaignDO.class);
			crit.add(Restrictions.eq("status", CampaignStatus.Active));
			crit.add(Restrictions.eq("mode", CampaignMode.Broadcast));
			crit.add(Restrictions.in("client.id", allowedClientIDs));
			
			List<CampaignDO> campList = (List<CampaignDO>)crit.list();
			Map<Long,Integer> campCounts = new HashMap<Long,Integer>();
			if(campList!=null && campList.size()>1)
			{
				List<Long> campIDList = new ArrayList<Long>();
				for(CampaignDO camp : campList)
					campIDList.add(camp.getPrimaryKey());
				Criteria countCrit = session.createCriteria(CampaignSubscriberLinkDO.class, "csl");
				countCrit.add(Restrictions.in("campaign.id", campIDList));
				countCrit.add(Restrictions.eq("active",true));
				ProjectionList pList = Projections.projectionList();
				pList.add(Projections.count("id"));
				pList.add(Projections.property("campaign.id"));
				pList.add(Projections.groupProperty("campaign.id"));
				countCrit.setProjection(pList);
				List<Object[]> result = (List<Object[]>)countCrit.list();
				for(Object[] row : result)
					campCounts.put((Long)row[1], (Integer)row[0]);
			}
				
			for(CampaignDO cmp : campList)
			{
				c = new BroadcastInfo();
				c.copyFrom(cmp);
				if(campCounts.containsKey(cmp.getPrimaryKey()))
					c.setSubscriberCount(campCounts.get(cmp.getPrimaryKey()));
				if(c.isCoupon())
				{
					Criteria couponCount = session.createCriteria(CouponRedemptionDO.class, "cr");
					couponCount.createAlias("couponResponse", "resp");
					couponCount.add(Restrictions.eq("resp.couponOffer.id", c.getCouponId()));
					couponCount.setProjection(Projections.rowCount());
					c.setCouponRedemptionCount((Integer)couponCount.uniqueResult());
				}
				ret.add(c);
			}
		}
		
		return ret;
	}
	
	public List<Campaign> getCampaignTemplates(List<Long> clientIDs)
	{
		List<Campaign> ret = new ArrayList<Campaign>();
		Campaign c;
		List<Long> allowedClientIDs = SecurityUtil.getAllowedClientIDs(ctx, session, clientIDs);
		
		if (allowedClientIDs.size() > 0)
		{
			Criteria crit = session.createCriteria(CampaignDO.class);
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
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	@PermitAll
	public ConnectorDO getSimpleConnector(String connectorUUID)
	{
			Criteria crit = session.createCriteria(ConnectorDO.class);
			crit.add(Restrictions.eq("UID", connectorUUID));
			crit.setCacheable(true);
			crit.setCacheRegion("query/simpleConnector");
			ConnectorDO ret = (ConnectorDO)crit.uniqueResult();
			
			if(ret==null)
				return null;
			
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), ret.getCampaign().getClient().getPrimaryKey()))
				throw new SecurityException("Current user is not allowed to access the specified connector.");
			
			return ret;
	}
	
	@Override
	@PermitAll
	public Connector getConnector(String connectorUUID) {
		ConnectorDO conn = getSimpleConnector(connectorUUID);
		Connector ret = Connector.createConnectorBO(conn);
		ret.copyFrom(conn);
		return ret;
	}

	@Override
	@PermitAll
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
	
	@PermitAll
	public CampaignDO getSimpleCampaign(String campaignUUID)
	{
		return getSimpleCampaign(campaignUUID, false);
	}
	
	@PermitAll
	public CampaignDO getSimpleCampaign(String campaignUUID, boolean eagerFetch)
	{
		try
		{
			Criteria crit = session.createCriteria(CampaignDO.class);
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
			
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), ret.getClient().getPrimaryKey()))
				throw new SecurityException("Current user is not allowed to access the specified campaign.");

			return ret;
		}
		catch(NoResultException e)
		{
			return null;
		}
	}
	
	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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

	@PermitAll
	public NodeDO getSimpleNode(String nodeUUID)
	{
		try
		{
			Criteria crit = session.createCriteria(NodeDO.class);
			crit.add(Restrictions.eq("UID", nodeUUID));
			crit.setCacheable(true);
			crit.setCacheRegion("query/SimpleNode");
			NodeDO ret = (NodeDO)crit.uniqueResult();
			
			if(ret==null)
				return null;
			
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), ret.getCampaign().getClient().getPrimaryKey()))
				throw new SecurityException("Current user is not allowed to access the specified node.");

			return ret;
		}catch(NoResultException e)
		{
			return null;
		}
	}
	
	@Override
	@PermitAll
	public Node getNode(String nodeUUID) {
		NodeDO node = getSimpleNode(nodeUUID);
		return getSpecificNodeVersion(nodeUUID,node.getCampaign().getCurrentVersion());
	}

	@Override
	@PermitAll
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
					ct.copyFrom(em.find(ContactTagDO.class, new Long(ni.getValue())));
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
	
	@Override
	@PermitAll
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

	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
				em.persist(newCCL);
				
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
					em.persist(newCI);
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
					em.persist(newNCL);
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
						offer = em.find(CouponOfferDO.class, cNode.getCouponId());
					offer.setMaxCoupons(cNode.getMaxCoupons());
					offer.setOfferUnavailableDate(cNode.getUnavailableDate());
					offer.setNodeUID(cNode.getUid());
					offer.setMaxRedemptions(cNode.getMaxRedemptions());
					offer.setCouponName(cNode.getName());
					offer.setCampaign(cnl.getCampaign());
					offer.setCouponExpirationDate(cNode.getExpireDate());
					offer.setCouponExpirationDays(cNode.getExpireDays());
					offer.setOfferCode(cNode.getOfferCode());
					em.persist(offer);
					
					cNode.associateCouponOffer(offer);
					cNode.copyTo(cnl.getNode());
				}
				
				em.persist(newCNL);
				
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
					em.persist(newNI);
				}
			}
			
			Query q = em.createNamedQuery("layouts.by.campaign.version");
			q.setParameter("campaignPK", camp.getPrimaryKey());
			q.setParameter("version",version);
			LayoutInfoDO newLI;
			for(LayoutInfoDO li : (List<LayoutInfoDO>)q.getResultList())
			{
				newLI=new LayoutInfoDO();
				newLI.setCampaign(li.getCampaign());
				newLI.setUID(li.getUID());
				newLI.setXLoc(li.getXLoc());
				newLI.setYLoc(li.getYLoc());
				newLI.setVersion(version+1);
				em.persist(newLI);
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
				q=em.createNamedQuery("subscription.count.for.node");
				q.setParameter("nodeUID", nodeUID);
				if(((Long)q.getSingleResult())>0)
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
						q=em.createNamedQuery("clear.tasks.for.connector");
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
						q = em.createNamedQuery("all.subscribers.on.node");
						q.setParameter("nodeUID", tempConnector.getSourceNodeUID());
						List<SubscriberDO> subs = q.getResultList();
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
					em.remove(cep);
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
			ctx.setRollbackOnly();
			throw new RuntimeException(e);
		}
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.SaveCampaign)
	public Campaign save(Campaign campaign) {
		CampaignDO camp = getSimpleCampaign(campaign.getUid());
		if(camp==null && campaign.getClientPK()==null)
			throw new IllegalArgumentException("Cannot create a new campaign without a valid client PK.");
		if(camp!=null && !camp.getClient().getPrimaryKey().equals(campaign.getClientPK()))
			throw new IllegalArgumentException("Cannot change the client ID associated with the campaign.");
		if(camp==null)
		{
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), campaign.getClientPK()))
				throw new SecurityException("Current user is not allowed to create campaigns for the specified client.");	
			
			camp = new CampaignDO();
			camp.setClient(em.find(ClientDO.class, campaign.getClientPK()));
			camp.setUID(campaign.getUid());
			camp.setMode(campaign.getMode());
			if(camp.getUID()==null || camp.getUID().trim().length()==0)
				camp.setUID(UUID.randomUUID().toString());
		}
		campaign.copyTo(camp);
		
		// Save campaign
		em.persist(camp);
		
		// Update list of addin messages
		if (campaign.getAddInMessages() != null)
		{
			for (AddInMessage add : campaign.getAddInMessages())
			{
					
				// Get existing or new data object
				AddInMessageDO addDO = null;
				if (add.getAddInMessageId() != null)
					addDO = em.find(AddInMessageDO.class, add.getAddInMessageId());
				if (addDO == null)
					addDO = new AddInMessageDO();

				// Campaigns should only have CLIENT addin messages
				if ( addDO.getAddInMessageId() == null &&
					add.getType() != AddInMessageType.CLIENT)
					throw new IllegalArgumentException("Campaings should not have this type of addin message assigned");
				
				// Update and persist object
				addDO.setCampaign(camp);
				add.copyTo(addDO);
				em.persist(addDO);
				
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
					ciDO = em.find(CampaignInfoDO.class, ci.getCampaignInfoId());
				if (ciDO == null)
					ciDO = new CampaignInfoDO();

				
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
					em.persist(ciDO);
					// Add info to list
					if (camp.getCampaignInfos() == null)
						camp.setCampaignInfos(new HashSet<CampaignInfoDO>());
					camp.getCampaignInfos().add(ciDO);
				} else {
					camp.getCampaignInfos().remove(ciDO);
					em.remove(ciDO);
				}
			}
		}
		
		em.flush();
		
		// Return updated campaign
		Campaign ret = new Campaign();
		ret.copyFrom(camp);
		return ret;
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.CreateCampaignFromTemplate)
	public Campaign createFromTemplate(Campaign campaign, String campaignTemplateUUID)
	{
		Campaign template = getDetailedCampaign(campaignTemplateUUID);
		if(template==null)
			throw new IllegalArgumentException("Template '"+campaignTemplateUUID+"' could not be found.");
		if(template.getMode().equals(CampaignMode.Normal))
			throw new IllegalArgumentException("Specified UUID is not a template!");
		
		Map<String,String> oldNewUIDMap = new HashMap<String,String>();
		
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
		Query q = em.createNamedQuery("find.matching.entry.point");
		q.setParameter("entryPoint", entryPoint);
		q.setParameter("keyword", keyword);
		q.setParameter("type", type);
		try
		{
			return (CampaignEntryPointDO)q.getSingleResult();
		}
		catch(NoResultException e)
		{
			return null;
		}
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
	
	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
			
			em.persist(n);
			em.persist(cnl);
			
			node.copyFrom(n);

		} else {
			if(camp!=null && !camp.getUID().equals(node.getCampaignUID()))
				throw new IllegalArgumentException("Cannot change the campaign a node is associated with.");

			if(node.getType().equals(NodeType.Entry))
				recordEntryPointChanges(node,camp);

			node.copyTo(n);
			em.persist(n);
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
					em.remove(cep);
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
				em.persist(cep);
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
					em.remove(cep);
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
					em.remove(cep);
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
				em.persist(cep);
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
					em.remove(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
			}
		}
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
			
			em.persist(c);
			em.persist(ccl);

			if(connector.getSourceNodeUID()!=null)
			{
				source = new NodeConnectorLinkDO();
				source.setVersion(camp.getCurrentVersion());
				source.setNode(getSimpleNode(connector.getSourceNodeUID()));
				source.setConnectionPoint(ConnectionPoint.Source);
				if(source.getNode()==null)
					throw new IllegalArgumentException("Source NodeDO could not be found.");
				source.setConnector(c);
				em.persist(source);
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
				em.persist(dest);
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
				em.persist(source);
			}
			
			if(source!=null && connector.getSourceNodeUID()==null)
			{
				em.remove(source);
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
				em.persist(dest);
			}
			
			if(dest!=null && connector.getDestinationUID()==null)
			{
				em.remove(dest);
			}
			
			connector.copyTo(c);
			em.persist(c);
			connector.copyFrom(c);
		}
		
		return connector;
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.DeleteCampaign)
	public void delete(Campaign campaign) {
		if(campaign==null || campaign.getUid()==null)
			throw new IllegalArgumentException("Cannot delete a non-existant campaign.");
		
		CampaignDO camp = getSimpleCampaign(campaign.getUid());
		if(camp==null)
			return;
		
		Query q = em.createNamedQuery("delete.campaign.entry.points");
		q.setParameter("campaignID", camp.getPrimaryKey());
		q.executeUpdate();
		
		camp.setStatus(CampaignStatus.Deleted);
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
						em.remove(cep);
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
					em.remove(link);
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
					em.remove(ni);
				}
			}
		}
		n.getNodeInfo().removeAll(removalNI);
		
		em.remove(n.getVersionedNodes().get(currentVersion));
		n.getVersionedNodes().remove(currentVersion);
		if(n.getVersionedNodes().size()==0)
			em.remove(n);
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
						em.remove(cep);
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
					em.remove(link);
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
					em.remove(ci);
				}
			}
		}
		c.getConnectorInfo().removeAll(removalCI);
		
		em.remove(c.getVersionedConnectors().get(currentVersion));
		c.getVersionedConnectors().remove(currentVersion);
		if(c.getVersionedConnectors().size()==0)
			em.remove(c);
	}

	@Override
	@PermitAll
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

	@Override
	@PermitAll
	public Map<String, Long> getNodeSubscriberCount(String campaignUUID) {
		getSimpleCampaign(campaignUUID); // Do nothing with this except invoke security checks.
		
		String queryString="select csl.lastHitNode.UID, count(csl.lastHitNode.UID) from CampaignSubscriberLinkDO csl where csl.campaign.UID=:campaignUID and csl.active=1 group by csl.lastHitNode.UID";
		Query q = em.createQuery(queryString);
		q.setParameter("campaignUID", campaignUUID);
		List<Object[]> result = q.getResultList();
		HashMap<String,Long> ret = new HashMap<String,Long>();
		for(Object[] row : result)
			ret.put((String)row[0], (Long)row[1]);
		return ret;
	}

	@Override
	public void deleteCampaign(String uid) {
		delete(getDetailedCampaign(uid));
	}

	@Override
	public void deleteConnector(String uid) {
		delete(getConnector(uid));
	}

	@Override
	public void deleteNode(String uid) {
		delete(getNode(uid));
	}
	
	@Override
	public CampaignDO getCampaignForNode(String uid) {
		String query = "select c from NodeDO n join n.campaign c where n.UID=:uid";
		return (CampaignDO)em.createQuery(query).setParameter("uid", uid).getSingleResult();
	}

	@Override
	public CampaignDO getCampaignForConnector(String uid) {
		String query = "select c from ConnectorDO con join con.campaign c where con.UID=:uid";
		return (CampaignDO)em.createQuery(query).setParameter("uid", uid).getSingleResult();
	}

	@Override
	public Integer getCurrentCampaignVersionForNode(String uid) {
		String query = "select c.currentVersion from NodeDO n join n.campaign c where n.UID=:uid";
		return (Integer)em.createQuery(query).setParameter("uid", uid).getSingleResult();
	}

	@Override
	public Integer getCurrentCampaignVersionForConnector(String uid) {
		String query = "select c.currentVersion from ConnectorDO con join con.campaign c where con.UID=:uid";
		return (Integer)em.createQuery(query).setParameter("uid", uid).getSingleResult();
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
		
		save(message);
		
		OutgoingEntryNode entry = new OutgoingEntryNode();
		entry.setCampaignUID(campaign.getUid());
		entry.setEntryData(entryPoints);
		
		save(entry);
		
		ImmediateConnector connector = new ImmediateConnector();
		connector.setCampaignUID(campaign.getUid());
		connector.setSourceNodeUID(entry.getUid());
		connector.setDestinationUID(message.getUid());
		
		save(connector);

		em.clear();

		publish(campaign.getUid());
		
		SubscriptionManager subscriptionManager = (SubscriptionManager)ctx.lookup("ejb/cat/SubscriptionManager");
		subscriptionManager.subscribeContactsToEntryPoint(contacts, entry.getUid());
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
	public void broadcastMessageSearch(Long clientPK, List<EntryData> entryPoints, MessageNode message, ContactSearchCriteria search) {
		List<Contact> contacts=new ArrayList<Contact>();
		for(EntryData entry : entryPoints)
		{
			if(entry.getMaxMessages()!=null && entry.getMaxMessages().intValue()>0)
			{
				Criteria crit = session.createCriteria(ContactDO.class,"c");
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

	@Override
	@RolesAllowed({"client","admin","account.manager"})
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
		
		save(coupon);
		
		OutgoingEntryNode entry = new OutgoingEntryNode();
		entry.setCampaignUID(campaign.getUid());
		entry.setEntryData(entryPoints);
		
		save(entry);
		
		ImmediateConnector connector = new ImmediateConnector();
		connector.setCampaignUID(campaign.getUid());
		connector.setSourceNodeUID(entry.getUid());
		connector.setDestinationUID(coupon.getUid());
		
		save(connector);

		em.clear();
		
		publish(campaign.getUid());
		
		em.clear();
		
		SubscriptionManager subscriptionManager = (SubscriptionManager)ctx.lookup("ejb/cat/SubscriptionManager");
		subscriptionManager.subscribeContactsToEntryPoint(contacts, entry.getUid());
	}

	@Override
	@RolesAllowed({"client","admin","account.manager"})
	public void broadcastCouponSearch(Long clientPK, List<EntryData> entryPoints, CouponNode coupon, ContactSearchCriteria search) {
		List<Contact> contacts=new ArrayList<Contact>();
		for(EntryData entry : entryPoints)
		{
			if(entry.getMaxMessages()!=null && entry.getMaxMessages().intValue()>0)
			{
				Criteria crit = session.createCriteria(ContactDO.class,"c");
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

	@Override
	@RolesAllowed({"client","admin","account.manager"})
    public CampaignEntryMessage loadEntryCampaign()
    {
		Set<Long> clientIds = SecurityUtil.extractClientIds(ctx, session);
		if(clientIds.size()!=1)
			throw new IllegalArgumentException("Unable to determine which client's Entry Campaign to load.");
		Query q = em.createNamedQuery("entry.campaign");
		q.setParameter("clientId", clientIds.iterator().next());
		try
		{
			CampaignEntryMessage ret = new CampaignEntryMessage();
			CampaignDO camp = (CampaignDO)q.getSingleResult();
			if(camp==null)
			{
				ret.setActive(false);
				return ret;
			}
			if(camp.getNodes().size()!=2 || camp.getConnectors().size()!=1)
				throw new IllegalArgumentException("Welcome message campaigns must contain exactly one immediate connector, one entry node, and one message node.");
			ret.setActive(true);
			for(CampaignNodeLinkDO nodeLink : camp.getNodes())
			{
				if(nodeLink.getNode().getType()==NodeType.OutgoingEntry)
				{
					OutgoingEntryNode entryNode = new OutgoingEntryNode();
					entryNode.copyFrom(nodeLink.getNode());
					ret.setEntryData(entryNode.getEntryData());
				}
				if(nodeLink.getNode().getType()==NodeType.Coupon)
				{
					CouponNode messageNode = new CouponNode();
					messageNode.copyFrom(nodeLink.getNode());
					ret.setMessageNode(messageNode);
				}
				if(nodeLink.getNode().getType()==NodeType.Message)
				{
					MessageNode messageNode = new MessageNode();
					messageNode.copyFrom(nodeLink.getNode());
					ret.setMessageNode(messageNode);
				}
			}
			return ret;
		}
		catch(NoResultException e)
		{
			return new CampaignEntryMessage();
		}
    }

	@Override
	@RolesAllowed({"client","admin","account.manager"})
    public CampaignEntryMessage saveEntryCampaign(CampaignEntryMessage campaignMessage)
    {
		Set<Long> clientIds = SecurityUtil.extractClientIds(ctx, session);
		if(clientIds.size()!=1)
			throw new IllegalArgumentException("Unable to determine which client's Entry Campaign to create.");
		Query q = em.createNamedQuery("entry.campaign");
		q.setParameter("clientId", clientIds.iterator().next());
		try
		{
			CampaignEntryMessage ret = new CampaignEntryMessage();
			CampaignDO camp = (CampaignDO)q.getSingleResult();
			if(camp.getNodes().size()!=2 || camp.getConnectors().size()!=1)
				throw new IllegalArgumentException("Welcome message campaigns must contain exactly one immediate connector, one entry node, and one message node.");
		
			String entryUID=null;
			
			Campaign existingCamp = getDetailedCampaign(camp.getUID());
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
					if(campaignMessage.getMessageNode().getType()!=NodeType.Message)
					{
						delete(node);
						((ImmediateConnector)existingCamp.getConnectors().iterator().next()).setDestinationUID(save(campaignMessage.getMessageNode()).getUid());
					} else {
						MessageNode mNode = (MessageNode)node;
						mNode.setMessages(((MessageNode)campaignMessage.getMessageNode()).getMessages());
						campaignMessage.setMessageNode(save(node));
					}
				}
				if(node.getType()==NodeType.Coupon)
				{
					if(campaignMessage.getMessageNode().getType()!=NodeType.Coupon)
					{
						delete(node);
						((ImmediateConnector)existingCamp.getConnectors().iterator().next()).setDestinationUID(save(campaignMessage.getMessageNode()).getUid());
					} else {
						CouponNode existingCNode = (CouponNode)node;
						CouponNode newCNode = (CouponNode)campaignMessage.getMessageNode();
						existingCNode.setAvailableMessages(newCNode.getAvailableMessages());
						existingCNode.setCouponCode(newCNode.getCouponCode());
						existingCNode.setExpireDate(newCNode.getExpireDate());
						existingCNode.setExpireDays(newCNode.getExpireDays());
						existingCNode.setMaxCoupons(newCNode.getMaxCoupons());
						existingCNode.setMaxRedemptions(newCNode.getMaxRedemptions());
						existingCNode.setOfferCode(newCNode.getOfferCode());
						existingCNode.setUnavailableDate(newCNode.getUnavailableDate());
						existingCNode.setUnavailableMessages(newCNode.getUnavailableMessages());
						campaignMessage.setMessageNode(save(node));
					}
				}
			}
			save(existingCamp.getConnectors().iterator().next());
			existingCamp.getCampaignInfos().clear();
			for(EntryData entryData : campaignMessage.getEntryData())
			{
				CampaignInfo ci = new CampaignInfo();
				ci.setEntryType(entryData.getEntryType());
				ci.setName("autoStartNodeUID");
				ci.setValue(entryUID);
				existingCamp.getCampaignInfos().add(ci);
			}
			existingCamp = save(existingCamp);
		}
		catch(NoResultException e)
		{
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
			
			em.clear();
			
			for(EntryData entryData : campaignMessage.getEntryData())
			{
				CampaignInfo ci = new CampaignInfo();
				ci.setEntryType(entryData.getEntryType());
				ci.setName("autoStartNodeUID");
				ci.setValue(entry.getUid());
				entryCamp.getCampaignInfos().add(ci);
			}
			save(entryCamp);
		}
		return campaignMessage;
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
