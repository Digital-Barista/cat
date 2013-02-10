package com.digitalbarista.cat.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.UserDO;
import com.digitalbarista.cat.ejb.session.CacheAccessManager;
import com.digitalbarista.cat.ejb.session.CacheAccessManager.CacheName;
import com.digitalbarista.cat.ejb.session.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
  
  @Autowired
  private ApplicationContext ctx;
  
  private UserManager userManager;

  private CacheAccessManager cache;
  
  private CacheAccessManager getCache()
  {
    if(cache==null)
      cache=ctx.getBean(CacheAccessManager.class);
    return cache;
  }
  
  private UserManager getUserManager()
  {
    if(userManager==null)
      userManager=ctx.getBean(UserManager.class);
    return userManager;
  }
  
	public boolean isAdmin(SecurityContext ctx)
	{
		for(GrantedAuthority auth : ctx.getAuthentication().getAuthorities())
    {
        if("ROLE_admin".equals(auth.getAuthority()))
            return true;
    }
		return false;
	}
	
	public boolean isAdmin()
	{
            return isAdmin(SecurityContextHolder.getContext());
	}
	
  public Set<Long> extractClientIds(Session session)
  {
      return extractClientIds(SecurityContextHolder.getContext(),session);
  }
        
	public Set<Long> extractClientIds(SecurityContext ctx, Session session)
	{
		return extractClientIds(ctx, session, null);
	}
	
	public Set<Long> extractClientIds(Session session, String user) 
	{
            return extractClientIds(SecurityContextHolder.getContext(),session,user);
        }
        
        public Set<Long> extractClientIds(SecurityContext ctx, Session session, String user) 
	{
		String username = user;
		if (username == null)
		{
			username = ""+ctx.getAuthentication().getPrincipal();
		}
		
		Set<Long> clientIDs;
		
		clientIDs = (Set<Long>)getCache().getCachedObject(CacheName.PermissionCache, username);
		if(clientIDs!=null)
			return clientIDs;

		clientIDs = new HashSet<Long>();
		
		// If the user is an admin return all "active" client IDs
		
		if(isAdmin(ctx))
		{
			Criteria crit = session.createCriteria(ClientDO.class);
			crit.add(Restrictions.eq("active", true));
			crit.setProjection(Projections.id());
			clientIDs = new HashSet<Long>(crit.list());
		}
		else
		{
			UserDO userDO = getUserManager().getSimpleUserByUsername(username);
			if(userDO==null)
				return clientIDs;
			for(RoleDO role : userDO.getRoles())
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
		if(!getCache().cacheContainsKey(CacheName.PermissionCache, username))
			getCache().cacheObject(CacheName.PermissionCache, username, clientIDs);
		return clientIDs;
	}
	
        public List<Long> getAllowedClientIDs(Session session, List<Long> clientIDs)
        {
            return getAllowedClientIDs(SecurityContextHolder.getContext(),session,clientIDs);
        }
        
	public List<Long> getAllowedClientIDs(SecurityContext ctx, Session session, List<Long> clientIDs)
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
        
        public String getPrincipalName()
        {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }

        public boolean isCallerInRole(String role)
        {
            for(GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
            {
                if(role.equals(ga.getAuthority()) || role.equals("ROLE_"+ga.getAuthority()))
                {
                    return true;
                }
            }
            return false;
        }
}
