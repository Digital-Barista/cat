package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * Entity implementation class for Entity: UserDO
 *
 */

@Entity
@Table(name="users")
@NamedQuery(name = "user.by.username", query = "select u from UserDO u where u.username=:username")
public class UserDO implements Serializable,DataObject {
	
	private String username; 
	private String password;
	private boolean active;
	private String email;
	private String name;
	private long primaryKey;
	private static final long serialVersionUID = 1L;
	
	private Set<RoleDO> roles = new HashSet<RoleDO>();
	
	public UserDO() {
		super();
	} 
	   
	@Column(name="username") 
	public String getUsername() {
 		return this.username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	   
	@Column(name="password")
	private String getPassword() {
 		return this.password;
	}
	private void setPassword(String password) {
		this.password = password;
	}

	public void changePassword(String unencryptedPassword)
	{
		/*
		try
		{
			MessageDigest mdEnc = MessageDigest.getInstance("MD5");
			mdEnc.update(unencryptedPassword.getBytes(), 0, unencryptedPassword.length());
			String md5 = new String(mdEnc.digest());
			password = md5;
			throw new NotImplementedException("Need to encrypt passwords before they can be set.");
		}catch(Exception e)
		{
			throw new RuntimeException("Problem Encrypting a password.");
		}*/
		password = unencryptedPassword;
	}
	
	public boolean isCorrectPassword(String unencryptedPassword)
	{
		try
		{
			MessageDigest mdEnc = MessageDigest.getInstance("MD5");
			mdEnc.update(unencryptedPassword.getBytes(), 0, unencryptedPassword.length());
			String md5 = new String(mdEnc.digest());
			return password.equals(md5);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Problem Encrypting a password.");
		}
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_pk")
	public long getPrimaryKey() {
 		return this.primaryKey;
	}
	public void setPrimaryKey(long primaryKey) {
		this.primaryKey = primaryKey;
	}

	@Column(name="active")
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	@OneToMany(targetEntity=RoleDO.class,fetch=FetchType.EAGER,mappedBy="user")
	@Cascade({CascadeType.DELETE_ORPHAN,
		CascadeType.DELETE,
		CascadeType.EVICT,
		CascadeType.PERSIST,
		CascadeType.REPLICATE})
	@JoinColumn(name="username",referencedColumnName="username")
	public Set<RoleDO> getRoles() {
		return roles;
	}
	public void setRoles(Set<RoleDO> roles) {
		this.roles = roles;
	}

	@Column(name="email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name="name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
