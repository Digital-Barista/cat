package com.digitalbarista.cat.mail;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class DBIMailPollerService implements DBIMailPollerServiceMBean{
	
	private List<MailboxConfig> mailboxConfigs=new ArrayList<MailboxConfig>();
	private MailPollManager manager;
	private String destinationName;
	private String connectionFactoryName;
	
	public DBIMailPollerService()
	{
		PropertyEditorManager.registerEditor(MailboxConfig.class, MailboxConfigPropertyEditor.class);
	}
	
	public void start()
	{
		manager = new MailPollManager(mailboxConfigs,destinationName,connectionFactoryName);
	}
	
	public void stop()
	{
		manager.shutdown();
		manager=null;
	}

	public List<MailboxConfig> getMailboxConfigs() {
		return mailboxConfigs;
	}

	public String getMailboxConfigString()
	{
		String ret="{";
		String comma="";
		PropertyEditor pe = PropertyEditorManager.findEditor(MailboxConfig.class);
		int index=0;
		for(MailboxConfig mailbox : mailboxConfigs)
		{
			ret+=comma+(index++)+":";
			pe.setValue(mailbox);
			ret+=pe.getAsText();
			comma=",\n";
		}
		ret+="}";
		return ret;
	}
	
	public void removeMailbox(int index)
	{
		mailboxConfigs.remove(index);
		manager.signalConfigChange();
	}
	
	public void addMailbox(MailboxConfig mbConfig)
	{
		mailboxConfigs.add(mbConfig);
		manager.signalConfigChange();
	}
	
	public void setMailboxConfigs(Element mailboxConfigsElement) {
		if(mailboxConfigsElement==null)
			return;
		NodeList mailboxNodes = mailboxConfigsElement.getElementsByTagName("mailbox");
		MailboxConfig mailbox;
		mailboxConfigs.clear();
		for(int boxIndex=0; boxIndex<mailboxNodes.getLength(); boxIndex++)
		{
			mailbox=new MailboxConfig();
			NamedNodeMap mailboxProps=mailboxNodes.item(boxIndex).getAttributes();
			if(mailboxProps.getNamedItem("hostName")!=null)
			{
				mailbox.setHostName(mailboxProps.getNamedItem("hostName").getTextContent());
			}
			if(mailboxProps.getNamedItem("port")!=null)
			{
				mailbox.setPort(new Integer(mailboxProps.getNamedItem("port").getTextContent()));
			}
			if(mailboxProps.getNamedItem("username")!=null)
			{
				mailbox.setUsername(mailboxProps.getNamedItem("username").getTextContent());
			}
			if(mailboxProps.getNamedItem("password")!=null)
			{
				mailbox.setPassword(mailboxProps.getNamedItem("password").getTextContent());
			}
			if(mailboxProps.getNamedItem("folder")!=null)
			{
				mailbox.setFolder(mailboxProps.getNamedItem("folder").getTextContent());
			}
			if(mailboxProps.getNamedItem("type")!=null)
			{
				mailbox.setType(MailboxConfig.MailboxType.valueOf(mailboxProps.getNamedItem("type").getTextContent()));
			}
			if(mailboxProps.getNamedItem("interval")!=null)
			{
				mailbox.setPollIntervalMillis(new Long(mailboxProps.getNamedItem("interval").getTextContent()));
			}
			if(mailbox.getPort()==-1 && mailbox.getType()!=null)
				mailbox.setPort(mailbox.getType().getPort());
			mailboxConfigs.add(mailbox);
		}
		if(manager!=null)
			manager.signalConfigChange();
	}

	public void setDestinationJNDIName(String dName) {
		this.destinationName = dName;
	}

	public void setConnectionFactoryJNDIName(String cfName) {
		this.connectionFactoryName = cfName;
	}
}
