package com.digitalbarista.cat.business;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.ConnectorType;

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
