package com.digitalbarista.cat.mail;

import org.w3c.dom.Element;

public interface DBIMailPollerServiceMBean {
	public void start();
	public void stop();
	public String getMailboxConfigString();
	public void setDestinationJNDIName(String dest);
	public void setConnectionFactoryJNDIName(String cf);
	public void setMailboxConfigs(Element configs);
	public void removeMailbox(int index);
	public void addMailbox(MailboxConfig mbConfig);
}
