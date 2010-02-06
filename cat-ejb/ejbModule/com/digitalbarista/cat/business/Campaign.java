package com.digitalbarista.cat.business;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;

@XmlRootElement(name="Campaign")
public class Campaign implements BusinessObject<CampaignDO>,Auditable {

	private Long primaryKey;
	private String name;
	
	@SecondaryDescriminator
	private String uid;
	private int currentVersion;
	private Set<Node> nodes=new HashSet<Node>();
	private Set<Connector> connectors=new HashSet<Connector>();
	private Set<AddInMessage> addInMessages = new HashSet<AddInMessage>();
	
	@PrimaryDescriminator
	private Long clientPK;
	private CampaignMode mode;
	
	public void copyFrom(CampaignDO dataObject, Integer version) {
		primaryKey=dataObject.getPrimaryKey();
		name=dataObject.getName();
		currentVersion=dataObject.getCurrentVersion();
		uid = dataObject.getUID();
		mode = dataObject.getMode();
		if(dataObject.getClient()!=null)
		{
			clientPK = dataObject.getClient().getPrimaryKey();
		}
		
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
	public void copyFrom(CampaignDO dataObject) {
		copyFrom(dataObject,null);
	}

	@Override
	public void copyTo(CampaignDO dataObject) {
		dataObject.setName(name);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append(";uid:"+getUid());
		ret.append(";currentVersion:"+getCurrentVersion());
		ret.append(";clientID:"+getClientPK());
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
		
		return ret.toString();
	}

	@XmlAttribute(name="id")
	public Long getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public int getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(int currentVersion) {
		this.currentVersion = currentVersion;
	}
	
	
	@XmlAttribute
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@XmlElementWrapper(name="Nodes")
	@XmlElementRef
	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	@XmlElementWrapper(name="Connectors")
	@XmlElementRef
	public Set<Connector> getConnectors() {
		return connectors;
	}

	public void setConnectors(Set<Connector> connectors) {
		this.connectors = connectors;
	}

	@XmlAttribute(name="clientid")
	public Long getClientPK() {
		return clientPK;
	}

	public void setClientPK(Long clientPK) {
		this.clientPK = clientPK;
	}

	@XmlAttribute
	public CampaignMode getMode() {
		return mode;
	}

	public void setMode(CampaignMode mode) {
		this.mode = mode;
	}

	public Set<AddInMessage> getAddInMessages() {
		return addInMessages;
	}

	public void setAddInMessages(Set<AddInMessage> addInMessages) {
		this.addInMessages = addInMessages;
	}
	
	
}
