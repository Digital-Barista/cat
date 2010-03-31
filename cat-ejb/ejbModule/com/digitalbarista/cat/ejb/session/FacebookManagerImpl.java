package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.data.FacebookMessageDO;


/**
 * Session Bean implementation class FacebookManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/FacebookManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class FacebookManagerImpl implements FacebookManager {

	public static final String CONTINUED_INDICATOR = "\n(...)";
	
	@Resource
	private SessionContext ctx; 

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;


	@SuppressWarnings("unchecked")
	@Override
	@PermitAll
	public List<FacebookMessage> getMessages(String facebookAppId, String uid) {

		List<FacebookMessage> ret = new ArrayList<FacebookMessage>();
		
		Criteria crit = session.createCriteria(FacebookMessageDO.class);
		crit.add(Restrictions.eq("facebookUID", uid));
		crit.add(Restrictions.eq("facebookAppId", facebookAppId));
		crit.addOrder(Order.desc("createDate"));
		
		for (FacebookMessageDO messageDO : (List<FacebookMessageDO>)crit.list())
		{
			FacebookMessage message = new FacebookMessage();
			message.copyFrom(messageDO);
			ret.add(message);
		}
		
		return ret;
	}


	@Override
	@PermitAll
	public void delete(Integer facebookMessageId) 
	{
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			em.remove(message);
		}
	}


	@Override
	public FacebookMessage respond(Integer facebookMessageId, String response) {

		FacebookMessage ret = null;
		
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			// If the response is valid update the message
			if (message.getMetadata().indexOf(response) > -1)
				message.setResponse(response);
			
			ret = new FacebookMessage();
			ret.copyFrom(message);
		}
		return ret;
	}



}
