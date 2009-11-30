package com.digitalbarista.cat.business;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;

@XmlRootElement(name="TaggingNode")
@XmlType(name="TaggingNode")
public class TaggingNode extends Node {

	private static final String INFO_PROPERTY_TAG="Tag";
	
	private List<String> tags;
	
	@Override
	public NodeType getType() {
		return NodeType.Tagging;
	}


	@Override
	public void copyFrom(NodeDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
						
			if(ni.getName().startsWith(INFO_PROPERTY_TAG+"["))
			{
				Matcher r = Pattern.compile(INFO_PROPERTY_TAG+"\\[([\\d]+)\\]").matcher(ni.getName());
				r.matches();
				fillListAndSet(tags,new Integer(r.group(1)), ni.getValue());
			}
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
		
		Set<NodeInfoDO> finalNodes = new HashSet<NodeInfoDO>();
		if (tags != null)
		{
			for(int loop=0; loop<tags.size(); loop++)
			{
				if(tags.get(loop)==null)
					continue;
				if(nodes.containsKey(INFO_PROPERTY_TAG+"["+loop+"]"))
				{
					nodes.get(INFO_PROPERTY_TAG+"["+loop+"]").setValue(tags.get(loop).toString());
					finalNodes.add(nodes.get(INFO_PROPERTY_TAG+"["+loop+"]"));
				}
				else
				{
					buildAndAddNodeInfo(dataObject, INFO_PROPERTY_TAG+"["+loop+"]", tags.get(loop).toString(), version);
				}
			}
		}
		Set<NodeInfoDO> removeNodes = new HashSet<NodeInfoDO>();
		removeNodes.addAll(nodes.values());
		removeNodes.removeAll(finalNodes);
		dataObject.getNodeInfo().removeAll(removeNodes);
	}

	private <T> void fillListAndSet(List<T> theList, int pos, T value)
	{
		while(theList.size()<pos+1)
			theList.add(null);
		theList.set(pos, value);
	}
	
	@XmlElementWrapper(name="Tags")
	@XmlElement(name="Tag")
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
