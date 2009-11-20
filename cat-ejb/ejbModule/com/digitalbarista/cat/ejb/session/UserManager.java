package com.digitalbarista.cat.ejb.session;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.digitalbarista.cat.business.Role;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.UserDO;

@Local
@Path("/users")
@Produces({"application/xml","application/json"})
@Consumes({"application/xml","application/json"})
public interface UserManager {
	@GET
	public User getUserByUsername(@QueryParam("username") String username);
	public UserDO getSimpleUserByUsername(String username);
	@Path("/{id}")
	@GET
	public User getUserByPK(@PathParam("id") long pk);
	@GET
	@Path("/me")
	public User getCurrentUser();
	@GET
	public List<User> getAllVisibleUsers();
	@POST
	public User save(User existingUser);
	@Path("/{id}/roles")
	@POST
	public void addRole(@PathParam("id") Long userPK,Role roleToAdd);
	public void addRoles(Long userPK,Set<Role> rolesToAdd);
	@Path("/{id}/roles")
	@DELETE
	public void removeRole(@PathParam("id") Long userPK,Role roleToRemove);
	public void removeRoles(Long userPK,Set<Role> rolesToRemove);
	public void syncRoles(Long userPK,Set<Role> roles);
	public boolean allowUsername(String username);
	public Set<String> usernamesAllowed(Set<String> nameList);
	@DELETE
	public void delete(User user);
	public Set<Long> extractClientIds(String username);
	public boolean isUserAllowedForClientId(String username, Long clientId);
}
