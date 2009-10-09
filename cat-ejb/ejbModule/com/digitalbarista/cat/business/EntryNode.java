package com.digitalbarista.cat.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;

public class EntryNode extends Node implements Auditable{

	public static final String INFO_PROPERTY_ENTRY_TYPE="EntryType";
	public static final String INFO_PROPERTY_ENTRY_POINT="EntryPointDO";
	public static final String INFO_PROPERTY_KEYWORD="EntryKeyword";
	
	private List<EntryPointType> entryTypes = new ArrayList<EntryPointType>();
	private List<String> entryPoints = new ArrayList<String>();
	private List<String> keywords = new ArrayList<String>();
	
	@Override
	public void copyFrom(NodeDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
						
			if(ni.getName().equals(INFO_PROPERTY_ENTRY_POINT))
			{
				entryPoints.clear();
				entryPoints.add(ni.getValue());
			}
			else if(ni.getName().equals(INFO_PROPERTY_ENTRY_TYPE))
			{
				entryTypes.clear();
				entryTypes.add(EntryPointType.valueOf(ni.getValue()));
			}
			else if(ni.getName().equals(INFO_PROPERTY_KEYWORD))
			{
				keywords.clear();
				keywords.add(ni.getValue());
			}
			else if(ni.getName().startsWith(INFO_PROPERTY_ENTRY_POINT+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_ENTRY_POINT+"\\[([\\d]+)\\]").matcher(ni.getName());
				r.matches();
				fillListAndSet(entryPoints,new Integer(r.group(1)), ni.getValue());
			}
			else if(ni.getName().startsWith(INFO_PROPERTY_ENTRY_TYPE+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_ENTRY_TYPE+"\\[([\\d]+)\\]").matcher(ni.getName());
				r.matches();
				fillListAndSet(entryTypes,new Integer(r.group(1)), EntryPointType.valueOf(ni.getValue()));
			}
			else if(ni.getName().startsWith(INFO_PROPERTY_KEYWORD+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_KEYWORD+"\\[([\\d]+)\\]").matcher(ni.getName());
				r.matches();
				fillListAndSet(keywords,new Integer(r.group(1)), ni.getValue());
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
		
		Set<NodeInfoDO> finalNodes = new HashSet<NodeInfoDO>();
		for(int loop=0; loop<entryTypes.size(); loop++)
		{
			if(entryTypes.get(loop)==null)
				continue;
			if(nodes.containsKey(INFO_PROPERTY_ENTRY_TYPE+"["+loop+"]"))
			{
				nodes.get(INFO_PROPERTY_ENTRY_TYPE+"["+loop+"]").setValue(entryTypes.get(loop).toString());
				finalNodes.add(nodes.get(INFO_PROPERTY_ENTRY_TYPE+"["+loop+"]"));
			}
			else
			{
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_ENTRY_TYPE+"["+loop+"]", entryTypes.get(loop).toString(), version);
			}
		}
		for(int loop=0; loop<entryPoints.size(); loop++)
		{
			if(entryPoints.get(loop)==null)
				continue;
			if(nodes.containsKey(INFO_PROPERTY_ENTRY_POINT+"["+loop+"]"))
			{
				nodes.get(INFO_PROPERTY_ENTRY_POINT+"["+loop+"]").setValue(entryPoints.get(loop));
				finalNodes.add(nodes.get(INFO_PROPERTY_ENTRY_POINT+"["+loop+"]"));
			}
			else
			{
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_ENTRY_POINT+"["+loop+"]", entryPoints.get(loop), version);
			}
		}
		for(int loop=0; loop<keywords.size(); loop++)
		{
			if(keywords.get(loop)==null)
				continue;
			if(nodes.containsKey(INFO_PROPERTY_KEYWORD+"["+loop+"]"))
			{
				nodes.get(INFO_PROPERTY_KEYWORD+"["+loop+"]").setValue(keywords.get(loop));
				finalNodes.add(nodes.get(INFO_PROPERTY_KEYWORD+"["+loop+"]"));
			}
			else
			{
				buildAndAddNodeInfo(dataObject, INFO_PROPERTY_KEYWORD+"["+loop+"]", keywords.get(loop), version);
			}
		}
		Set<NodeInfoDO> removeNodes = new HashSet<NodeInfoDO>();
		removeNodes.addAll(nodes.values());
		removeNodes.removeAll(finalNodes);
		dataObject.getNodeInfo().removeAll(removeNodes);
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
		if(entryTypes!=null && entryTypes.size()==1)
			return entryTypes.get(0);
		return null;
	}

	public void setEntryType(EntryPointType entryType) {
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

	public String[] getKeywords() {
		return keywords.toArray(new String[keywords.size()]);
	}

	public void setKeywords(String[] keywords) {
		this.keywords = new ArrayList<String>();
		for(String item : keywords)
			this.keywords.add(item);
	}


	public EntryPointType[] getEntryTypeEnums() {
		return entryTypes.toArray(new EntryPointType[entryTypes.size()]);
	}

	public void setEntryTypeEnums(EntryPointType[] entryTypes) {
		this.entryTypes = new ArrayList<EntryPointType>();
		for(EntryPointType item : entryTypes)
			this.entryTypes.add(item);
	}

	
	public String[] getEntryTypes()
	{
		String[] ret = new String[entryTypes.size()];
		for(int loop=0; loop<entryTypes.size(); loop++)
			ret[loop]=entryTypes.get(loop).toString();
		return ret;
	}
	
	public void setEntryTypes(String[] entryTypes)
	{
		this.entryTypes = new ArrayList<EntryPointType>();
		for(String type : entryTypes)
			this.entryTypes.add(EntryPointType.valueOf(type));
	}

	public String[] getEntryPoints() {
		return entryPoints.toArray(new String[entryPoints.size()]);
	}

	public void setEntryPoints(String[] entryPoints) {
		this.entryPoints = new ArrayList<String>();
		for(String item : entryPoints)
			this.entryPoints.add(item);
	}

}
