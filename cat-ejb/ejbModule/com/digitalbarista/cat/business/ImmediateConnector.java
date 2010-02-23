package com.digitalbarista.cat.business;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.ConnectorType;

@XmlRootElement(name="ImmediateConnector")
@XmlType(name="ImmediateConnector")
public class ImmediateConnector extends Connector implements Auditable{

	@Override
	public ConnectorType getType() {
		return ConnectorType.Immediate;
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";source:"+getSourceNodeUID());
		ret.append(";dest:"+getDestinationUID());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}
}
