package com.digitalbarista.cat.business;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;

@XmlRootElement
public class Campaign implements BusinessObject<CampaignDO>,Auditable {

	private Long primaryKey;
	private String name;
	
	@SecondaryDescriminator
	private String uid;
	private int currentVersion;
	private String addInMessage;
	private Set<Node> nodes=new HashSet<Node>();
	private Set<Connector> connectors=new HashSet<Connector>();
	
	@PrimaryDescriminator
	private Long clientPK;
	private CampaignMode mode;
	
	public void copyFrom(CampaignDO dataObject, Integer version) {
		primaryKey=dataObject.getPrimaryKey();
		name=dataObject.getName();
		currentVersion=dataObject.getCurrentVersion();
		uid = dataObject.getUID();
		addInMessage = dataObject.getAddInMessage();
		mode = dataObject.getMode();
		if(dataObject.getClient()!=null)
		{
			clientPK = dataObject.getClient().getPrimaryKey();
		}
	}
	
	@Override
	public void copyFrom(CampaignDO dataObject) {
		copyFrom(dataObject,null);
	}

	@Override
	public void copyTo(CampaignDO dataObject) {
		dataObject.setName(name);
		dataObject.setAddInMessage(addInMessage);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append(";uid:"+getUid());
		ret.append(";currentVersion:"+getCurrentVersion());
		ret.append(";clientID:"+getClientPK());
		ret.append(";addInMessage:"+getAddInMessage());
		ret.append(";name:"+getName());
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
	
	@XmlElement
	public String getAddInMessage() {
		return addInMessage;
	}

	public void setAddInMessage(String addInMessage) {
		this.addInMessage = addInMessage;
	}
	
	@XmlAttribute
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Wrapped(element="Nodes")
	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	@Wrapped(element="Connectors")
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
}
