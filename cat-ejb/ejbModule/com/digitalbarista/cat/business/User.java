package com.digitalbarista.cat.business;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.UserDO;

@XmlRootElement
public class User implements BusinessObject<UserDO>{

	private String username; 
	private String password;
	private Boolean active;
	private Long primaryKey;
	private List<Role> roles = new ArrayList<Role>();
	
	public void copyFrom(UserDO dataObject)
	{
		if(dataObject==null)
			return;
		username = dataObject.getUsername();
		active = dataObject.isActive();
		primaryKey = dataObject.getPrimaryKey();
		roles.clear();
		Role boRole;
		for(RoleDO role : dataObject.getRoles())
		{
			boRole = new Role();
			boRole.copyFrom(role);
			roles.add(boRole);
		}
	}
	public void copyTo(UserDO user)
	{
		user.setUsername(username);
		user.setActive(active != null && active); // Should render to false if null.
		if(password!=null)
			user.changePassword(password);
	}
	
	@XmlAttribute
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@XmlAttribute
	public Boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	@XmlAttribute(name="id")
	public Long getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	@Wrapped(element="Roles")
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
}
