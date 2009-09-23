package com.digitalbarista.cat.business;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.EntryPointType;

public class ResponseConnector extends Connector implements Auditable {

	public static final String INFO_PROPERTY_ENTRY_POINT="EntryPointDO";
	public static final String INFO_PROPERTY_ENTRY_POINT_TYPE="EntryPointType";
	public static final String INFO_PROPERTY_CONNECTOR_KEYWORD="KeywordDO";
	
	private String entryPoint;
	private EntryPointType entryPointType;
	private String keyword;
	
	@Override
	public void copyFrom(ConnectorDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(ConnectorInfoDO ci : dataObject.getConnectorInfo())
		{
			if(!ci.getVersion().equals(version))
				continue;
			
			if(ci.getName().equals(INFO_PROPERTY_ENTRY_POINT))
				entryPoint = ci.getValue();
			else if(ci.getName().equals(INFO_PROPERTY_ENTRY_POINT))
				entryPoint = ci.getValue();

			
			if(ci.getName().equals(INFO_PROPERTY_ENTRY_POINT_TYPE))
				entryPointType = EntryPointType.valueOf(ci.getValue());
			else if(ci.getName().equals(INFO_PROPERTY_ENTRY_POINT_TYPE))
				entryPointType = EntryPointType.valueOf(ci.getValue());

			
			if(ci.getName().equals(INFO_PROPERTY_CONNECTOR_KEYWORD))
				keyword = ci.getValue();
			else if(ci.getName().equals(INFO_PROPERTY_CONNECTOR_KEYWORD))
				keyword = ci.getValue();
		}
	}

	@Override
	public void copyTo(ConnectorDO dataObject) {
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,ConnectorInfoDO> nodes = new HashMap<String,ConnectorInfoDO>();
		for(ConnectorInfoDO ci : dataObject.getConnectorInfo())
		{
			if(!ci.getVersion().equals(version))
				continue;
			nodes.put(ci.getName(), ci);
		}
		
		if(entryPointType!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_ENTRY_POINT_TYPE))
				nodes.get(INFO_PROPERTY_ENTRY_POINT_TYPE).setValue(entryPointType.toString());
			else
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_ENTRY_POINT_TYPE, entryPointType.toString(), version);
		}
		
		if(entryPoint!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_ENTRY_POINT))
				nodes.get(INFO_PROPERTY_ENTRY_POINT).setValue(entryPoint);
			else
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_ENTRY_POINT, entryPoint, version);
		}
		
		if(keyword!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_CONNECTOR_KEYWORD))
				nodes.get(INFO_PROPERTY_CONNECTOR_KEYWORD).setValue(keyword);
			else
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_CONNECTOR_KEYWORD, keyword, version);
		}
	}
	
	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		ret.append(";incomingAddress:"+getEntryPoint());
		ret.append(";keyword:"+getKeyword());
		if(getEntryPointType()!=null)
			ret.append(";addressType:"+getEntryPointType().toString());
		else
			ret.append(";addressType:null");
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";source:"+getSourceNodeUID());
		ret.append(";dest:"+getDestinationUID());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}

	@Override
	public ConnectorType getType() {
		return ConnectorType.Response;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public EntryPointType getEntryPointType() {
		return entryPointType;
	}

	public void setEntryPointType(EntryPointType entryPointType) {
		this.entryPointType = entryPointType;
	}
}
