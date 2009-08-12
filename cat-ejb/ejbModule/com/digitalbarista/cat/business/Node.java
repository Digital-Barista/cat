package com.digitalbarista.cat.business;

import java.util.ArrayList;
import java.util.List;

import com.digitalbarista.cat.audit.PrimaryDescriminator;
import com.digitalbarista.cat.audit.SecondaryDescriminator;
import com.digitalbarista.cat.data.ConnectionPoint;
import com.digitalbarista.cat.data.NodeConnectorLinkDO;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;

public abstract class Node implements BusinessObject<NodeDO> {

	private List<String> downstreamConnections=new ArrayList<String>();
	private List<String> upstreamConnections=new ArrayList<String>();
	
	@PrimaryDescriminator
	private String campaignUID;
	private String name;
	
	@SecondaryDescriminator
	private String uid;
	
	public void copyFrom(NodeDO dataObject, Integer version)
	{
		if(!dataObject.getType().equals(getType()))
			throw new IllegalArgumentException("The node Business Object is not the same type as the NodeDO Data Object: Biz="+getType()+", data="+dataObject.getType());
		name=dataObject.getName();
		uid=dataObject.getUID().toString();
		campaignUID=dataObject.getCampaign().getUID();
		for(NodeConnectorLinkDO link : dataObject.getConnections())
		{
			if(!link.getVersion().equals(version))
				continue;
			if(link.getConnectionPoint().equals(ConnectionPoint.Source))
				downstreamConnections.add(link.getConnector().getUID());
			else
				upstreamConnections.add(link.getConnector().getUID());
		}
	}
	
	public void copyFrom(NodeDO dataObject)
	{
		copyFrom(dataObject,dataObject.getCampaign().getCurrentVersion());
	}
	
	public void copyTo(NodeDO dataObject)
	{
		dataObject.setName(name);
		dataObject.setType(getType());
	}
	
	protected void buildAndAddNodeInfo(NodeDO node,String name,String data,Integer version)
	{
		NodeInfoDO ni = new NodeInfoDO();
		ni.setName(name);
		ni.setValue(data);
		ni.setNode(node);
		ni.setVersion(version);
		node.getNodeInfo().add(ni);
	}
	
	public static Node createNodeBO(NodeDO dataObject)
	{
		if(dataObject==null || dataObject.getType()==null)
			throw new IllegalArgumentException("Cannot create NodeDO Business Object for null node, or unknown type.");

		switch(dataObject.getType())
		{
		case Entry:
			return new EntryNode();
		case Message:
			return new MessageNode();
		case Termination:
			return new TerminationNode();
		}
		throw new IllegalArgumentException("Unknown node type specified.  Cannot create a NodeDO Business Object.");
	}
	
	public List<String> getDownstreamConnections() {
		return downstreamConnections;
	}
	public void setDownstreamConnections(List<String> downstreamConnections) {
		this.downstreamConnections = downstreamConnections;
	}
	public List<String> getUpstreamConnections() {
		return upstreamConnections;
	}
	public void setUpstreamConnections(List<String> upstreamConnections) {
		this.upstreamConnections = upstreamConnections;
	}
	public abstract NodeType getType();
	public final void setType(NodeType type) {
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
}
