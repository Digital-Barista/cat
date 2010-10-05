package com.digitalbarista.cat.remote;

import org.apache.log4j.LogManager;

import flex.messaging.messages.Message;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class ErrorLoggingJavaAdapter extends JavaAdapter {

	@Override
	public Object invoke(Message message) {
		try
		{
			return super.invoke(message);
		}catch(RuntimeException e)
		{
			LogManager.getLogger(getClass()).error("An exception was swallowed by Blaze.  Here it is:",e);
			throw e;
		}
	}
}
