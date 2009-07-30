package com.digitalbarista.cat.business;

import com.digitalbarista.cat.data.NodeType;

public class TerminationNode extends Node {

	@Override
	public NodeType getType() {
		return NodeType.Termination;
	}

}
