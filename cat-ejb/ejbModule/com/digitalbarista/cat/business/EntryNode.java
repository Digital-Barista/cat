package com.digitalbarista.cat.business;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;

public class EntryNode extends Node implements Auditable{

	public static final String INFO_PROPERTY_ENTRY_TYPE="EntryType";
	public static final String INFO_PROPERTY_ENTRY_POINT="EntryPointDO";
	public static final String INFO_PROPERTY_KEYWORD="EntryKeyword";
	
	private EntryPointType entryType;
	private String entryPoint;
	private String keyword;
	
	@Override
	public void copyFrom(NodeDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			
			if(ni.getName().equals(INFO_PROPERTY_ENTRY_POINT))
				entryPoint = ni.getValue();
			else if(ni.getName().equals(INFO_PROPERTY_ENTRY_TYPE))
				entryType = EntryPointType.valueOf(ni.getValue());
			else if(ni.getName().equals(INFO_PROPERTY_KEYWORD))
				keyword = ni.getValue();
		}
	}

	@Override
	public void copyTo(NodeDO dataObject) {
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,NodeInfoDO> nodes = new HashMap<String,NodeInfoDO>();
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.equals(version))
				continue;
			nodes.put(ni.getName(), ni);
		}
		
		if(entryType!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_ENTRY_TYPE))
				nodes.get(INFO_PROPERTY_ENTRY_TYPE).setValue(entryType.toString());
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_ENTRY_TYPE, entryType.toString(), version);
		}
		
		if(entryPoint!=null)
		{
			if(nodes.containsValue(INFO_PROPERTY_ENTRY_POINT))
				nodes.get(INFO_PROPERTY_ENTRY_POINT).setValue(entryPoint);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_ENTRY_POINT, entryPoint, version);
		}
		
		if(keyword!=null)
		{
			if(nodes.containsValue(INFO_PROPERTY_KEYWORD))
				nodes.get(INFO_PROPERTY_KEYWORD).setValue(keyword);
			else
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_KEYWORD, keyword, version);
		}
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		ret.append(";incomingAddress:"+getEntryPoint());
		ret.append(";keyword:"+getKeyword());
		if(getEntryType()!=null)
			ret.append(";addressType:"+getEntryType().toString());
		else
			ret.append(";addressType:null");
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}

	@Override
	public NodeType getType() {
		return NodeType.Entry;
	}

	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
