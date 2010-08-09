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
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.business.FacebookApp;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.FacebookAppDO;
import com.digitalbarista.cat.util.SecurityUtil;

/**
 * Session Bean implementation class AccountManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/AccountManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class AccountManagerImpl implements AccountManager {

	private Logger log = LogManager.getLogger(AccountManagerImpl.class);
	
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
	public List<FacebookApp> getFacebookApps()
	{
		Criteria crit = null;
		List<FacebookApp> ret = new ArrayList<FacebookApp>();
		crit = session.createCriteria(FacebookAppDO.class);

		// Limit query by allowed clients if necessary
    	if(!ctx.isCallerInRole("admin"))
    	{
    		crit.add(Restrictions.in("client.primaryKey", SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName())));
			if(SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName()).size()==0)
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
	
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@PermitAll
	@AuditEvent(AuditType.SaveFacebookApp)
	public FacebookApp save(FacebookApp app) 
	{
		if(app == null)
			throw new IllegalArgumentException("Cannot save a null facebook app.");

		FacebookAppDO appDO = null;
		if(app.getFacebookAppId()!=null)
			appDO = em.find(FacebookAppDO.class, app.getFacebookAppId());
		
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

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(FacebookApp app) {
		if(app == null)
			throw new IllegalArgumentException("Cannot delete a null facebook app.");

		FacebookAppDO appDO = null;
		if(app.getFacebookAppId()!=null)
			appDO = em.find(FacebookAppDO.class, app.getFacebookAppId());

		if (appDO == null)
			throw new IllegalArgumentException("Cannot find tag to delete with contactTagId: " + app.getFacebookAppId());
		
		em.remove(appDO);
	}
}
