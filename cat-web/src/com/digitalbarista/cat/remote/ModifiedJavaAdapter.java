package com.digitalbarista.cat.remote;

import org.apache.log4j.LogManager;

import flex.messaging.messages.Message;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class ModifiedJavaAdapter extends JavaAdapter {

	@Override
	public Object invoke(Message message) {
		try
		{
			return super.invoke(message);
		}catch(RuntimeException t){
			LogManager.getLogger(getClass()).error("An error was thrown while invoking a BlazeDS Remote object:",t);
			throw t;
		}
	}

}
