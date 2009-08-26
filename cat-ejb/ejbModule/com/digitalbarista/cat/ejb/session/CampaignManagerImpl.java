package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditInterceptor;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.CalendarConnector;
import com.digitalbarista.cat.business.Campaign;
import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.EntryNode;
import com.digitalbarista.cat.business.IntervalConnector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.business.ResponseConnector;
import com.digitalbarista.cat.data.CampaignConnectorLinkDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignEntryPointDO;
import com.digitalbarista.cat.data.CampaignNodeLinkDO;
import com.digitalbarista.cat.data.CampaignStatus;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ConnectionPoint;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.LayoutInfoDO;
import com.digitalbarista.cat.data.NodeConnectorLinkDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.message.event.CATEvent;
import com.digitalbarista.cat.message.event.CATEventType;

/**
 * Session Bean implementation class CampaignManagerImpl
 */

@Stateless
@LocalBinding(jndiBinding = "ejb/cat/CampaignManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@Interceptors(AuditInterceptor.class)
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
	
	@Override
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<Campaign> getAllCampaigns() {
		List<Campaign> ret = new ArrayList<Campaign>();
		Campaign c;
		Criteria crit = session.createCriteria(CampaignDO.class);
		crit.add(Restrictions.eq("status", CampaignStatus.Active));
		
		if(!ctx.isCallerInRole("admin"))
			crit.add(Restrictions.in("client.id", userManager.extractClientIds(ctx.getCallerPrincipal().getName())));
		
		for(CampaignDO cmp : (List<CampaignDO>)crit.list())
		{
			c = new Campaign();
			c.copyFrom(cmp);
			ret.add(c);
		}
		
		return ret;
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	@PermitAll
	public ConnectorDO getSimpleConnector(String connectorUUID)
	{
			Criteria crit = session.createCriteria(ConnectorDO.class);
			crit.add(Restrictions.eq("UID", connectorUUID));
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
		ConnectorDO conn = getSimpleConnector(connectorUUID);
		Connector ret = Connector.createConnectorBO(conn);
		ret.copyFrom(conn,version);
		return ret;
	}
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	@PermitAll
	public CampaignDO getSimpleCampaign(String campaignUUID)
	{
		try
		{
			Criteria crit = session.createCriteria(CampaignDO.class);
			crit.add(Restrictions.eq("UID", campaignUUID));
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
	public Campaign getDetailedCampaign(String campaignUUID) {
		Campaign campaign = new Campaign();
		CampaignDO dataCPN = getSimpleCampaign(campaignUUID);
		campaign.copyFrom(dataCPN);
		
		Node tempNode;
		for(CampaignNodeLinkDO nodeLink : dataCPN.getNodes())
		{
			if(!nodeLink.getVersion().equals(campaign.getCurrentVersion()))
				continue;
			tempNode = Node.createNodeBO(nodeLink.getNode());
			tempNode.copyFrom(nodeLink.getNode());
			campaign.getNodes().add(tempNode);
		}
		
		Connector tempConn;
		for(CampaignConnectorLinkDO connLink : dataCPN.getConnectors())
		{
			if(!connLink.getVersion().equals(campaign.getCurrentVersion()))
				continue;
			tempConn = Connector.createConnectorBO(connLink.getConnector());
			tempConn.copyFrom(connLink.getConnector());
			campaign.getConnectors().add(tempConn);
		}
		
		return campaign;
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	@PermitAll
	public NodeDO getSimpleNode(String nodeUUID)
	{
		try
		{
			Criteria crit = session.createCriteria(NodeDO.class);
			crit.add(Restrictions.eq("UID", nodeUUID));
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
		Node ret = Node.createNodeBO(node);
		ret.copyFrom(node);
		return ret;
	}

	@Override
	@PermitAll
	public Node getSpecificNodeVersion(String nodeUUID, Integer versionNumber)
	{
		NodeDO node = getSimpleNode(nodeUUID);
		Node ret = Node.createNodeBO(node);
		ret.copyFrom(node,versionNumber);
		return ret;
	}
	
	@Override
	@PermitAll
	public Campaign getSpecificCampaignVersion(String campaignUUID, int version) {
		Campaign campaign = new Campaign();
		CampaignDO dataCPN = getSimpleCampaign(campaignUUID);
		campaign.copyFrom(dataCPN,version);
		
		Node tempNode;
		for(CampaignNodeLinkDO nodeLink : dataCPN.getNodes())
		{
			if(!nodeLink.getVersion().equals(version))
				continue;
			tempNode = Node.createNodeBO(nodeLink.getNode());
			tempNode.copyFrom(nodeLink.getNode(),version);
			campaign.getNodes().add(tempNode);
		}
		
		Connector tempConn;
		for(CampaignConnectorLinkDO connLink : dataCPN.getConnectors())
		{
			if(!connLink.getVersion().equals(version))
				continue;
			tempConn = Connector.createConnectorBO(connLink.getConnector());
			tempConn.copyFrom(connLink.getConnector(),version);
			campaign.getConnectors().add(tempConn);
		}
		
		return campaign;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.PublishCampaign)
	public void publish(String campaignUUID) {
		try
		{
			CampaignDO camp = getSimpleCampaign(campaignUUID);
			if(camp==null)
				throw new IllegalArgumentException("Campaign '"+campaignUUID+"' could not be found.");
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
				if(((Integer)q.getSingleResult())>0)
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
						q.executeUpdate();
				}
			}
			
			diffSet = new HashSet<String>(newVersionConnectors);
			diffSet.removeAll(previousVersionConnectors);
			
			//All new connectors . . . 
			for(String connUID : diffSet)
			{
				tempConnector=getSpecificConnectorVersion(connUID,version);
				switch(tempConnector.getType())
				{
					case Calendar:
						timer.setTimer(connUID, null, CATEventType.ConnectorFired, ((CalendarConnector)tempConnector).getTargetDate());
					break;
					
					case Immediate:
						eventManager.queueEvent(CATEvent.buildFireConnectorForAllSubscribersEvent(connUID));
					break;
					
					case Interval:
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
							timer.setTimer(iConn.getUid(), sub.getPrimaryKey().toString(), CATEventType.ConnectorFired, c.getTime());
						}
					break;
				}
			}
			
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.SaveCampaign)
	public void save(Campaign campaign) {
		CampaignDO camp = getSimpleCampaign(campaign.getUid());
		if(camp==null && campaign.getClientPK()==null)
			throw new IllegalArgumentException("Cannot create a new campaign without a valid client PK.");
		if(camp!=null && !camp.getClient().getPrimaryKey().equals(campaign.getClientPK()))
			throw new IllegalArgumentException("Cannot change the client ID associated with the campaign.");
		if(camp!=null && !camp.getCampaignType().equals(campaign.getType()))
			throw new IllegalArgumentException("Cannot change the campaign type.");
		if(camp==null)
		{
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), campaign.getClientPK()))
				throw new SecurityException("Current user is not allowed to create campaigns for the specified client.");	
			
			camp = new CampaignDO();
			camp.setClient(em.find(ClientDO.class, campaign.getClientPK()));
			camp.setUID(campaign.getUid());
			camp.setCampaignType(campaign.getType());
		}
		campaign.copyTo(camp);
		em.persist(camp);
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
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
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	protected boolean isEntryPointValid(String campaignUID, String entryPoint, EntryPointType type, String keyword)
	{
		CampaignEntryPointDO cep=getSpecificEntryPoint(entryPoint,type,keyword);
		if(cep==null)
			return true;
		if(cep.getCampaign().getUID().equals(campaignUID))
			return true;
		return false;
	}
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.SaveNode)
	public void save(Node node) {
		CampaignDO camp = getSimpleCampaign(node.getCampaignUID());
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
						
			node.copyTo(n);
			
			em.persist(n);
			em.persist(cnl);

		} else {
			if(camp!=null && !camp.getUID().equals(node.getCampaignUID()))
				throw new IllegalArgumentException("Cannot change the campaign a node is associated with.");

			if(node.getType().equals(NodeType.Entry))
				recordEntryPointChanges(node,camp);

			node.copyTo(n);
			em.persist(n);
		}
	}

	private void recordEntryPointChanges(Node node, CampaignDO camp)
	{
		EntryNode eNode = (EntryNode)node;
		EntryNode oldNode = (EntryNode)getNode(eNode.getUid());
		
		if(oldNode!=null &&
		   eNode.getEntryPoint().equals(oldNode.getEntryPoint()) &&
		   eNode.getEntryType().equals(oldNode.getEntryType()) &&
		   ((eNode.getKeyword() == null && oldNode.getKeyword() == null) || 
		   eNode.getKeyword().equals(oldNode.getKeyword()) ) )
		{
			return;
		}
		
		if(!isEntryPointValid(camp.getUID(),eNode.getEntryPoint(),eNode.getEntryType(),eNode.getKeyword()))
			throw new IllegalArgumentException("Entry point is already in use by another campaign.");

		CampaignEntryPointDO cep = getSpecificEntryPoint(oldNode.getEntryPoint(),oldNode.getEntryType(),oldNode.getKeyword());
		if(cep==null)
		{
			log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
		} else {
			if(cep.getCampaign()!=camp)
				throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
			cep.setQuantity(cep.getQuantity()-1);
			if(cep.getQuantity()==0)
				em.remove(cep);
			else if(cep.getQuantity()<0)
				log.warn("more entry points removed than have been initially catalogued");
		}

		cep = getSpecificEntryPoint(eNode.getEntryPoint(),eNode.getEntryType(),eNode.getKeyword());
		if(cep==null && isEntryPointDefinable(eNode.getEntryPoint(),eNode.getKeyword(),eNode.getEntryType()))
		{
			cep = new CampaignEntryPointDO();
			cep.setCampaign(camp);
			cep.setEntryPoint(eNode.getEntryPoint());
			cep.setType(eNode.getEntryType());
			cep.setKeyword(eNode.getKeyword());
		}
		if(cep!=null)
		{
			cep.setQuantity(cep.getQuantity()+1);
			em.persist(cep);
		}		
	}
	
	private void recordEntryPointChanges(Connector conn, CampaignDO camp)
	{
		ResponseConnector rConn = (ResponseConnector)conn;
		ResponseConnector oldConn = (ResponseConnector)getConnector(conn.getUid());
		
		if(oldConn!=null &&
			rConn.getEntryPoint().equals(oldConn.getEntryPoint()) &&
			rConn.getEntryPointType().equals(oldConn.getEntryPointType()) &&
			
			((rConn.getKeyword() == null && oldConn.getKeyword() == null) ||
			rConn.getKeyword().equals(oldConn.getKeyword()) ) )
		{
			return;
		}
		
		if(!isEntryPointValid(camp.getUID(),rConn.getEntryPoint(),rConn.getEntryPointType(),rConn.getKeyword()))
			throw new IllegalArgumentException("Entry point is already in use by another campaign.");

		CampaignEntryPointDO cep = getSpecificEntryPoint(oldConn.getEntryPoint(),oldConn.getEntryPointType(),oldConn.getKeyword());
		if(cep==null)
		{
			log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
		} else {
			if(cep.getCampaign()!=camp)
				throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
			cep.setQuantity(cep.getQuantity()-1);
			if(cep.getQuantity()==0)
				em.remove(cep);
			else if(cep.getQuantity()<0)
				log.warn("more entry points removed than have been initially catalogued");
		}

		cep = getSpecificEntryPoint(rConn.getEntryPoint(),rConn.getEntryPointType(),rConn.getKeyword());
		if(cep==null && isEntryPointDefinable(rConn.getEntryPoint(),rConn.getKeyword(),rConn.getEntryPointType()))
		{
			cep = new CampaignEntryPointDO();
			cep.setCampaign(camp);
			cep.setEntryPoint(rConn.getEntryPoint());
			cep.setType(rConn.getEntryPointType());
			cep.setKeyword(rConn.getKeyword());
		}
		if(cep!=null)
		{
			cep.setQuantity(cep.getQuantity()+1);
			em.persist(cep);
		}		
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.SaveConnection)
	public void save(Connector connector) {
		CampaignDO camp = getSimpleCampaign(connector.getCampaignUID());
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
			
			c.setCampaign(camp);
			connector.copyTo(c);
			
			NodeConnectorLinkDO source=null;
			NodeConnectorLinkDO dest=null;
			
			em.persist(c);
			em.persist(ccl);

			if(connector.getSourceNodeUID()!=null)
			{
				source = new NodeConnectorLinkDO();
				source.setNode(getSimpleNode(connector.getSourceNodeUID()));
				if(source.getNode()==null)
					throw new IllegalArgumentException("Source NodeDO could not be found.");
				source.setConnector(c);
				em.persist(source);
			}
			
			if(connector.getDestinationUID()!=null)
			{
				dest = new NodeConnectorLinkDO();
				dest.setNode(getSimpleNode(connector.getDestinationUID()));
				if(dest.getNode()==null)
					throw new IllegalArgumentException("Destination NodeDO could not be found.");
				dest.setConnector(c);
				em.persist(dest);
			}
			
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
			
			if(source==null && connector.getSourceNodeUID()!=null)
			{
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
			
			if(dest==null && connector.getDestinationUID()!=null)
			{
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
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.DeleteNode)
	public void delete(Node node) {
		if(node==null || node.getUid()==null)
			throw new IllegalArgumentException("Cannot delete a non-existant node.");
		
		if(node.getType().equals(NodeType.Entry))
		{
			EntryNode eNode = (EntryNode)node;
			CampaignEntryPointDO cep = getSpecificEntryPoint(eNode.getEntryPoint(),eNode.getEntryType(),eNode.getKeyword());
			if(cep==null)
			{
				log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
			} else {
				if(!cep.getCampaign().getUID().equals(node.getCampaignUID()))
					throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
				cep.setQuantity(cep.getQuantity()-1);
				if(cep.getQuantity()==0)
					em.remove(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({"client","admin","account.manager"})
	@AuditEvent(AuditType.DeleteConnection)
	public void delete(Connector connector) {
		if(connector==null || connector.getUid()==null)
			throw new IllegalArgumentException("Cannot delete a non-existant connector.");
		
		if(connector.getType().equals(ConnectorType.Response))
		{
			ResponseConnector rConn = (ResponseConnector)connector;
			CampaignEntryPointDO cep = getSpecificEntryPoint(rConn.getEntryPoint(),rConn.getEntryPointType(),rConn.getKeyword());
			if(cep==null)
			{
				log.warn("entry point modified without an appropriate CampaignEntryPointDO entry already existing.");
			} else {
				if(!cep.getCampaign().getUID().equals(connector.getCampaignUID()))
					throw new IllegalStateException("Trying to change the entry point belonging to a different campaign.");
				cep.setQuantity(cep.getQuantity()-1);
				if(cep.getQuantity()==0)
					em.remove(cep);
				else if(cep.getQuantity()<0)
					log.warn("more entry points removed than have been initially catalogued");
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
		
		String queryString="select csl.lastHitNode.UID, count(csl.lastHitNode.UID) from CampaignSubscriberLinkDO csl where csl.campaign.UID=:campaignUID group by csl.lastHitNode.UID";
		Query q = em.createQuery(queryString);
		q.setParameter("campaignUID", campaignUUID);
		List<Object[]> result = q.getResultList();
		Map<String,Long> ret = new HashMap<String,Long>();
		for(Object[] row : result)
			ret.put((String)row[0], (Long)row[1]);
		return ret;
	}
}