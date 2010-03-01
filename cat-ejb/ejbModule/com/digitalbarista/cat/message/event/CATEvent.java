package com.digitalbarista.cat.message.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.digitalbarista.cat.data.EntryPointType;

public class CATEvent implements Serializable{
	private static final long serialVersionUID = 1L;
	private CATEventSource sourceType;
	private String source;
	private CATEventType type;
	private String target;
	private CATTargetType targetType;
	private Map<String,String> args=new HashMap<String,String>();
	
	private CATEvent(){}
	
	public static CATEvent buildIncomingTwitterEvent(String fromAddress, String toAddress, String subject)
	{
		CATEvent ret = new CATEvent();
		ret.source=toAddress;
		ret.sourceType=CATEventSource.TwitterEndpoint;
		ret.type=CATEventType.IncomingMessage;
		ret.target=fromAddress;
	    ret.targetType=CATTargetType.SpecificSubscriber;
	    ret.args.put("message", subject);
	    return ret;
	}
	
	public static CATEvent buildIncomingEmailEvent(String fromAddress, String toAddress, String subject)
	{
		CATEvent ret = new CATEvent();
		ret.source=toAddress;
		ret.sourceType=CATEventSource.EmailEndpoint;
		ret.type=CATEventType.IncomingMessage;
		ret.target=fromAddress;
	    ret.targetType=CATTargetType.SpecificSubscriber;
	    ret.args.put("subject", subject);
	    return ret;
	}
	
	public static CATEvent buildIncomingSMSEvent(String fromAddress, String toAddress, String message)
	{
		CATEvent ret = new CATEvent();
		ret.source=toAddress;
		ret.sourceType=CATEventSource.SMSEndpoint;
		ret.type=CATEventType.IncomingMessage;
		ret.target=fromAddress;
	    ret.targetType=CATTargetType.SpecificSubscriber;
	    ret.args.put("message", message);
	    return ret;
	}
	
	public static CATEvent buildIncomingTwitterDMEvent(String fromAddress, String toAddress, String message, String twitterID)
	{
		CATEvent ret = new CATEvent();
		ret.source=toAddress;
		ret.sourceType=CATEventSource.TwitterEndpoint;
		ret.type=CATEventType.IncomingMessage;
		ret.target=fromAddress;
	    ret.targetType=CATTargetType.SpecificSubscriber;
	    ret.args.put("message", message);
	    ret.args.put("twitterID", twitterID);
	    return ret;
	}
	
	public static CATEvent buildFireConnectorForSubscriberEvent(String connectorUID, String subscriberPK)
	{
		CATEvent ret = new CATEvent();
		ret.source=connectorUID;
		ret.sourceType=CATEventSource.Trigger;
		ret.type=CATEventType.ConnectorFired;
		ret.target=subscriberPK;
		ret.targetType=CATTargetType.SpecificSubscriber;
		return ret;
	}
	
	public static CATEvent buildFireConnectorForAllSubscribersEvent(String connectorUID)
	{
		CATEvent ret = new CATEvent();
		ret.source=connectorUID;
		ret.sourceType=CATEventSource.Trigger;
		ret.type=CATEventType.ConnectorFired;
		ret.targetType=CATTargetType.AllAppliedSubscribers;
		return ret;
	}
	
	public static CATEvent buildNodeOperationCompletedEvent(String nodeUID, String subscriberPK)
	{
		CATEvent ret = new CATEvent();
		ret.source=nodeUID;
		ret.sourceType=CATEventSource.Node;
		ret.type=CATEventType.NodeOperationCompleted;
		ret.targetType=CATTargetType.SpecificSubscriber;
		ret.target=subscriberPK;
		return ret;
	}

	public static CATEvent buildSendMessageRequestedEvent(String from, EntryPointType type, String to, String message, String subject, String nodeUID, Integer nodeVersion)
	{
		CATEvent ret = new CATEvent();
		ret.source=from;
		switch(type)
		{
			case Email:
				ret.sourceType=CATEventSource.EmailEndpoint;
				break;
				
			case SMS:
				ret.sourceType=CATEventSource.SMSEndpoint;
				break;
				
			case Twitter:
				ret.sourceType=CATEventSource.TwitterEndpoint;
				break;
		}
		ret.type=CATEventType.MessageSendRequested;
		ret.targetType=CATTargetType.SpecificSubscriber;
		ret.target=to;
		ret.args.put("subject", subject);
		ret.args.put("message", message);
		ret.args.put("nodeUID", nodeUID);
		ret.args.put("nodeVersion", (nodeVersion!=null)?nodeVersion.toString():"-1");
		return ret;
	}

	public CATEventSource getSourceType() {
		return sourceType;
	}
	public void setSourceType(CATEventSource sourceType) {
		this.sourceType = sourceType;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public CATEventType getType() {
		return type;
	}
	public void setType(CATEventType type) {
		this.type = type;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public CATTargetType getTargetType() {
		return targetType;
	}
	public void setTargetType(CATTargetType targetType) {
		this.targetType = targetType;
	}
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("source="+sourceType+":"+source);
		buf.append("type="+type);
		buf.append("target="+targetType+":"+target);
		return buf.toString();
	}
	public Map<String, String> getArgs() {
		return args;
	}
	public void setArgs(Map<String, String> args) {
		this.args = args;
	}
}
