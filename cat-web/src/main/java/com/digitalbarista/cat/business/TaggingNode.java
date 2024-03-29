package com.digitalbarista.cat.business;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.data.NodeDO;
import com.digitalbarista.cat.data.NodeInfoDO;
import com.digitalbarista.cat.data.NodeType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@XmlRootElement(name="TaggingNode")
@XmlType(name="TaggingNode")
public class TaggingNode extends Node {

	public static final String INFO_PROPERTY_TAG="Tag";
		
	private List<ContactTag> tags;
	
	@Override
	public NodeType getType() {
		return NodeType.Tagging;
	}

	@Override
	public void copyFrom(NodeDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);		
	}

	@Override
	public void copyTo(NodeDO dataObject) {
                Logger log = LogManager.getLogger(getClass());
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,NodeInfoDO> nodes = new HashMap<String,NodeInfoDO>();
		for(NodeInfoDO ni : dataObject.getNodeInfo())
		{
			if(!ni.getVersion().equals(version))
				continue;
			log.debug("sifting nodes : "+ni.getName()+":"+ni.getValue());
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
					log.debug("found existing node "+INFO_PROPERTY_TAG+"["+loop+"] setting value "+tags.get(loop).getContactTagId());
					nodes.get(INFO_PROPERTY_TAG+"["+loop+"]").setValue(""+tags.get(loop).getContactTagId());
					finalNodes.add(nodes.get(INFO_PROPERTY_TAG+"["+loop+"]"));
				}
				else
				{
					log.debug("no existing node "+INFO_PROPERTY_TAG+"["+loop+"] adding value "+tags.get(loop).getContactTagId());
					buildAndAddNodeInfo(dataObject, INFO_PROPERTY_TAG+"["+loop+"]", ""+tags.get(loop).getContactTagId(), version);
				}
			}
		}
		Set<NodeInfoDO> removeNodes = new HashSet<NodeInfoDO>();
		removeNodes.addAll(nodes.values());
		removeNodes.removeAll(finalNodes);
		for(NodeInfoDO ni : removeNodes)
			log.debug("need to remove node info "+ni.getName()+":"+ni.getValue());
		dataObject.getNodeInfo().removeAll(removeNodes);
	}

	private <T> void fillListAndSet(List<T> theList, int pos, T value)
	{
		while(theList.size()<pos+1)
			theList.add(null);
		theList.set(pos, value);
	}
	
	@XmlElementWrapper(name="ContactTags")
	@XmlElement(name="ContactTag")
	public List<ContactTag> getTags() {
		return tags;
	}

	public void setTags(List<ContactTag> tags) {
		this.tags = tags;
	}
}
