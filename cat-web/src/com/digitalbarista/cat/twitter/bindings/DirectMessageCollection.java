package com.digitalbarista.cat.twitter.bindings;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="direct_message")
public class DirectMessageCollection {

	private List<DirectMessage> directMessages;

	@XmlElement
	public List<DirectMessage> getDirectMessages() {
		return directMessages;
	}
	public void setDirectMessages(List<DirectMessage> directMessages) {
		this.directMessages = directMessages;
	}
}
