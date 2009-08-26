package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.LayoutInfo;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.LayoutInfoDO;

@Stateless
@LocalBinding(jndiBinding = "ejb/cat/LayoutManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class LayoutManagerImpl implements LayoutManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@EJB(name="ejb/cat/CampaignManager")
	CampaignManager campMan;
	
	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	@PermitAll
	public LayoutInfoDO getSimpleLayoutInfo(String uuid)
	{
		try
		{
			Criteria crit = session.createCriteria(LayoutInfoDO.class);
			crit.add(Restrictions.eq("UID", uuid));
			LayoutInfoDO ret = (LayoutInfoDO)crit.uniqueResult();
			
			if(ret==null)
				return null;
			
			if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), ret.getCampaign().getPrimaryKey()))
				throw new SecurityException("Current user is not allowed to view layout infor for this campaign.");
			
			return ret;
		}catch(NoResultException e)
		{
			return null;
		}
	}
	
	@Override
	@PermitAll
	public List<LayoutInfo> getLayoutInfo(List<String> uidList) {
		if(uidList==null)
			return new ArrayList<LayoutInfo>();
		
		Criteria crit = session.createCriteria(LayoutInfoDO.class);
		crit.add(Restrictions.in("UID", uidList));
		if(!ctx.isCallerInRole("admin"))
		{
			crit.createAlias("campaign", "campaign");
			crit.add(Restrictions.in("campaign.client.id", userManager.extractClientIds(ctx.getCallerPrincipal().getName())));
		}

		LayoutInfo info;
		List<LayoutInfo> ret = new ArrayList<LayoutInfo>();
		for(LayoutInfoDO li : (List<LayoutInfoDO>)crit.list())
		{
			info = new LayoutInfo();
			info.copyFrom(li);
			ret.add(info);
		}
		return ret;
	}

	@Override
	@PermitAll
	public LayoutInfo getLayoutInfo(String uid) {
		if(uid==null)
			return null;
		
		LayoutInfo ret = new LayoutInfo();
		ret.copyFrom(getSimpleLayoutInfo(uid));
		return ret;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PermitAll
	public void save(LayoutInfo layout) {
		if(layout==null)
			throw new IllegalArgumentException("Cannot save a null layout.");

		if(layout.getUUID()==null)
			throw new IllegalArgumentException("No UUID assigned to this layout.");

		//Grab the live info object.
		LayoutInfoDO info = getSimpleLayoutInfo(layout.getUUID());
		
		//Gotta ACTUALLY set a campaign if this is a new object.
		if(info==null && layout.getCampaignUUID()==null)
			throw new IllegalArgumentException("No CampaignDO assigned to this layout.");

		
		//Find the persistent campaign for the one that got passed in.
		CampaignDO c=null;
		if(layout.getCampaignUUID()!=null)
			c=campMan.getSimpleCampaign(layout.getCampaignUUID());

		if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), c.getClient().getPrimaryKey()))
			throw new SecurityException("Current user is not allowed to create or alter layout info entries for this campaign.");
		
		// If layout doesn't exist yet create a new one and assign the campaign
		if(info==null)
		{
			info = new LayoutInfoDO();
			info.setCampaign(c);
		}
		
		//If this isn't a new object, and there *IS* a campaign already,
		//  make sure it matches the one supplied.
		if(c!=null && !info.getCampaign().getUID().equals(c.getUID()))
			throw new IllegalArgumentException("CampaignDO assigned to this layout did not match previously saved value.");
		
		//If this is a new layout, set the campaign
		if(c!=null)
			info.setCampaign(c);
		
		layout.copyTo(info);
		info.setVersion(info.getCampaign().getCurrentVersion());
		em.persist(info);
	}

	@Override
	@PermitAll
	public List<LayoutInfo> getLayoutsByCampaign(String uid) {
		return getLayoutsByCampaignAndVersion(uid,null);
	}
	
	@Override
	@PermitAll
	public List<LayoutInfo> getLayoutsByCampaignAndVersion(String uid,Integer version) {
		CampaignDO c = campMan.getSimpleCampaign(uid);
		
		if(!userManager.isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), c.getClient().getPrimaryKey()))
			throw new SecurityException("Current user is not allowed to view layout info for the specified campaign.");

		if(version==null)
			version=c.getCurrentVersion();
	
		if(c==null)
			return new ArrayList<LayoutInfo>();
		
		List<LayoutInfo> ret = new ArrayList<LayoutInfo>();
		LayoutInfo temp;
		
		String query = "select li from LayoutInfoDO li where li.campaign.id=:pk and li.version=:version";
		Query q = em.createQuery(query);
		q.setParameter("pk", c.getPrimaryKey());
		q.setParameter("version", version);
		for(LayoutInfoDO info : (List<LayoutInfoDO>)q.getResultList())
		{
			temp=new LayoutInfo();
			temp.copyFrom(info);
			ret.add(temp);
		}
		return ret;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PermitAll
	public void delete(String uid) {
		if(uid==null)
			throw new IllegalArgumentException("Cannot delete an unspecified layout.");
		
		LayoutInfoDO li = getSimpleLayoutInfo(uid);
		if(li==null)
			return;
		
		em.remove(li);
	}

}