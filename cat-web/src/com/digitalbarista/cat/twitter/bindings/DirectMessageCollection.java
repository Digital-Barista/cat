package com.digitalbarista.cat.twitter.bindings;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="direct-messages")
public class DirectMessageCollection {

	private List<DirectMessage> directMessages;

	@XmlElement(name="direct_message")
	public List<DirectMessage> getDirectMessages() {
		return directMessages;
	}
	public void setDirectMessages(List<DirectMessage> directMessages) {
		this.directMessages = directMessages;
	}
}
