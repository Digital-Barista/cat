package com.digitalbarista.cat.business;

import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.ConnectionPoint;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.NodeConnectorLinkDO;

public abstract class Connector implements
		BusinessObject<ConnectorDO> {

	private String sourceNodeUID;
	private String destinationUID;
	
	@PrimaryDescriminator 
	private String campaignUID;
	private String name;
	
	@SecondaryDescriminator
	private String uid;
	
	public void copyFrom(ConnectorDO dataObject, Integer version)
	{
		if(!dataObject.getType().equals(getType()))
			throw new IllegalArgumentException("The ConnectorDO Business Object is not the same type as the ConnectorDO Data Object: Biz="+getType()+", data="+dataObject.getType());
		name=dataObject.getName();
		uid=dataObject.getUID().toString();
		campaignUID=dataObject.getCampaign().getUID();
		for(NodeConnectorLinkDO link : dataObject.getConnections())
		{
			if(!link.getVersion().equals(version))
				continue;
			if(link.getConnectionPoint().equals(ConnectionPoint.Source))
				sourceNodeUID=link.getNode().getUID();
			else
				destinationUID=link.getNode().getUID();
		}
	}
	
	public void copyFrom(ConnectorDO dataObject)
	{
		copyFrom(dataObject,dataObject.getCampaign().getCurrentVersion());
	}
	
	public void copyTo(ConnectorDO dataObject)
	{
		dataObject.setName(name);
		dataObject.setUID(uid);
		dataObject.setType(getType());
	}

	protected void buildAndAddConnectorInfo(ConnectorDO connector,String name,String data,Integer version)
	{
		ConnectorInfoDO ci = new ConnectorInfoDO();
		ci.setName(name);
		ci.setValue(data);
		ci.setConnector(connector);
		ci.setVersion(version);
		connector.getConnectorInfo().add(ci);
	}
	
	public static Connector createConnectorBO(ConnectorDO dataObject)
	{
		if(dataObject==null || dataObject.getType()==null)
			throw new IllegalArgumentException("Cannot create ConnectorDO Business Object for null ConnectorDO, or unknown type.");

		switch(dataObject.getType())
		{
		case Calendar:
			return new CalendarConnector();
		case Immediate:
			return new ImmediateConnector();
		case Response:
			return new ResponseConnector();
		case Interval:
			return new IntervalConnector();
		}
		throw new IllegalArgumentException("Unknown connector type specified.  Cannot create a ConnectorDO Business Object.");
	}
	
	public abstract ConnectorType getType();
	public final void setType(ConnectorType type) {
		return;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getCampaignUID() {
		return campaignUID;
	}

	public void setCampaignUID(String campaignUID) {
		this.campaignUID = campaignUID;
	}

	public String getSourceNodeUID() {
		return sourceNodeUID;
	}

	public void setSourceNodeUID(String sourceNodeUID) {
		this.sourceNodeUID = sourceNodeUID;
	}

	public String getDestinationUID() {
		return destinationUID;
	}

	public void setDestinationUID(String destinationUID) {
		this.destinationUID = destinationUID;
	}
}
