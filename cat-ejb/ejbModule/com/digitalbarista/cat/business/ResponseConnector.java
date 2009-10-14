package com.digitalbarista.cat.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;
import com.digitalbarista.cat.data.EntryPointType;

@XmlRootElement
public class ResponseConnector extends Connector implements Auditable {

	public static final String INFO_PROPERTY_ENTRY_POINT="EntryPointDO";
	public static final String INFO_PROPERTY_ENTRY_POINT_TYPE="EntryPointType";
	public static final String INFO_PROPERTY_CONNECTOR_KEYWORD="KeywordDO";
	
	private List<EntryPointType> entryTypes = new ArrayList<EntryPointType>();
	private List<String> entryPoints = new ArrayList<String>();
	private List<String> keywords = new ArrayList<String>();
	
	@Override
	public void copyFrom(ConnectorDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(ConnectorInfoDO ci : dataObject.getConnectorInfo())
		{
			if(!ci.getVersion().equals(version))
				continue;
						
			if(ci.getName().equals(INFO_PROPERTY_ENTRY_POINT))
			{
				entryPoints.clear();
				entryPoints.add(ci.getValue());
			}
			else if(ci.getName().equals(INFO_PROPERTY_ENTRY_POINT_TYPE))
			{
				entryTypes.clear();
				entryTypes.add(EntryPointType.valueOf(ci.getValue()));
			}
			else if(ci.getName().equals(INFO_PROPERTY_CONNECTOR_KEYWORD))
			{
				keywords.clear();
				keywords.add(ci.getValue());
			}
			else if(ci.getName().startsWith(INFO_PROPERTY_ENTRY_POINT+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_ENTRY_POINT+"\\[([\\d]+)\\]").matcher(ci.getName());
				r.matches();
				fillListAndSet(entryPoints,new Integer(r.group(1)), ci.getValue());
			}
			else if(ci.getName().startsWith(INFO_PROPERTY_ENTRY_POINT_TYPE+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_ENTRY_POINT_TYPE+"\\[([\\d]+)\\]").matcher(ci.getName());
				r.matches();
				fillListAndSet(entryTypes,new Integer(r.group(1)), EntryPointType.valueOf(ci.getValue()));
			}
			else if(ci.getName().startsWith(INFO_PROPERTY_CONNECTOR_KEYWORD+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_CONNECTOR_KEYWORD+"\\[([\\d]+)\\]").matcher(ci.getName());
				r.matches();
				fillListAndSet(keywords,new Integer(r.group(1)), ci.getValue());
			}
		}
	}
	private <T> void fillListAndSet(List<T> theList, int pos, T value)
	{
		while(theList.size()<pos+1)
			theList.add(null);
		theList.set(pos, value);
	}
	

	@Override
	public void copyTo(ConnectorDO dataObject) {
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,ConnectorInfoDO> connectors = new HashMap<String,ConnectorInfoDO>();
		for(ConnectorInfoDO ni : dataObject.getConnectorInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			connectors.put(ni.getName(), ni);
		}
		
		Set<ConnectorInfoDO> finalConnectors = new HashSet<ConnectorInfoDO>();
		for(int loop=0; loop<entryTypes.size(); loop++)
		{
			if(entryTypes.get(loop)==null)
				continue;
			if(connectors.containsKey(INFO_PROPERTY_ENTRY_POINT_TYPE+"["+loop+"]"))
			{
				connectors.get(INFO_PROPERTY_ENTRY_POINT_TYPE+"["+loop+"]").setValue(entryTypes.get(loop).toString());
				finalConnectors.add(connectors.get(INFO_PROPERTY_ENTRY_POINT_TYPE+"["+loop+"]"));
			}
			else
			{
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_ENTRY_POINT_TYPE+"["+loop+"]", entryTypes.get(loop).toString(), version);
			}
		}
		for(int loop=0; loop<entryPoints.size(); loop++)
		{
			if(entryPoints.get(loop)==null)
				continue;
			if(connectors.containsKey(INFO_PROPERTY_ENTRY_POINT+"["+loop+"]"))
			{
				connectors.get(INFO_PROPERTY_ENTRY_POINT+"["+loop+"]").setValue(entryPoints.get(loop));
				finalConnectors.add(connectors.get(INFO_PROPERTY_ENTRY_POINT+"["+loop+"]"));
			}
			else
			{
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_ENTRY_POINT+"["+loop+"]", entryPoints.get(loop), version);
			}
		}
		for(int loop=0; loop<keywords.size(); loop++)
		{
			if(keywords.get(loop)==null)
				continue;
			if(connectors.containsKey(INFO_PROPERTY_CONNECTOR_KEYWORD+"["+loop+"]"))
			{
				connectors.get(INFO_PROPERTY_CONNECTOR_KEYWORD+"["+loop+"]").setValue(keywords.get(loop));
				finalConnectors.add(connectors.get(INFO_PROPERTY_CONNECTOR_KEYWORD+"["+loop+"]"));
			}
			else
			{
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_CONNECTOR_KEYWORD+"["+loop+"]", keywords.get(loop), version);
			}
		}
		Set<ConnectorInfoDO> removeConnectors = new HashSet<ConnectorInfoDO>();
		removeConnectors.addAll(connectors.values());
		removeConnectors.removeAll(finalConnectors);
		dataObject.getConnectorInfo().removeAll(removeConnectors);
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

	public EntryPointType getEntryPointType() {
		if(entryTypes!=null && entryTypes.size()==1)
			return entryTypes.get(0);
		return null;
	}

	public void setEntryPointType(EntryPointType entryType) {
		entryTypes.clear();
		entryTypes.add(entryType);
	}

	public String getEntryPoint() {
		if(entryPoints!=null && entryPoints.size()==1)
			return entryPoints.get(0);
		return null;
	}

	public void setEntryPoint(String entryPoint) {
		entryPoints.clear();
		entryPoints.add(entryPoint);
	}

	public String getKeyword() {
		if(keywords!=null && keywords.size()==1)
			return keywords.get(0);
		return null;
	}

	public void setKeyword(String keyword) {
		keywords.clear();
		keywords.add(keyword);
	}
	
	public EntryData[] getEntryData()
	{
		int maxSize = entryTypes.size();
		maxSize = entryPoints.size() > maxSize ? entryPoints.size() : maxSize;
		maxSize = keywords.size() > maxSize ? keywords.size() : maxSize;
		EntryData[] ret = new EntryData[maxSize];
		for(int loop=0; loop<maxSize; loop++)
		{
			ret[loop]=new EntryData();
			ret[loop].setEntryType(entryTypes.size()>loop?entryTypes.get(loop):null);
			ret[loop].setEntryPoint(entryPoints.size()>loop?entryPoints.get(loop):null);
			ret[loop].setKeyword(keywords.size()>loop?keywords.get(loop):null);
		}
		return ret;
	}
	
	public void setEntryData(EntryData[] entries)
	{
		entryTypes.clear();
		entryPoints.clear();
		keywords.clear();
		for(EntryData data : entries)
		{
			entryTypes.add(data.getEntryType());
			entryPoints.add(data.getEntryPoint());
			keywords.add(data.getKeyword());
		}
	}
}
