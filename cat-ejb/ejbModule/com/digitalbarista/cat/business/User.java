package com.digitalbarista.cat.business;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.digitalbarista.cat.data.RoleDO;
import com.digitalbarista.cat.data.UserDO;

@XmlRootElement
public class User implements BusinessObject<UserDO>{

	private String username; 
	private String password;
	private String name;
	private String email;
	private Boolean active;
	private Long primaryKey;
	private List<Role> roles = new ArrayList<Role>();
	
	public void copyFrom(UserDO dataObject)
	{
		if(dataObject==null)
			return;
		username = dataObject.getUsername();
		email = dataObject.getEmail();
		name = dataObject.getName();
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
		user.setEmail(email);
		user.setName(name);
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

	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@XmlTransient
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@XmlAttribute
	public Boolean getIsActive() {
		return active;
	}
	public void setIsActive(Boolean active) {
		this.active = active;
	}
	@XmlAttribute(name="id")
	public Long getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	@XmlElementWrapper(name="Roles")
	@XmlElement(name="Role")
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
}
