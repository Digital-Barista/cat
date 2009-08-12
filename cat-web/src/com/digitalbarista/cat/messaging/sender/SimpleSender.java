package com.digitalbarista.cat.messaging.sender;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SimpleSender implements JmsSender {
	private JmsTemplate jmsTemplate;
	private Destination destination;
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void send(final byte[] message) {
		jmsTemplate.send(destination, new MessageCreator(){

			@Override
			public Message createMessage(Session session) throws JMSException {
				BytesMessage m = session.createBytesMessage();
				m.writeBytes(message);
				return m;
			}
			
		});
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void send(final String message) {
		jmsTemplate.send(destination, new MessageCreator(){

			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(message);
			}
			
		});
	}

	@Override
	public void setConnectionFactory(ConnectionFactory fact) {
		jmsTemplate = new JmsTemplate(fact);
	}

	@Override
	public void setDestination(Destination dest) {
		destination = dest;
	}

}
