package com.digitalbarista.cat.business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;

import com.digitalbarista.cat.audit.Auditable;
import com.digitalbarista.cat.data.ConnectorDO;
import com.digitalbarista.cat.data.ConnectorInfoDO;
import com.digitalbarista.cat.data.ConnectorType;

public class CalendarConnector extends Connector implements Auditable {

	public static final String INFO_PROPERTY_TARGET_DATE="TargetDate";
	private static String dateFormat = "MM/dd/yyyy HH:mm:ss";
	
	private Date targetDate;
	
	@Override
	public void copyFrom(ConnectorDO dataObject, Integer version) {
		super.copyFrom(dataObject, version);
		for(ConnectorInfoDO ci : dataObject.getConnectorInfo())
		{
			if(!ci.getVersion().equals(version))
				continue;
			
			try
			{
				if(ci.getName().equals(INFO_PROPERTY_TARGET_DATE))
					targetDate = new SimpleDateFormat(dateFormat).parse(ci.getValue());
				else if(ci.getName().equals(INFO_PROPERTY_TARGET_DATE))
					targetDate = new SimpleDateFormat(dateFormat).parse(ci.getValue());
			}
			catch(ParseException e)
			{
				LogManager.getLogger(getClass()).error("Error parsing the date stored in the database.  '"+ci.getValue()+"'");
			}
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
		
		if(targetDate!=null)
		{
			if(nodes.containsKey(INFO_PROPERTY_TARGET_DATE))
				nodes.get(INFO_PROPERTY_TARGET_DATE).setValue(new SimpleDateFormat(dateFormat).format(targetDate));
			else
				buildAndAddConnectorInfo(dataObject, INFO_PROPERTY_TARGET_DATE, new SimpleDateFormat(dateFormat).format(targetDate), version);
		}
	}

	@Override
	public ConnectorType getType() {
		return ConnectorType.Calendar;
	}

	public Date getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(Date targetDate) {
		this.targetDate = targetDate;
	}

	@Override
	public String auditString() {
		StringBuffer ret = new StringBuffer();
		ret.append("type:"+getType().toString());
		if(getTargetDate()!=null)
			ret.append(";targetDate:"+new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(getTargetDate()));
		else
			ret.append(";targetDate:null");
		ret.append(";name:"+getName());
		ret.append(";UID:"+getUid());
		ret.append(";source:"+getSourceNodeUID());
		ret.append(";dest:"+getDestinationUID());
		ret.append(";campaign:"+getCampaignUID());
		return ret.toString();
	}

}
