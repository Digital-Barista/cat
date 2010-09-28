package com.digitalbarista.cat.ejb.session;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.Role;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.UserDO;
import com.digitalbarista.cat.util.SecurityUtil;

/**
 * Session Bean implementation class UserManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/UserManager")
@RunAsPrincipal("admin")
@RunAs("admin")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserManagerImpl implements UserManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@PersistenceContext(unitName="cat-data")
	private Session session;
	
	@EJB(name="ejb/cat/UserManager")
	UserManager userManager;
	
    /**
     * Default constructor. 
     */
    public UserManagerImpl() {
        // TODO Auto-generated constructor stub
    }

	@RolesAllowed({"admin","account.manager"})
	public void addRole(Long userPK, Role roleToAdd) {
		UserDO user = getSimpleUserByPK(userPK);
		
		if(user==null)
			throw new IllegalArgumentException("Cannot add a role to a null user.");
		
		if(roleToAdd==null)
			return;

		if(roleToAdd.getRoleName().equals("admin") && !ctx.isCallerInRole("admin"))
			throw new SecurityException("Current user is not allowed to add the admin role.");
		
		if(roleToAdd.getRoleName().equals("account.manager") || roleToAdd.getRoleName().equals("client"))
			if(!isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), roleToAdd.getRefId()))
				throw new SecurityException("Current user is not allowed to assign priveleges for the specified client id.");
		
		user.getRoles().add(roleToAdd.buildDataRole());
	}

	@RolesAllowed({"admin","account.manager"})
	public void addRoles(Long userPK, Set<Role> rolesToAdd) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot add a role to a null user.");
		
		if(rolesToAdd==null || rolesToAdd.size()==0)
			return;
		
		for(Role role : rolesToAdd)
			addRole(userPK,role);
	}

	@RolesAllowed({"admin","account.manager"})
	protected User createUser(User newUser) {
		UserDO user = new UserDO();
		newUser.copyTo(user);
		RoleDO tempRole;
		for(Role roleToAdd : newUser.getRoles())
		{
			if(roleToAdd.getRoleName().equals("admin") && !ctx.isCallerInRole("admin"))
				throw new SecurityException("Current user is not allowed to add the admin role.");
			
			if(roleToAdd.getRoleName().equals("account.manager") || roleToAdd.getRoleName().equals("client"))
				if(!isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), roleToAdd.getRefId()))
					throw new SecurityException("Current user is not allowed to assign priveleges for the specified client id.");
			
			tempRole = roleToAdd.buildDataRole();
			tempRole.setUser(user);
			user.getRoles().add(tempRole);
		}
		em.persist(user);
		em.flush();
		
		User ret = new User();
		ret.copyFrom(user);
		return ret;
	}

	@RolesAllowed({"admin","account.manager"})
	public User getUserByPK(long pk) {
		User ret=new User();
		ret.copyFrom(getSimpleUserByPK(pk));
		return ret;
	}

	@PermitAll
	public UserDO getSimpleUserByUsername(String username)
	{
		try
		{
			Criteria crit = session.createCriteria(UserDO.class);
			crit.add(Restrictions.eq("username", username));
			crit.setCacheable(true);
			UserDO ret = (UserDO)crit.uniqueResult();
			
			if(ret==null)
				return null;
			
			if(ctx.isCallerInRole("admin"))
				return ret;
			
			if(ctx.getCallerPrincipal().getName().equals(ret.getUsername()))
				return ret;
			
			Set<Long> clientIds = SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName());
			
			for(RoleDO role : ret.getRoles())
			{
				if(role.getRoleName().equals("account.manager"))
					if(clientIds.contains(role.getRefId()))
						return ret;
			}
			
			throw new SecurityException("Current user is not allowed to view the specified user.");
		}
		catch(NoResultException e)
		{
			return null;
		}
	}
	
	@PermitAll
	public UserDO getSimpleUserByPK(Long pk)
	{
		UserDO ret =  em.find(UserDO.class, pk);

		if(ret==null)
			return null;
		
		if(ctx.isCallerInRole("admin"))
			return ret;
		
		if(ctx.getCallerPrincipal().getName().equals(ret.getUsername()))
			return ret;

		Set<Long> clientIds = SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName());
		
		for(RoleDO role : ret.getRoles())
		{
			if(role.getRoleName().equals("account.manager"))
				if(clientIds.contains(role.getRefId()))
					return ret;
		}
		
		throw new SecurityException("Current user is not allowed to view the specified user.");
	}
	
	@RolesAllowed({"admin","account.manager"})
	public User getUserByUsername(String username) {
		User ret = new User();
		ret.copyFrom(getSimpleUserByUsername(username));
		return ret;
	}

	@PermitAll
	public User getCurrentUser() {
		Principal caller = ctx.getCallerPrincipal();
		User ret = new User();
		ret.copyFrom(getSimpleUserByUsername(caller.getName()));
		return ret;
	}

	@RolesAllowed({"admin","account.manager"})
	public void removeRole(Long userPK, Role roleToRemove) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot remove a role from a null user.");
		
		if(roleToRemove==null)
			return;
				
		if(roleToRemove.getRoleName().equals("admin") && !ctx.isCallerInRole("admin"))
			throw new SecurityException("Current user is not allowed to remove the admin role.");
		
		if(roleToRemove.getRoleName().equals("account.manager") || roleToRemove.getRoleName().equals("client"))
			if(!isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), roleToRemove.getRefId()))
				throw new SecurityException("Current user is not allowed to remove priveleges for the specified client id.");

		user.getRoles().remove(roleToRemove.buildDataRole());
	}

	@RolesAllowed({"admin","account.manager"})
	public void removeRoles(Long userPK, Set<Role> rolesToRemove) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot remove a role from a null user.");
		
		if(rolesToRemove==null || rolesToRemove.size()==0)
			return;
		
		for(Role role : rolesToRemove)
			removeRole(userPK,role);
	}

	@PermitAll
	public User save(User user) {
		UserDO current = getSimpleUserByPK(user.getPrimaryKey());
		
		//If this is you, or you're an admin . . . have at it!
		if(!ctx.isCallerInRole("admin") && !ctx.getCallerPrincipal().getName().equals(user.getUsername()))
		{
			//Other than that . . . only account managers allowed
			if(!ctx.isCallerInRole("account.manager"))
				throw new SecurityException("You do not have permission to edit / create this user.");

			//Account managers can create new users, but if it's a current one
			if(current!=null)
			{
				//They're NOT allowed to change admin user info!
				for(RoleDO role : current.getRoles())
				{
					if(role.getRoleName().equals("admin"))
						throw new SecurityException("You do not have permission to edit / create this user.");
				}
				
				//And ALL of their allowed IDs
				Set<Long> allowedIDs = SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName());
				//Have to match the client user's IDs
				Set<Long> neededIDs = SecurityUtil.extractClientIds(ctx,userManager,session,user.getUsername());
				//Otherwise, they're booted!
				if(!allowedIDs.containsAll(neededIDs))
				{
					throw new SecurityException("You do not have permission to edit / create this user.");
				}
			}
		}
		
		User ret = null;
		
		if(current==null)
		{
			ret = createUser(user);
		} else {
			user.copyTo(current);
			if(user.getRoles()!=null)
				syncRoles(user.getPrimaryKey(),new HashSet<Role>(user.getRoles()));
			ret = new User();
			ret.copyFrom(current);
		}
		return ret;
	}

	@RolesAllowed({"admin","account.manager"})
	public void syncRoles(Long userPK, Set<Role> roles) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot add/remove a role for a null user.");
		
		if(roles==null || roles.size()==0)
			user.getRoles().clear();
		else
		{
			Set<RoleDO> convertedRoles = new HashSet<RoleDO>();
			RoleDO tempRole;
			for(Role role : roles)
			{
				tempRole = role.buildDataRole();
				tempRole.setUser(user);
				convertedRoles.add(tempRole);
			}

			for(RoleDO role : convertedRoles)
			{
				if(user.getRoles().contains(role))
					continue;
				
				if(role.getRoleName().equals("admin") && !ctx.isCallerInRole("admin"))
					throw new SecurityException("Current user may not add the admin role.");
				
				if(role.getRoleName().equals("client") || role.getRoleName().equals("account.manager"))
					if(!isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), role.getRefId()))
						throw new SecurityException("Current user may not add the specified client-specific role.");
			}
			
			for(RoleDO role : user.getRoles())
			{
				if(user.getRoles().contains(role))
					continue;
				
				if(role.getRoleName().equals("admin") && !ctx.isCallerInRole("admin"))
					throw new SecurityException("Current user may not remove the admin role.");
				
				if(role.getRoleName().equals("client") || role.getRoleName().equals("account.manager"))
					if(!isUserAllowedForClientId(ctx.getCallerPrincipal().getName(), role.getRefId()))
						throw new SecurityException("Current user may not remove the specified client-specific role.");
			}
			
			user.getRoles().retainAll(convertedRoles);
			user.getRoles().addAll(convertedRoles);
		}
	}

	@RolesAllowed({"admin","account.manager"})
	public boolean allowUsername(String username) {
		return getSimpleUserByUsername(username)!=null;
	}

	@RolesAllowed({"admin","account.manager"})
	public Set<String> usernamesAllowed(Set<String> nameList) {
		String query = "select u.username" +
				" from UserDO u" +
				" where u.username in (:nameList)";
		Query q = em.createQuery(query);
		q.setParameter("nameList", nameList);
		return new HashSet<String>(q.getResultList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getAllVisibleUsers() {
		Criteria crit = null;
		List<User> ret = new ArrayList<User>();
		if(ctx.isCallerInRole("admin"))
		{
			crit = session.createCriteria(UserDO.class);
			crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		} else {
			Set<Long> clientIDs = SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName());
			if(SecurityUtil.extractClientIds(ctx,userManager,session,ctx.getCallerPrincipal().getName()).size()==0)
				return ret;
			crit = session.createCriteria(RoleDO.class);
			crit.add(Restrictions.in("roleName",new String[]{"account.admin","client"}));
			crit.add(Restrictions.in("refId", clientIDs));
			crit.createAlias("user", "user");
			crit.setProjection(Projections.distinct(Projections.groupProperty("user")));
		}

		User u;
		List<UserDO> list = (List<UserDO>)crit.list();
		for(UserDO user : list)
		{
			u = new User();
			u.copyFrom(user);
			ret.add(u);
		}
		return ret;
	}
	
	@RolesAllowed({"admin","account.manager"})
	public void delete(User user)
	{
		em.remove(getSimpleUserByUsername(user.getUsername()));
	}

	@Override
	public boolean isUserAllowedForClientId(String username, Long clientId) {
		UserDO user = getSimpleUserByUsername(username);
		for(RoleDO role : user.getRoles())
		{
			if("admin".equals(role.getRoleName()))
				return true;
			if(clientId.equals(role.getRefId()))
			{
				if("client".equals(role.getRoleName()) ||
				   "account.manager".equals(role.getRoleName()))
					return true;
			}
		}
		return false;
	}

	private boolean isAdmin(String username)
	{
		for(RoleDO role : getSimpleUserByUsername(username).getRoles())
		{
			if(role.getRoleName().equals("admin"))
					return true;
		}
		return false;
	}
}	
