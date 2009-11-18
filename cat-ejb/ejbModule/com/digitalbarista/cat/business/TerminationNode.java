package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.data.NodeType;

@XmlRootElement(name="TerminationNode")
@XmlType(name="TerminationNode")
public class TerminationNode extends Node {

	@Override
	public NodeType getType() {
		return NodeType.Termination;
	}

}
