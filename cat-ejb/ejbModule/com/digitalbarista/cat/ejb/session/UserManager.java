package com.digitalbarista.cat.ejb.session;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.business.Role;
import com.digitalbarista.cat.business.User;
import com.digitalbarista.cat.data.UserDO;
import com.digitalbarista.cat.util.SecurityUtil;

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
	@Wrapped(element="Users")
	public List<User> getAllVisibleUsers();
	@POST
	public User save(User existingUser);
	@Path("/{id}/roles")
	@POST
	public void addRole(@PathParam("id") Long userPK,Role roleToAdd);
	@Path("/{id}/roles")
	@POST
	public void addRoles(@PathParam("id") Long userPK,@Wrapped(element="Roles") Set<Role> rolesToAdd);
	@Path("/{id}/roles")
	@DELETE
	public void removeRole(@PathParam("id") Long userPK,Role roleToRemove);
	@Path("/{id}/roles")
	@DELETE
	public void removeRoles(@PathParam("id") Long userPK,@Wrapped(element="Roles") Set<Role> rolesToRemove);
	@Path("/{id}/roles")
	@PUT
	public void syncRoles(@PathParam("id") Long userPK,@Wrapped(element="Roles") Set<Role> roles);
	@Path("/available/{username}")
	@GET
	public boolean allowUsername(@PathParam("username") String username);
	@Path("/available")
	@POST
	@Wrapped(element="Usernames")
	public Set<String> usernamesAllowed(@Wrapped(element="Usernames") Set<String> nameList);
	@DELETE
	public void delete(User user);
	public boolean isUserAllowedForClientId(String username, Long clientId);
	
	public List<User> getVisibleUsers(List<Long> clientIds);
}
