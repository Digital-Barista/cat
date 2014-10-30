package com.digitalbarista.cat.message.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.digitalbarista.cat.data.EntryPointType;

public class CATEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    private CATEventSource sourceType;
    private String source;
    private CATEventType type;
    private String target;
    private CATTargetType targetType;
    private Map<String, String> args = new HashMap<String, String>();

    private CATEvent() {
    }

    public static CATEvent buildIncomingTwitterEvent(String fromAddress, String toAddress, String subject) {
        CATEvent ret = new CATEvent();
        ret.source = toAddress;
        ret.sourceType = CATEventSource.TwitterEndpoint;
        ret.type = CATEventType.IncomingMessage;
        ret.target = fromAddress;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.args.put("message", subject);
        return ret;
    }

    public static CATEvent buildIncomingEmailEvent(String fromAddress, String toAddress, String subject) {
        CATEvent ret = new CATEvent();
        ret.source = toAddress;
        ret.sourceType = CATEventSource.EmailEndpoint;
        ret.type = CATEventType.IncomingMessage;
        ret.target = fromAddress;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.args.put("subject", subject);
        return ret;
    }

    public static CATEvent buildIncomingSMSEvent(String fromAddress, String toAddress, String message) {
        CATEvent ret = new CATEvent();
        ret.source = toAddress;
        ret.sourceType = CATEventSource.SMSEndpoint;
        ret.type = CATEventType.IncomingMessage;
        ret.target = fromAddress;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.args.put("message", message);
        return ret;
    }

    public static CATEvent buildIncomingTwitterDMEvent(String fromAddress, String toAddress, String message, String twitterID) {
        CATEvent ret = new CATEvent();
        ret.source = toAddress;
        ret.sourceType = CATEventSource.TwitterEndpoint;
        ret.type = CATEventType.IncomingMessage;
        ret.target = fromAddress;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.args.put("message", message);
        ret.args.put("twitterID", twitterID);
        return ret;
    }

    public static CATEvent buildIncomingFacebookEvent(String fromAddress, String toAddress, String message, String facebookID) {
        CATEvent ret = new CATEvent();
        ret.source = toAddress;
        ret.sourceType = CATEventSource.FacebookEndpoint;
        ret.type = CATEventType.IncomingMessage;
        ret.target = fromAddress;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.args.put("message", message);
        ret.args.put("facebookID", facebookID);
        return ret;
    }

    public static CATEvent buildSubscriptionRequestedEvent(String fromAddress, EntryPointType type, String entryPointUID) {
        CATEvent ret = new CATEvent();
        ret.source = fromAddress;
        switch (type) {
            case Email:
                ret.sourceType = CATEventSource.EmailEndpoint;
                break;
            case Facebook:
                ret.sourceType = CATEventSource.FacebookEndpoint;
                break;
            case Twitter:
                ret.sourceType = CATEventSource.TwitterEndpoint;
                break;
            case SMS:
                ret.sourceType = CATEventSource.SMSEndpoint;
                break;
        }
        ret.type = CATEventType.UserSubscribed;
        ret.target = entryPointUID;
        ret.targetType = CATTargetType.EntryNode;
        return ret;
    }

    public static CATEvent buildFireConnectorForSubscriberEvent(String connectorUID, String subscriberPK, Integer version) {
        CATEvent ret = new CATEvent();
        ret.source = connectorUID;
        ret.sourceType = CATEventSource.Trigger;
        ret.type = CATEventType.ConnectorFired;
        ret.target = subscriberPK;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.getArgs().put("version", "" + version);
        return ret;
    }

    public static CATEvent buildFireConnectorForAllSubscribersEvent(String connectorUID, Integer version) {
        CATEvent ret = new CATEvent();
        ret.source = connectorUID;
        ret.sourceType = CATEventSource.Trigger;
        ret.type = CATEventType.ConnectorFired;
        ret.targetType = CATTargetType.AllAppliedSubscribers;
        ret.getArgs().put("version", "" + version);
        return ret;
    }

    public static CATEvent buildNodeOperationCompletedEvent(String nodeUID, String subscriberPK) {
        CATEvent ret = new CATEvent();
        ret.source = nodeUID;
        ret.sourceType = CATEventSource.Node;
        ret.type = CATEventType.NodeOperationCompleted;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.target = subscriberPK;
        return ret;
    }

    public static CATEvent buildSendMessageRequestedEvent(String from, EntryPointType type, String to, String message, String subject, String nodeUID, Integer nodeVersion) {
        CATEvent ret = new CATEvent();
        ret.source = from;
        switch (type) {
            case Email:
                ret.sourceType = CATEventSource.EmailEndpoint;
                break;

            case SMS:
                ret.sourceType = CATEventSource.SMSEndpoint;
                break;

            case Twitter:
                ret.sourceType = CATEventSource.TwitterEndpoint;
                break;

            case Facebook:
                ret.sourceType = CATEventSource.FacebookEndpoint;
                break;
        }
        ret.type = CATEventType.MessageSendRequested;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.target = to;
        ret.args.put("subject", subject);
        ret.args.put("message", message);
        ret.args.put("nodeUID", nodeUID);
        ret.args.put("nodeVersion", (nodeVersion != null) ? nodeVersion.toString() : "-1");
        return ret;
    }
    
    public static CATEvent buildNotificationRequestedEvent(String from, String to) {
        CATEvent ret = new CATEvent();
        ret.source = from;
        ret.type = CATEventType.NotificationSendRequested;
        ret.targetType = CATTargetType.SpecificSubscriber;
        ret.target = to;
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
        buf.append("source=" + sourceType + ":" + source);
        buf.append("type=" + type);
        buf.append("target=" + targetType + ":" + target);
        return buf.toString();
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CATEvent)) {
            return false;
        }
        CATEvent e = (CATEvent) obj;
        if (!source.equals(e.source)) {
            return false;
        }
        if (!sourceType.equals(e.sourceType)) {
            return false;
        }
        if (!type.equals(e.type)) {
            return false;
        }
        if (!target.equals(e.target)) {
            return false;
        }
        if (!targetType.equals(e.targetType)) {
            return false;
        }
        if (args == null && e.args != null && e.args.size() > 0) {
            return false;
        }
        if (args != null && args.size() > 0 && e.args == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : args.entrySet()) {
            if (!e.args.containsKey(entry.getKey())) {
                return false;
            }
            if (!e.args.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
