package com.digitalbarista.cat.business;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.EntryPointDO;

@XmlRootElement
public class Client implements
		BusinessObject<ClientDO>,Auditable {

	@PrimaryDescriminator
	private Long clientId;
	@SecondaryDescriminator
	private String name;
	private String adminAddInMessage;
	private String userAddInMessage;
	private Set<EntryPointDefinition> entryPoints = new TreeSet<EntryPointDefinition>(
			new Comparator<EntryPointDefinition>()
			{
				@Override
				public int compare(EntryPointDefinition arg0,
						EntryPointDefinition arg1) {
					if(arg0==null && arg1==null)
						return 0;
					if(arg1==null)
						return 1;
					if(arg0==null)
						return -1;
					if(arg0.getValue()==null && arg1.getValue()==null)
						return 0;
					if(arg0.getValue()==null)
						return -1;
					return arg0.getValue().compareToIgnoreCase(arg1.getValue());
				}
			});
	
	@Override
	public void copyFrom(ClientDO dataObject) {
		clientId=dataObject.getPrimaryKey();
		name=dataObject.getName();
		adminAddInMessage = dataObject.getAdminAddInMessage();
		userAddInMessage = dataObject.getUserAddInMessage();
		entryPoints = new HashSet<EntryPointDefinition>();
		EntryPointDefinition epd;
		if (dataObject.getEntryPoints() != null)
		{
			for(EntryPointDO ep : dataObject.getEntryPoints())
			{
				epd = new EntryPointDefinition();
				epd.copyFrom(ep,dataObject.getPrimaryKey());
				entryPoints.add(epd);
			}
		}
}

	@Override
	public void copyTo(ClientDO dataObject) {
		dataObject.setName(name);
		dataObject.setAdminAddInMessage(adminAddInMessage);
		dataObject.setUserAddInMessage(userAddInMessage);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("pk:"+getClientId());
		ret.append(";name:"+getName());
		ret.append(";AdminAddInMessage:"+getAdminAddInMessage());
		ret.append(";UserAddInMessage:"+getUserAddInMessage());
		ret.append(";entryPoints:{");
		if(getEntryPoints()==null)
		{
			ret.append("none");
		}
		else
		{
			for(EntryPointDefinition epd : getEntryPoints())
				ret.append(epd.getType().toString()+"-"+epd.getValue()+",");
		}
		ret.append("}");
		return ret.toString();
	}

	@XmlAttribute
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getAdminAddInMessage() {
		return adminAddInMessage;
	}

	public void setAdminAddInMessage(String adminAddInMessage) {
		this.adminAddInMessage = adminAddInMessage;
	}

	@XmlAttribute
	public String getUserAddInMessage() {
		return userAddInMessage;
	}

	public void setUserAddInMessage(String userAddInMessage) {
		this.userAddInMessage = userAddInMessage;
	}
	
	@Wrapped(element="EntryPoints")
	public Set<EntryPointDefinition> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(Set<EntryPointDefinition> entryPoints) {
		this.entryPoints = entryPoints;
	}

}
