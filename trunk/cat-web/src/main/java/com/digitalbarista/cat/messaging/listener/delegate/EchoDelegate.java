package com.digitalbarista.cat.messaging.listener.delegate;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.digitalbarista.cat.messaging.sender.JmsSender;

public class EchoDelegate {

	private JmsSender sender; 
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void handleMessage(byte[] message)
	{
		sender.send(message);
		System.err.println("Message '"+message+"'echoed at: "+System.currentTimeMillis());
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void handleMessage(String message)
	{
		sender.send(message);
		System.err.println("Message '"+message+"'echoed at: "+System.currentTimeMillis());
	}

	public void setSender(JmsSender sender) {
		this.sender = sender;
	}
}
