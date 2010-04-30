package com.digitalbarista.cat.business;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ClientInfoDO;
import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.KeywordLimitDO;

@XmlRootElement
public class Client implements
		BusinessObject<ClientDO>,Auditable {

	@PrimaryDescriminator
	private Long clientId;
	@SecondaryDescriminator
	private String name;
	private String contactName;
	private String contactEmail;
	private String contactPhone;
	private boolean active;
	private Set<AddInMessage> addInMessages = new HashSet<AddInMessage>();
	private Set<KeywordLimit> keywordLimits;
	private Set<ClientInfo> clientInfos = new HashSet<ClientInfo>();
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
		contactName=dataObject.getContactName();
		contactEmail=dataObject.getContactEmail();
		contactPhone=dataObject.getContactPhone();
		active=dataObject.isActive();
		keywordLimits = new HashSet<KeywordLimit>();
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
		
		// Copy client infos
		if (dataObject.getClientInfos() != null)
		{
			for (ClientInfoDO infoDO : dataObject.getClientInfos())
			{
				ClientInfo info = new ClientInfo();
				info.copyFrom(infoDO);
				clientInfos.add(info);
			}
		}
		
		// Copy keyword limits
		if (dataObject.getKeywordLimits() != null)
		{
			for (KeywordLimitDO kl : dataObject.getKeywordLimits())
			{
				KeywordLimit limit = new KeywordLimit();
				limit.copyFrom(kl);
				keywordLimits.add(limit);
			}
		}
		
		// Copy addin messages
		if (dataObject.getAddInMessages() != null)
		{
			for (AddInMessageDO addDO : dataObject.getAddInMessages())
			{
				AddInMessage add = new AddInMessage();
				add.copyFrom(addDO);
				addInMessages.add(add);
			}
		}
	}

	@Override
	public void copyTo(ClientDO dataObject) {
		dataObject.setActive(active);
		dataObject.setName(name);
		dataObject.setContactEmail(contactEmail);
		dataObject.setContactPhone(contactPhone);
		dataObject.setContactName(contactName);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("pk:"+getClientId());
		ret.append(";name:"+getName());

		// Build list of add in messages
		ret.append(";addInMessages:(");
		if (getAddInMessages() == null ||
			getAddInMessages().size() == 0)
		{
			ret.append("none");
		}
		else
		{
			for (AddInMessage add : getAddInMessages())
				ret.append(add.getEntryType() + " - " + add.getType() + " - " + add.getMessage() + ", ");
		}
		ret.append(")");
			
		// Build list of entry points
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
	
	@XmlElementWrapper(name="EntryPoints")
	@XmlElement(name="EntryPoint")
	public Set<EntryPointDefinition> getEntryPoints() {
		
		return entryPoints;
	}

	public void setEntryPoints(Set<EntryPointDefinition> entryPoints) {
		this.entryPoints = entryPoints;
	}

	@XmlElementWrapper(name="KeywordLimits")
	@XmlElement(name="KeywordLimit")
	public Set<KeywordLimit> getKeywordLimits() {
		return keywordLimits;
	}

	public void setKeywordLimits(Set<KeywordLimit> keywordLimits) {
		this.keywordLimits = keywordLimits;
	}

	public Set<AddInMessage> getAddInMessages() {
		return addInMessages;
	}

	public void setAddInMessages(Set<AddInMessage> addInMessages) {
		this.addInMessages = addInMessages;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<ClientInfo> getClientInfos() {
		return clientInfos;
	}

	public void setClientInfos(Set<ClientInfo> clientInfos) {
		this.clientInfos = clientInfos;
	}
	
}
