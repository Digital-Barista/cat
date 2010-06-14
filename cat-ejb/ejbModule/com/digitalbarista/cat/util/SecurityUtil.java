package com.digitalbarista.cat.util;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.SessionContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.ejb.session.UserManager;

public class SecurityUtil {

	public static Set<Long> extractClientIds(SessionContext ctx, UserManager uMan, Session session, String username) {
		Set<Long> clientIDs = new HashSet<Long>();
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
		return clientIDs;
	}

}
