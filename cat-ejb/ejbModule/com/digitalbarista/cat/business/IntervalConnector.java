package com.digitalbarista.cat.business;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;

public class IntervalConnector extends Connector implements Auditable {

	public static final String INFO_PROPERTY_INTERVAL="Interval";
	public static final String INFO_PROPERTY_INTERVAL_TYPE="IntervalType";
	
	private Long interval;
	private IntervalType intervalType;
	
	@Override
	public void copyFrom(ConnectorDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(ConnectorInfoDO ci : dataObject.getConnectorInfo())
		{
			if(!ci.getVersion().equals(version))
				continue;
			
			if(ci.getName().equals(INFO_PROPERTY_INTERVAL))
				interval = new Long(ci.getValue());
			else if(ci.getName().equals(INFO_PROPERTY_INTERVAL))
				interval = new Long(ci.getValue());
			
			if(ci.getName().equals(INFO_PROPERTY_INTERVAL_TYPE))
				intervalType = IntervalType.valueOf(ci.getValue());
			else if(ci.getName().equals(INFO_PROPERTY_INTERVAL_TYPE))
				intervalType = IntervalType.valueOf(ci.getValue());
		}
	}

	@Override
	public void copyTo(ConnectorDO dataObject) {
		super.copyTo(dataObject);
		Integer version = dataObject.getCampaign().getCurrentVersion();
		Map<String,ConnectorInfoDO> nodes = new HashMap<String,ConnectorInfoDO>();
		for(ConnectorInfoDO ci : dataObject.getConnectorInfo())
		{
			if(!ci.getVersion().equals(version))
				continue;
			nodes.put(ci.getName(), ci);
		}
		
		if(interval!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_INTERVAL))
				nodes.get(INFO_PROPERTY_INTERVAL).setValue(interval.toString());
			else
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_INTERVAL, interval.toString(), version);
		}
		
		if(intervalType!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_INTERVAL_TYPE))
				nodes.get(INFO_PROPERTY_INTERVAL_TYPE).setValue(intervalType.toString());
			else
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_INTERVAL_TYPE, intervalType.toString(), version);
		}
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		if(getIntervalType()!=null)
			ret.append(";intervalType:"+getIntervalType().toString());
		ret.append(";interval:"+getInterval());
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";source:"+getSourceNodeUID());
		ret.append(";dest:"+getDestinationUID());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}
	
	@Override
	public ConnectorType getType() {
		return ConnectorType.Interval;
	}

	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	public IntervalType getIntervalType() {
		return intervalType;
	}

	public void setIntervalType(IntervalType intervalType) {
		this.intervalType = intervalType;
	}

}
