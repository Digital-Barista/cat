package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.NodeType;

@XmlRootElement
public class TerminationNode extends Node {

	@Override
	public NodeType getType() {
		return NodeType.Termination;
	}

}
