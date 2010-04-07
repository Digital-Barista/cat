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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.FacebookMessage;
import com.digitalbarista.cat.data.FacebookMessageDO;
import com.digitalbarista.cat.message.event.CATEvent;


/**
 * Session Bean implementation class FacebookManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/FacebookManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class FacebookManagerImpl implements FacebookManager {

	private Logger logger = LogManager.getLogger(getClass());
	
	@Resource
	private SessionContext ctx; 

	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	

	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;

	@EJB(name="ejb/cat/EventManager")
	EventManager eventManager;

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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(Integer facebookMessageId) 
	{
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			em.remove(message);
		}
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FacebookMessage respond(Integer facebookMessageId, String response) {

		FacebookMessage ret = null;
		
		FacebookMessageDO message = em.find(FacebookMessageDO.class, facebookMessageId);
		if (message != null)
		{
			// If the response is valid update the message
			if (message.getMetadata().indexOf(response) > -1)
				message.setResponse(response);
			
			eventManager.queueEvent(CATEvent.buildIncomingFacebookEvent(message.getFacebookUID(), message.getFacebookAppId(), response, message.getFacebookUID()));
			
			ret = new FacebookMessage();
			ret.copyFrom(message);
		}
		return ret;
	}


	@Override
	public String authorize(String authToken) {
		if (authToken.length() > 0)
		{
			checkValidSession(authToken);
			return "<message>token present</message>";
		}
		return "<message>no token</message>";
	}
	
	private boolean checkValidSession(String authToken)
	{
		String apiKey = "6c028b61ae2d51b43e8582420b8a75be";
		String secret = "8c7bd765ef219a7bea8132c03dbe2892";
		
		String params = "api_key=" + apiKey;
		params += "auth_token=" + authToken;
		params += "v=1.0";
		params += secret;
		
//		MessageDigest md = MessageDigest.getInstance("MD5");
//        md.update(params.getBytes());
//        byte keyB[] = md.digest();


		return false;
	}

}
