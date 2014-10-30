package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.NodeType;

@XmlRootElement(name="OutgoingEntryNode")
@XmlType(name="OutgoingEntryNode")
public class OutgoingEntryNode extends EntryNode implements Auditable
{

	@Override
	public NodeType getType() {
		return NodeType.OutgoingEntry;
	}
}
