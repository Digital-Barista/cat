package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.FacebookApp;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.FacebookAppDO;
import com.digitalbarista.cat.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session Bean implementation class AccountManagerImpl
 */
@Component
@Transactional(propagation= Propagation.REQUIRED)
public class AccountManager{

	private Logger log = LogManager.getLogger(AccountManager.class);
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;

        @Autowired
        UserManager userManager;
        
        @Autowired
        SecurityUtil securityUtil;
	
	@SuppressWarnings("unchecked")
	public List<FacebookApp> getFacebookApps()
	{
		Criteria crit = null;
		List<FacebookApp> ret = new ArrayList<FacebookApp>();
		crit = session.createCriteria(FacebookAppDO.class);

		// Limit query by allowed clients if necessary
    	if(!securityUtil.isAdmin())
    	{
    		crit.add(Restrictions.in("client.primaryKey", securityUtil.extractClientIds(session)));
			if(securityUtil.extractClientIds(session).size() == 0)
				return ret;
    	}
    	
    	// Convert data objects to business objects
		for(FacebookAppDO appDO : (List<FacebookAppDO>)crit.list())
		{
			FacebookApp f = new FacebookApp();
			f.copyFrom(appDO);
			ret.add(f);
		}
		return ret;
	}
	
	
	@AuditEvent(AuditType.SaveFacebookApp)
	public FacebookApp save(FacebookApp app) 
	{
		if(app == null)
			throw new IllegalArgumentException("Cannot save a null facebook app.");

		FacebookAppDO appDO = null;
		if(app.getAppName()!=null)
			appDO = em.find(FacebookAppDO.class, app.getAppName());
		
		if(appDO == null)
			appDO = new FacebookAppDO();
		
		// Copy all properties to DO
		app.copyTo(appDO);
		appDO.setClient(em.find(ClientDO.class, app.getClientId()));

		em.persist(appDO);
		em.flush();
		FacebookApp ret = new FacebookApp();
		ret.copyFrom(appDO);
		return ret;
	}

	public void delete(FacebookApp app) {
		if(app == null)
			throw new IllegalArgumentException("Cannot delete a null facebook app.");

		FacebookAppDO appDO = null;
		if(app.getAppName()!=null)
			appDO = em.find(FacebookAppDO.class, app.getAppName());

		if (appDO == null)
			throw new IllegalArgumentException("Cannot find tag to delete with contactTagId: " + app.getAppName());
		
		em.remove(appDO);
	}
}
