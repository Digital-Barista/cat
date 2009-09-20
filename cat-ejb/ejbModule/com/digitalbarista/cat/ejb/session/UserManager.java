package com.digitalbarista.cat.ejb.session;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import com.digitalbarista.cat.business.Role;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.UserDO;

@Local
public interface UserManager {
	public User getUserByUsername(String username);
	public UserDO getSimpleUserByUsername(String username);
	public User getUserByPK(long pk);
	public User getCurrentUser();
	public List<User> getAllVisibleUsers();
	public User save(User existingUser);
	public void addRole(Long userPK,Role roleToAdd);
	public void addRoles(Long userPK,Set<Role> rolesToAdd);
	public void removeRole(Long userPK,Role roleToRemove);
	public void removeRoles(Long userPK,Set<Role> rolesToRemove);
	public void syncRoles(Long userPK,Set<Role> roles);
	public boolean allowUsername(String username);
	public Set<String> usernamesAllowed(Set<String> nameList);
	public void delete(User user);
	public Set<Long> extractClientIds(String username);
	public boolean isUserAllowedForClientId(String username, Long clientId);
}
