package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.RoleDO;

@XmlRootElement
public class Role implements BusinessObject<RoleDO>{

	private String roleName;
	private String roleType;
	private Long refId;
	
	@XmlAttribute
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	@XmlAttribute
	public String getRoleType() {
		return roleType;
	}
	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	@XmlAttribute
	public Long getRefId() {
		return refId;
	}
	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public boolean equivalentOf(RoleDO dataRole) {
		if(dataRole==null)
			return false;
		if(!(this.roleName==null & dataRole.getRoleName()==null))
			return false; //one is null, but not the other.
		if(roleName!=null && !roleName.equals(dataRole.getRoleName()))
			return false;
		if(!(this.roleType==null & dataRole.getRoleType()==null))
			return false; //one is null, but not the other.
		if(roleType!=null && !roleType.equals(dataRole.getRoleType()))
			return false;
		if(!(this.refId==null & dataRole.getRefId()==null))
			return false; //one is null, but not the other.
		if(refId!=null && !refId.equals(dataRole.getRefId()))
			return false;
		return true;
	}
	@Override
	public void copyFrom(RoleDO dataObject) {
		roleName=dataObject.getRoleName();
		roleType=dataObject.getRoleType();
		refId=dataObject.getRefId();
	}
	@Override
	public void copyTo(RoleDO dataObject) {
		throw new UnsupportedOperationException("Roles may not be changed.  The must be removed and re-created.");
	}
	public RoleDO buildDataRole()
	{
		RoleDO temp = new RoleDO();
		temp.setRoleName(roleName);
		temp.setRoleType(roleType);
		temp.setRefId(refId);
		return temp;
	}
}
