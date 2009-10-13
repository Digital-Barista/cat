package com.digitalbarista.cat.twitter.mbean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.digitalbarista.cat.twitter.bindings.DirectMessage;
import com.digitalbarista.cat.twitter.bindings.Tweeter;


public class TwitterPollCoordinator implements TwitterPollCoordinatorMBean {

	@Override
	public int checkMessages(String account) {
		int messageCount=0;
		try
		{
			JAXBContext context = JAXBContext.newInstance(DirectMessage.class,Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			HttpClient client = new HttpClient();
			GetMethod get = new GetMethod("http://www.twitter.com/direct_messages.xml");
			
		}catch(JAXBException e)
		{
			e.printStackTrace();
			return -1;
		}
		return messageCount;
	}

	@Override
	public void sendMessage(String account, String to, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteMesage(int id) {
		// TODO Auto-generated method stub
		
	}
	
}
