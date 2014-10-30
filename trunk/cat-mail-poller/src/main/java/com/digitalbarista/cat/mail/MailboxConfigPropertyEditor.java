package com.digitalbarista.cat.mail;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;

import com.digitalbarista.cat.mail.MailboxConfig.MailboxType;

public class MailboxConfigPropertyEditor extends PropertyEditorSupport
{

	@Override
	public String getAsText() 
	{
		if(!(getValue() instanceof MailboxConfig))
			throw new IllegalArgumentException("Cannot convert object of type OTHER than MailboxConfig.");
		MailboxConfig val = (MailboxConfig)getValue();
		
		Map<String,String> props = new HashMap<String,String>();
		props.put("hostName", val.getHostName());
		props.put("port", ""+val.getPort());
		props.put("folder", val.getFolder());
		props.put("username", val.getUsername());
		props.put("password", val.getPassword());
		props.put("type", val.getType().toString());
		props.put("interval", ""+val.getPollIntervalMillis());
		
		StringBuilder ret = new StringBuilder();
		ret.append("[");
		String comma="";
		for(Map.Entry<String, String> entry : props.entrySet())
		{
			if(entry.getKey()==null || entry.getValue()==null)
				continue;
			ret.append(comma);
			ret.append(entry.getKey());
			ret.append("=");
			ret.append(entry.getValue());
			comma=",";
		}
		ret.append("]");
		return ret.toString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException 
	{
		Map<String,String> props = new HashMap<String,String>();
		String mapString;
		int eqPos;
		mapString=text.substring(text.indexOf("[")+1,text.lastIndexOf("]"));
		for(String prop : mapString.split(","))
		{
			eqPos=prop.indexOf("=");
			if(eqPos<=0)
				continue;
			if(eqPos==prop.length()-1)
				continue;
			props.put(prop.substring(0,eqPos), prop.substring(eqPos+1));
		}
		
		MailboxConfig val = new MailboxConfig();
		if(props.containsKey("hostName")) val.setHostName((String)props.get("hostName"));
		if(props.containsKey("port")) val.setPort(new Integer((String)props.get("port")));
		if(props.containsKey("folder")) val.setFolder((String)props.get("folder"));
		if(props.containsKey("username")) val.setUsername((String)props.get("username"));
		if(props.containsKey("password")) val.setPassword((String)props.get("password"));
		if(props.containsKey("type")) val.setType(MailboxType.valueOf((String)props.get("type")));
		if(props.containsKey("interval")) val.setPollIntervalMillis(new Long((String)props.get("interval")));
		if(val.getPort()==-1 && val.getType()!=null)
			val.setPort(val.getType().getPort());
		setValue(val);
	}

}
