package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditInterceptor;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.business.Keyword;
import com.digitalbarista.cat.data.CampaignEntryPointDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.KeywordDO;

/**
 * Session Bean implementation class ClientDO
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/ClientManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@Interceptors(AuditInterceptor.class)
public class ClientManagerImpl implements ClientManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;

	@PersistenceContext(unitName="cat-data")
	private Session session;

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
    @SuppressWarnings("unchecked")
    @PermitAll
    public List<Client> getVisibleClients() {
    	List<Client> ret = new ArrayList<Client>();
    	Criteria crit = session.createCriteria(ClientDO.class);
    	if(!ctx.isCallerInRole("admin"))
    		crit.add(Restrictions.in("id", userManager.extractClientIds(ctx.getCallerPrincipal().getName())));
    	for(ClientDO client : (List<ClientDO>)crit.list())
    	{
        	Client c = new Client();
    		c.copyFrom(client);
    		ret.add(c);
    	}
    	for(Client c : ret)
    		fillInEntryPointKeywordData(c);
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	@RolesAllowed("admin")
    public List<EntryPointDefinition> getEntryPointDefinitions() {
    	List<EntryPointDefinition> ret = new ArrayList<EntryPointDefinition>();
    	Query q = em.createQuery("select e from EntryPointDO e");
    	for(EntryPointDO entry : (List<EntryPointDO>)q.getResultList())
    	{
        	EntryPointDefinition epd = new EntryPointDefinition();
    		epd.copyFrom(entry);
    		ret.add(epd);
    	}
    	for(EntryPointDefinition epd : ret)
    		fillInEntryPointKeywordData(epd);

    	return ret;
    }
    
    @PermitAll
    public Client getClientById(long id) {
    	if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), id))
    		throw new SecurityException("Current user is not allowed to view specified client.");
    	
        Client c = new Client();
        c.copyFrom(em.find(ClientDO.class, id));
        fillInEntryPointKeywordData(c);
        return c;
    }

    private void fillInEntryPointKeywordData(Client c)
    {
    	for(EntryPointDefinition epd : c.getEntryPoints())
    		fillInEntryPointKeywordData(epd);
    }
    
    private void fillInEntryPointKeywordData(EntryPointDefinition epd)
    {
		Query q=em.createQuery("select cep from CampaignEntryPointDO cep where cep.entryPoint=:epName and cep.type=:epType");
		q.setParameter("epName", epd.getValue());
		q.setParameter("epType", epd.getType());
		List<CampaignEntryPointDO> result = q.getResultList();
		Map<String,String> keywords = new HashMap<String,String>();
		for(CampaignEntryPointDO row : result)
			keywords.put(row.getKeyword(),row.getCampaign().getUID());
		for(Keyword kywd : epd.getKeywords())
			if(keywords.containsKey(kywd.getKeyword()))
				kywd.setCampaignUID(keywords.get(kywd.getKeyword()));
    }
    
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("admin")
	@AuditEvent(AuditType.SaveClient)
	public Client save(Client client) {
		if(client==null)
			throw new IllegalArgumentException("Cannot save a null client.");
		ClientDO c = em.find(ClientDO.class, client.getClientId());
		if(c == null)
			c = new ClientDO();
		client.copyTo(c);
		EntryPointDO ep;
		if(client.getEntryPoints()!=null)
		{
			c.getEntryPoints().clear();
			for(EntryPointDefinition epd : client.getEntryPoints())
			{
				if(epd.getPrimaryKey()==null)
					throw new IllegalArgumentException("Cannot assign an EntryPointDefinition to a client that has no primary key.  Define the entry point first.");
				ep = em.find(EntryPointDO.class, epd.getPrimaryKey());
				if(ep == null)
					throw new IllegalArgumentException("Cannot assign an invalid EntryPointDefinition to a client.  pk='"+epd.getPrimaryKey()+"' not found.");
				c.getEntryPoints().add(ep);
			}
		}
		em.persist(c);
		em.flush();
		Client ret = new Client();
		ret.copyFrom(c);
		return ret;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("admin")
	public Keyword save(Keyword kwd) {
		if(kwd == null)
			throw new IllegalArgumentException("Cannot save a null keyword.");
		KeywordDO kwdData=null;
		if(kwd.getPrimaryKey()!=null)
			kwdData = em.find(KeywordDO.class, kwd.getPrimaryKey());
		if(kwdData==null)
			kwdData = new KeywordDO();
		kwd.copyTo(kwdData);
		kwdData.setClient(em.find(ClientDO.class, kwd.getClientId()));
		kwdData.setEntryPoint(em.find(EntryPointDO.class, kwd.getEntryPointId()));
		em.persist(kwdData);
		em.flush();
		Keyword ret = new Keyword();
		ret.copyFrom(kwdData);
		return ret;
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("admin")
	public EntryPointDefinition save(EntryPointDefinition epd) {
		if(epd == null)
			throw new IllegalArgumentException("Cannot save a null entry point definition.");
		EntryPointDO ep=null;
		if(epd.getPrimaryKey()!=null)
			ep = em.find(EntryPointDO.class, epd.getPrimaryKey());
		if(ep==null)
			ep = new EntryPointDO();
		epd.copyTo(ep);
		
		Set<ClientDO> newClientList = new HashSet<ClientDO>();
		if (epd.getClientIDs() != null)
		{
			for(Integer id : epd.getClientIDs())
				newClientList.add(em.find(ClientDO.class, id.longValue()));
		
			ep.getClients().retainAll(newClientList);
			ep.getClients().addAll(newClientList);
		}
		
		em.persist(ep);
		em.flush();
		EntryPointDefinition ret = new EntryPointDefinition();
		ret.copyFrom(ep);
		return ret;
	}

	@Override
	@RolesAllowed("admin")
	public List<Keyword> getAllKeywords() {
		Query q = em.createQuery("select k from KeywordDO k");
		List<Keyword> ret = new ArrayList<Keyword>();
		Keyword temp;
		for(KeywordDO keyword : (List<KeywordDO>)q.getResultList())
		{
			temp=new Keyword();
			temp.copyFrom(keyword);
			ret.add(temp);
		}
		return ret;
	}

	@Override
	public List<Keyword> getAllKeywordsForClient(Long clientID) {
		if(clientID==null)
			throw new IllegalArgumentException("Must provide a client ID to query on.");
		Query q = em.createQuery("select k from KeywordDO k where k.client.id=:id");
		q.setParameter("id", clientID);
		List<Keyword> ret = new ArrayList<Keyword>();
		Keyword temp;
		for(KeywordDO keyword : (List<KeywordDO>)q.getResultList())
		{
			temp=new Keyword();
			temp.copyFrom(keyword);
			ret.add(temp);
		}
		return ret;
	}

	@Override
	public List<Keyword> getAllKeywordsForClientAndEntryPoint(Long clientID,
			Long entryPointID) {
		if(clientID==null)
			throw new IllegalArgumentException("Must provide a client ID to query on.");
		if(entryPointID==null)
			throw new IllegalArgumentException("Must provide an Entry Point ID to query on.");
		
		Query q = em.createQuery("select k from KeywordDO k where k.client.id=:clientID and k.entryPoint.id=:entryID");
		q.setParameter("clientID", clientID);
		q.setParameter("entryID", entryPointID);
		List<Keyword> ret = new ArrayList<Keyword>();
		Keyword temp;
		for(KeywordDO keyword : (List<KeywordDO>)q.getResultList())
		{
			temp=new Keyword();
			temp.copyFrom(keyword);
			ret.add(temp);
		}
		return ret;
	}

	@Override
	public List<Keyword> getAllKeywordsForEntryPoint(Long entryPointID) {
		if(entryPointID==null)
			throw new IllegalArgumentException("Must provide an Entry Point ID to query on");
		Query q = em.createQuery("select k from KeywordDO k where k.entryPoint.id=:id");
		q.setParameter("id", entryPointID);
		List<Keyword> ret = new ArrayList<Keyword>();
		Keyword temp;
		for(KeywordDO keyword : (List<KeywordDO>)q.getResultList())
		{
			temp=new Keyword();
			temp.copyFrom(keyword);
			ret.add(temp);
		}
		return ret;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("admin")
	public void delete(Keyword kwd) {
		if(kwd == null)
			throw new IllegalArgumentException("Cannot save a null keyword.");
				
		KeywordDO kwdData=null;
		if(kwd.getPrimaryKey()!=null)
			kwdData = em.find(KeywordDO.class, kwd.getPrimaryKey());

		Query q = em.createNamedQuery("find.matching.entry.point");
		q.setParameter("entryPoint", kwdData.getEntryPoint().getValue());
		q.setParameter("keyword", kwdData.getKeyword());
		q.setParameter("type", kwdData.getEntryPoint().getType());
		try
		{
			CampaignEntryPointDO cep = (CampaignEntryPointDO)q.getSingleResult();
			throw new IllegalArgumentException("Cannot delete this keyword, as it is still in use in campaign UID='"+cep.getCampaign().getUID()+"'");
		}
		catch(NoResultException e)
		{}
		
		em.remove(kwdData);
	}
}