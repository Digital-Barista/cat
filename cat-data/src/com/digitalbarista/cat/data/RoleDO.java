package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: RoleDO
 *
 */

@Entity
@Table(name="roles")
public class RoleDO implements Serializable,DataObject {

	private long primaryKey;
	private UserDO user;
	private String roleName;
	private String roleType;
	private Long refId;
	private static final long serialVersionUID = 1L;

	public RoleDO() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="role_pk")
	public long getPrimaryKey() {
		return this.primaryKey;
	}
	public void setPrimaryKey(long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="role_name")
	public String getRoleName() {
		return this.roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	@Column(name="role_type")
	public String getRoleType() {
		return this.roleType;
	}
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof RoleDO))
			return false;
		if(!(this.roleName==null & ((RoleDO)obj).roleName==null))
			return false; //one is null, but not the other.
		if(roleName!=null && !roleName.equals(((RoleDO)obj).roleName))
			return false;
		if(!(this.roleType==null & ((RoleDO)obj).roleType==null))
			return false; //one is null, but not the other.
		if(roleType!=null && !roleType.equals(((RoleDO)obj).roleType))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int typeHash=(roleType!=null) ? roleType.hashCode() : 0;
		int nameHash=(roleName!=null) ? roleName.hashCode() : 0;
		return typeHash*13 + nameHash;
	}

	@Column(name="ref_id")
	public Long getRefId() {
		return refId;
	}

	public void setRefId(Long refId) {
		this.refId = refId;
	}

	@ManyToOne
	@JoinColumn(name="username",referencedColumnName="username",nullable=false)
	public UserDO getUser() {
		return user;
	}

	public void setUser(UserDO user) {
		this.user = user;
	}
   
}
