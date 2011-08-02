package com.digitalbarista.cat.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.ejb.session.CacheAccessManager;
import com.digitalbarista.cat.ejb.session.CacheAccessManager.CacheName;
import com.digitalbarista.cat.ejb.session.UserManager;

public class SecurityUtil {

	public static boolean isAdmin(SessionContext ctx)
	{
		if(ctx.isCallerInRole("admin"))
		{
			return true;
		}
		return false;
	}
	
	public static Set<Long> extractClientIds(SessionContext ctx, Session session)
	{
		return extractClientIds(ctx, session, null);
	}
	
	public static Set<Long> extractClientIds(SessionContext ctx, Session session, String user) 
	{
		UserManager uMan = (UserManager)ctx.lookup("ejb/cat/UserManager");
		String username = user;
		if (username == null)
		{
			username = ctx.getCallerPrincipal().getName();
		}
		
		Set<Long> clientIDs;
		
		CacheAccessManager cache = (CacheAccessManager)ctx.lookup("ejb/cat/CacheAccessManager");
		clientIDs = (Set<Long>)cache.getCachedObject(CacheName.PermissionCache, username);
		if(clientIDs!=null)
			return clientIDs;

		clientIDs = new HashSet<Long>();
		if("guest".equalsIgnoreCase(username))
			return clientIDs;
		
		// If the user is an admin return all "active" client IDs
		
		if(ctx.isCallerInRole("admin"))
		{
			Criteria crit = session.createCriteria(ClientDO.class);
			crit.add(Restrictions.eq("active", true));
			crit.setProjection(Projections.id());
			clientIDs = new HashSet<Long>(crit.list());
		}
		else
		{
			for(RoleDO role : uMan.getSimpleUserByUsername(username).getRoles())
				if(role.getRoleName().equals("account.manager") || role.getRoleName().equals("client"))
					clientIDs.add(role.getRefId());
			if(clientIDs.size()==0)
				return clientIDs;
			Criteria crit = session.createCriteria(ClientDO.class);
			crit.add(Restrictions.eq("active", true));
			crit.add(Restrictions.in("id", clientIDs));
			crit.setProjection(Projections.id());
			clientIDs = new HashSet<Long>(crit.list());
		}
		if(!cache.cacheContainsKey(CacheName.PermissionCache, username))
			cache.cacheObject(CacheName.PermissionCache, username, clientIDs);
		return clientIDs;
	}
	
	public static List<Long> getAllowedClientIDs(SessionContext ctx, Session session, List<Long> clientIDs)
	{
		// Get client count
		Set<Long> allowedClientIDs = extractClientIds(ctx, session);
		
		// Restrict to given client IDs if given
		List<Long> filterClientIDs = new ArrayList<Long>();
		if (clientIDs == null)
		{
			filterClientIDs.addAll(allowedClientIDs);
		}
		else
		{
			for (Long allowedClientId : allowedClientIDs)
			{
				for (int i = 0; i < clientIDs.size(); i++)
				{
					// Stupid check because Blaze thinks ArrayCollection<Integer> fits 
					// the List<Long> interface
					Object number = clientIDs.get(i);
					Long value;
					if (number instanceof Integer)
						value = ((Integer)number).longValue();
					else
						value = ((Long)number).longValue();
					
					// Add allowed client IDs
					if (allowedClientId.equals(value) )
					{
						filterClientIDs.add(allowedClientId);
						break;
					}
				}
			}
		}
		return filterClientIDs;
	}

}
