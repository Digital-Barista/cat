package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.business.Role;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.UserDO;
import com.digitalbarista.cat.util.SecurityUtil;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Session Bean implementation class UserManagerImpl
 */
@Controller("UserManager")
@Lazy
@Transactional(propagation=Propagation.REQUIRED)
@RequestMapping(value={"/rest/users","/rs/users"})
public class UserManager {
	
    @Autowired
    private CacheAccessManager cache;

    @Autowired
    private SessionFactory sf;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    /**
     * Default constructor. 
     */
    public UserManager() {
        // TODO Auto-generated constructor stub
    }

  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.POST,value="/{id}/role")
	public void addRole(@PathVariable("id") Long userPK, @RequestBody Role roleToAdd) {
		UserDO user = getSimpleUserByPK(userPK);
		
		if(user==null)
			throw new IllegalArgumentException("Cannot add a role to a null user.");
		
		if(roleToAdd==null)
			return;

		if(roleToAdd.getRoleName().equals("admin") && !securityUtil.isAdmin())
			throw new SecurityException("Current user is not allowed to add the admin role.");
		
		if(roleToAdd.getRoleName().equals("account.manager") || roleToAdd.getRoleName().equals("client"))
			if(!isUserAllowedForClientId(securityUtil.getPrincipalName(), roleToAdd.getRefId()))
				throw new SecurityException("Current user is not allowed to assign priveleges for the specified client id.");
		
		user.getRoles().add(roleToAdd.buildDataRole());
	}

  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.POST,value="/{id}/roles")
	public void addRoles(@PathVariable("id") Long userPK, @RequestBody Set<Role> rolesToAdd) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot add a role to a null user.");
		
		if(rolesToAdd==null || rolesToAdd.size()==0)
			return;
		
		for(Role role : rolesToAdd)
			addRole(userPK,role);
	}

        @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
	protected User createUser(User newUser) {
		UserDO user = new UserDO();
		newUser.copyTo(user);
		RoleDO tempRole;
		for(Role roleToAdd : newUser.getRoles())
		{
			if(roleToAdd.getRoleName().equals("admin") && !securityUtil.isAdmin())
				throw new SecurityException("Current user is not allowed to add the admin role.");
			
			if(roleToAdd.getRoleName().equals("account.manager") || roleToAdd.getRoleName().equals("client"))
				if(!isUserAllowedForClientId(securityUtil.getPrincipalName(), roleToAdd.getRefId()))
					throw new SecurityException("Current user is not allowed to assign priveleges for the specified client id.");
			
			tempRole = roleToAdd.buildDataRole();
			tempRole.setUser(user);
			user.getRoles().add(tempRole);
		}
		sf.getCurrentSession().persist(user);
		sf.getCurrentSession().flush();
		
		User ret = new User();
		ret.copyFrom(user);
		return ret;
	}

        @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
        @RequestMapping(method=RequestMethod.GET,value="/{id}")
	public User getUserByPK(@PathVariable("id") long pk) {
		User ret=new User();
		ret.copyFrom(getSimpleUserByPK(pk));
		return ret;
	}

	public UserDO getSimpleUserByUsername(String username)
	{
		try
		{
			Criteria crit = sf.getCurrentSession().createCriteria(UserDO.class);
			crit.add(Restrictions.eq("username", username));
			crit.setCacheable(true);
			crit.setCacheRegion("query/userByUsername");
			UserDO ret = (UserDO)crit.uniqueResult();
			
			if(ret==null)
				return null;
			
			if(securityUtil.isAdmin())
				return ret;
			
			if(securityUtil.getPrincipalName().equals(ret.getUsername()))
				return ret;
			
			Set<Long> clientIds = securityUtil.extractClientIds(sf.getCurrentSession());
			
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
	
	public UserDO getSimpleUserByPK(Long pk)
	{
		UserDO ret =  (UserDO)sf.getCurrentSession().get(UserDO.class, pk);

		if(ret==null)
			return null;
		
		if(securityUtil.isAdmin())
			return ret;
		
		if(securityUtil.getPrincipalName().equals(ret.getUsername()))
			return ret;

		Set<Long> clientIds = securityUtil.extractClientIds(sf.getCurrentSession());
		
		for(RoleDO role : ret.getRoles())
		{
			if(role.getRoleName().equals("account.manager"))
				if(clientIds.contains(role.getRefId()))
					return ret;
		}
		
		throw new SecurityException("Current user is not allowed to view the specified user.");
	}
	
  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.GET,value="/{username}")
	public User getUserByUsername(@PathVariable("username") String username) {
		User ret = new User();
		ret.copyFrom(getSimpleUserByUsername(username));
		return ret;
	}

  @RequestMapping(method=RequestMethod.GET,value="/me")
	public User getCurrentUser() {
		User ret = new User();
		ret.copyFrom(getSimpleUserByUsername(securityUtil.getPrincipalName()));
		return ret;
	}

  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.DELETE,value="/{id}/role")
	public void removeRole(@PathVariable("id") Long userPK, @RequestBody Role roleToRemove) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot remove a role from a null user.");
		
		if(roleToRemove==null)
			return;
				
		if(roleToRemove.getRoleName().equals("admin") && !securityUtil.isAdmin())
			throw new SecurityException("Current user is not allowed to remove the admin role.");
		
		if(roleToRemove.getRoleName().equals("account.manager") || roleToRemove.getRoleName().equals("client"))
			if(!isUserAllowedForClientId(securityUtil.getPrincipalName(), roleToRemove.getRefId()))
				throw new SecurityException("Current user is not allowed to remove priveleges for the specified client id.");

		user.getRoles().remove(roleToRemove.buildDataRole());
	}

  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.DELETE,value="/{id}/roles")
	public void removeRoles(@PathVariable("id") Long userPK, @RequestBody Set<Role> rolesToRemove) {
		UserDO user = getSimpleUserByPK(userPK);

		if(user==null)
			throw new IllegalArgumentException("Cannot remove a role from a null user.");
		
		if(rolesToRemove==null || rolesToRemove.size()==0)
			return;
		
		for(Role role : rolesToRemove)
			removeRole(userPK,role);
	}

  @RequestMapping(method=RequestMethod.POST)
	public User save(@RequestBody User user) {
		UserDO current = getSimpleUserByPK(user.getPrimaryKey());
		
		//If this is you, or you're an admin . . . have at it!
		if(!securityUtil.isAdmin() && !securityUtil.getPrincipalName().equals(user.getUsername()))
		{
			//Other than that . . . only account managers allowed
			if(!securityUtil.isCallerInRole("account.manager"))
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
				Set<Long> allowedIDs = securityUtil.extractClientIds(sf.getCurrentSession());
				//Have to match the client user's IDs
				Set<Long> neededIDs = securityUtil.extractClientIds(sf.getCurrentSession(), user.getUsername());
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
		} 
		else 
		{
			if(!securityUtil.isAdmin() && !current.isCorrectPassword(user.getCurrentPassword()))
				throw new SecurityException("You may not change user details unless you're an admin or have the correct password.");
			user.copyTo(current);
			if(user.getRoles()!=null)
				syncRoles(user.getPrimaryKey(),new HashSet<Role>(user.getRoles()));
			ret = new User();
			ret.copyFrom(current);
		}
		return ret;
	}

        @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
        @RequestMapping(method= RequestMethod.PUT,value="/{id}/roles")
	public void syncRoles(@PathVariable("id") Long userPK, @RequestBody Set<Role> roles) {
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
				
				if(role.getRoleName().equals("admin") && !securityUtil.isAdmin())
					throw new SecurityException("Current user may not add the admin role.");
				
				if(role.getRoleName().equals("client") || role.getRoleName().equals("account.manager"))
					if(!isUserAllowedForClientId(securityUtil.getPrincipalName(), role.getRefId()))
						throw new SecurityException("Current user may not add the specified client-specific role.");
			}
			
			for(RoleDO role : user.getRoles())
			{
				if(convertedRoles.contains(role))
					continue;
				
				if(role.getRoleName().equals("admin") && !securityUtil.isAdmin())
					throw new SecurityException("Current user may not remove the admin role.");
				
				if(role.getRoleName().equals("client") || role.getRoleName().equals("account.manager"))
					if(!isUserAllowedForClientId(securityUtil.getPrincipalName(), role.getRefId()))
						throw new SecurityException("Current user may not remove the specified client-specific role.");
			}
			
			user.getRoles().clear();
			sf.getCurrentSession().flush();
			user.getRoles().addAll(convertedRoles);
		}
	}

        @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
        @RequestMapping(method=RequestMethod.GET,value="/available/{username}")
	public boolean allowUsername(@PathVariable("username") String username) {
		return getSimpleUserByUsername(username)!=null;
	}

  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.POST,value="/available")
	public Set<String> usernamesAllowed(@RequestBody Set<String> nameList) {
		String query = "select u.username" +
				" from UserDO u" +
				" where u.username in (:nameList)";
		Query q = sf.getCurrentSession().createQuery(query);
		q.setParameter("nameList", nameList);
		return new HashSet<String>(q.list());
	}

	@SuppressWarnings("unchecked")
  @RequestMapping(method=RequestMethod.GET)
  public List<User> getAllVisibleUsers() 
	{
            return getVisibleUsers(null);
	}
	
	public List<User> getVisibleUsers(List<Long> clientIds)
	{
		Criteria crit = null;
		List<User> ret = new ArrayList<User>();
		
		List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIds);
		
		if (allowedClientIds.size() > 0)
		{
			crit = sf.getCurrentSession().createCriteria(RoleDO.class);
//			crit.add(Restrictions.in("roleName",new String[]{"account.admin","client"}));
			crit.add(Restrictions.in("refId", allowedClientIds));
			crit.createAlias("user", "user");
			crit.setProjection(Projections.distinct(Projections.groupProperty("user")));
			crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			
			User u;
			List<UserDO> list = (List<UserDO>)crit.list();
			for(UserDO user : list)
			{
				u = new User();
				u.copyFrom(user);
				ret.add(u);
			}
		}
		return ret;
	}
	
  @PreAuthorize("hasRole(admin) or hasRole(account.manager)")
  @RequestMapping(method=RequestMethod.DELETE)
	public void delete(@RequestBody User user)
	{
		sf.getCurrentSession().delete(getSimpleUserByUsername(user.getUsername()));
	}

	public boolean isUserAllowedForClientId(String username, Long clientId) {
		return securityUtil.extractClientIds(sf.getCurrentSession(), username).contains(clientId);
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
