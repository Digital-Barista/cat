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
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.RunAsPrincipal;

import com.digitalbarista.cat.business.Role;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.UserDO;

/**
 * Session Bean implementation class UserManagerImpl
 */
@Stateless
@LocalBinding(jndiBinding = "ejb/cat/UserManager")
@RunAsPrincipal("admin")
@RunAs("admin")
public class UserManagerImpl implements UserManager {

	@Resource
	private SessionContext ctx; //Used to flag rollbacks.
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
    /**
     * Default constructor. 
     */
    public UserManagerImpl() {
        // TODO Auto-generated constructor stub
    }

	@RolesAllowed({"admin","account.manager"})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
			Query q = em.createNamedQuery("user.by.username");
			q.setParameter("username", username);
			UserDO ret = (UserDO)q.getSingleResult();
			
			if(ret==null)
				return null;
			
			if(ctx.isCallerInRole("admin"))
				return ret;
			
			if(ctx.getCallerPrincipal().getName().equals(ret.getUsername()))
				return ret;
			
			Set<Long> clientIds = this.extractClientIds(ctx.getCallerPrincipal().getName());
			
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

		Set<Long> clientIds = this.extractClientIds(ctx.getCallerPrincipal().getName());
		
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeRoles(Long userPK, Set<Role> rolesToRemove) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot remove a role from a null user.");
		
		if(rolesToRemove==null || rolesToRemove.size()==0)
			return;
		
		for(Role role : rolesToRemove)
			removeRole(userPK,role);
	}

	@RolesAllowed({"admin","account.manager"})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public User save(User user) {
		UserDO current = getSimpleUserByPK(user.getPrimaryKey());
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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

	@Override
	public List<User> getAllVisibleUsers() {
		Query q = em.createQuery("select u from UserDO u");
		List<User> ret = new ArrayList<User>();
		Set<Long> clientIDs = extractClientIds(ctx.getCallerPrincipal().getName());
		Set<Long> userClientIDs;
		User u;
		for(UserDO user : (List<UserDO>)q.getResultList())
		{
			//admins can see ALL users
			if(!ctx.isCallerInRole("admin"))
			{
				//Get the clients this user belongs to
				userClientIDs = extractClientIds(user.getUsername());
				
				//ONLY keep the clientIDs our caller can see.
				userClientIDs.retainAll(clientIDs);
				
				//If there's nothing left, they can't see it.
				if(userClientIDs.size()==0)
					continue;
			}
			
			u = new User();
			u.copyFrom(user);
			ret.add(u);
		}
		return ret;
	}
	
	@RolesAllowed({"admin","account.manager"})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(User user)
	{
		em.remove(getSimpleUserByUsername(user.getUsername()));
	}

	@Override
	public Set<Long> extractClientIds(String username) {
		Set<Long> clientIDs = new HashSet<Long>();
		for(RoleDO role : getSimpleUserByUsername(ctx.getCallerPrincipal().getName()).getRoles())
			if(role.getRoleName().equals("account.manager") || role.getRoleName().equals("client"))
				clientIDs.add(role.getRefId());
		return clientIDs;
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
}	
