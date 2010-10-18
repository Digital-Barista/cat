package com.digitalbarista.cat.business;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;

@XmlRootElement(name="MessageNode")
@XmlType(name="MessageNode")
public class MessageNode extends Node implements Auditable {

	public static final String INFO_PROPERTY_MESSAGE="MessageText";
	public static final String INFO_PROPERTY_MESSAGE_TYPE="MessageType";
	
	private String message;
	private EntryPointType messageType;
	
	@Override
	public void copyFrom(NodeDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			
			if(ni.getName().equals(INFO_PROPERTY_MESSAGE))
				message = ni.getValue();

			if(ni.getName().equals(INFO_PROPERTY_MESSAGE_TYPE))
				messageType = EntryPointType.valueOf(ni.getValue());
		}
	}

	@Override
	public void copyTo(NodeDO dataObject) {
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,NodeInfoDO> nodes = new HashMap<String,NodeInfoDO>();
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			nodes.put(ni.getName(), ni);
		}
		
		if(message!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_MESSAGE))
				nodes.get(INFO_PROPERTY_MESSAGE).setValue(message);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_MESSAGE, message, version);
		}

		if(messageType!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_MESSAGE_TYPE))
				nodes.get(INFO_PROPERTY_MESSAGE_TYPE).setValue(messageType.toString());
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_MESSAGE_TYPE, messageType.toString(), version);
		}
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		ret.append(";message:"+getMessage());
		ret.append(";messageType:"+getMessageType());
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}


	@Override
	public NodeType getType() {
		return NodeType.Message;
	}

	@XmlAttribute
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlAttribute
	public EntryPointType getMessageType() {
		return messageType;
	}

	public void setMessageType(EntryPointType messageType) {
		this.messageType = messageType;
	}
}
