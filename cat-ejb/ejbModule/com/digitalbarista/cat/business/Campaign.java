package com.digitalbarista.cat.business;

import java.util.HashSet;
import java.util.Set;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.CampaignDO;
import com.digitalbarista.cat.data.CampaignMode;
import com.digitalbarista.cat.data.EntryPointType;

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
	private EntryPointType type;
	private String defaultFromAddress;
	private CampaignMode mode;
	
	public void copyFrom(CampaignDO dataObject, Integer version) {
		primaryKey=dataObject.getPrimaryKey();
		name=dataObject.getName();
		currentVersion=dataObject.getCurrentVersion();
		uid = dataObject.getUID();
		type = dataObject.getCampaignType();
		addInMessage = dataObject.getAddInMessage();
		defaultFromAddress = dataObject.getDefaultFrom();
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
		dataObject.setDefaultFrom(defaultFromAddress);
		dataObject.setAddInMessage(addInMessage);
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		ret.append(";fromAddress:"+getDefaultFromAddress());
		ret.append(";uid:"+getUid());
		ret.append(";currentVersion:"+getCurrentVersion());
		ret.append(";clientID:"+getClientPK());
		ret.append(";addInMessage:"+getAddInMessage());
		ret.append(";name:"+getName());
		return ret.toString();
	}

	public Long getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(int currentVersion) {
		this.currentVersion = currentVersion;
	}
	
	public String getAddInMessage() {
		return addInMessage;
	}

	public void setAddInMessage(String addInMessage) {
		this.addInMessage = addInMessage;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	public Set<Connector> getConnectors() {
		return connectors;
	}

	public void setConnectors(Set<Connector> connectors) {
		this.connectors = connectors;
	}

	public Long getClientPK() {
		return clientPK;
	}

	public void setClientPK(Long clientPK) {
		this.clientPK = clientPK;
	}

	public EntryPointType getType() {
		return type;
	}

	public void setType(EntryPointType type) {
		this.type = type;
	}

	public String getDefaultFromAddress() {
		return defaultFromAddress;
	}

	public void setDefaultFromAddress(String defaultFromAddress) {
		this.defaultFromAddress = defaultFromAddress;
	}

	public CampaignMode getMode() {
		return mode;
	}

	public void setMode(CampaignMode mode) {
		this.mode = mode;
	}
}
