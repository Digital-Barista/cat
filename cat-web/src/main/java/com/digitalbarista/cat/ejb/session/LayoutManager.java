package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.business.LayoutInfo;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.LayoutInfoDO;
import com.digitalbarista.cat.util.SecurityUtil;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("LayoutManager")
@Lazy
@Transactional(propagation=Propagation.REQUIRED)
public class LayoutManager {
  
  @Autowired
  private ApplicationContext ctx;

  @Autowired
  UserManager userManager;

  @Autowired
  SessionFactory sf;

  @Autowired
  SecurityUtil securityUtil;
        
	public LayoutInfoDO getSimpleLayoutInfo(String uuid, Integer version)
	{
    Criteria crit = sf.getCurrentSession().createCriteria(LayoutInfoDO.class);
    crit.add(Restrictions.eq("UID", uuid));
    crit.add(Restrictions.eq("version", version));
    LayoutInfoDO ret = (LayoutInfoDO)crit.uniqueResult();

    if(ret==null)
      return null;

    if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), ret.getCampaign().getClient().getPrimaryKey()))
      throw new SecurityException("Current user is not allowed to view layout infor for this campaign.");

    return ret;
	}
	
	public List<LayoutInfo> getLayoutInfo(List<String> uidList) {
		List<LayoutInfo> ret = new ArrayList<LayoutInfo>();

		if(uidList==null)
			return new ArrayList<LayoutInfo>();
		
		Criteria crit = sf.getCurrentSession().createCriteria(LayoutInfoDO.class);
		crit.add(Restrictions.in("UID", uidList));
		if(!securityUtil.isAdmin())
		{
			crit.createAlias("campaign", "campaign");
			crit.add(Restrictions.in("campaign.client.id", securityUtil.extractClientIds(sf.getCurrentSession())));
			if(securityUtil.extractClientIds(sf.getCurrentSession()).size()==0)
				return ret;
		}

		LayoutInfo info;
		for(LayoutInfoDO li : (List<LayoutInfoDO>)crit.list())
		{
			info = new LayoutInfo();
			info.copyFrom(li);
			ret.add(info);
		}
		return ret;
	}

	public LayoutInfo getLayoutInfo(String uid, Integer version) {
		if(uid==null)
			return null;
		
		LayoutInfo ret = new LayoutInfo();
		ret.copyFrom(getSimpleLayoutInfo(uid, version));
		return ret;
	}

	public void save(LayoutInfo layout) {
    CampaignManager campaignManager = ctx.getBean(CampaignManager.class);
		if(layout==null)
			throw new IllegalArgumentException("Cannot save a null layout.");

		if(layout.getUUID()==null)
			throw new IllegalArgumentException("No UUID assigned to this layout.");

		//Grab the live info object.
		LayoutInfoDO info = getSimpleLayoutInfo(layout.getUUID(), layout.getVersion());
		
		//Gotta ACTUALLY set a campaign if this is a new object.
		if(info==null && layout.getCampaignUUID()==null)
			throw new IllegalArgumentException("No CampaignDO assigned to this layout.");

		
		//Find the persistent campaign for the one that got passed in.
		CampaignDO c=null;
		if(layout.getCampaignUUID()!=null)
			c=campaignManager.getSimpleCampaign(layout.getCampaignUUID());

		if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), c.getClient().getPrimaryKey()))
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
		sf.getCurrentSession().persist(info);
	}

  public List<LayoutInfo> getLayoutsByCampaign(String uid) {
		return getLayoutsByCampaignAndVersion(uid,null);
	}
	
	public List<LayoutInfo> getLayoutsByCampaignAndVersion(String uid, Integer version) {
    CampaignManager campaignManager = ctx.getBean(CampaignManager.class);
		CampaignDO c = campaignManager.getSimpleCampaign(uid);
		
		if(!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), c.getClient().getPrimaryKey()))
			throw new SecurityException("Current user is not allowed to view layout info for the specified campaign.");

		if(version==null)
			version=c.getCurrentVersion();
	
		if(c==null)
			return new ArrayList<LayoutInfo>();
		
		List<LayoutInfo> ret = new ArrayList<LayoutInfo>();
		LayoutInfo temp;
		
		String query = "select li from LayoutInfoDO li where li.campaign.id=:pk and li.version=:version";
		Query q = sf.getCurrentSession().createQuery(query);
		q.setParameter("pk", c.getPrimaryKey());
		q.setParameter("version", version);
		for(LayoutInfoDO info : (List<LayoutInfoDO>)q.list())
		{
			temp=new LayoutInfo();
			temp.copyFrom(info);
			ret.add(temp);
		}
		return ret;
	}

  public void delete(String uid, Integer version) {
		if(uid==null)
			throw new IllegalArgumentException("Cannot delete an unspecified layout.");
		
		LayoutInfoDO li = getSimpleLayoutInfo(uid, version);
		if(li==null)
			return;
		
		sf.getCurrentSession().delete(li);
	}

}
