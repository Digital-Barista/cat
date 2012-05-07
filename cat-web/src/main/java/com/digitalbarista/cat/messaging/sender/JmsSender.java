package com.digitalbarista.cat.messaging.sender;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

public interface JmsSender {

	public void setDestination(Destination dest);
	public void setConnectionFactory(ConnectionFactory cFact);
	public void send(byte[] message);
	public void send(String message);
	
}
