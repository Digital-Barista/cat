package com.digitalbarista.cat.remote;

import javax.persistence.EntityExistsException;

import org.apache.log4j.LogManager;

import flex.messaging.MessageException;
import flex.messaging.messages.Message;
import flex.messaging.services.remoting.adapters.JavaAdapter;

public class ErrorLoggingJavaAdapter extends JavaAdapter {

	@Override
	public Object invoke(Message message) {
		try
		{
			return super.invoke(message);
		}catch(MessageException e)
		{
			if(!(e.getCause().getCause() instanceof EntityExistsException))
			{
				LogManager.getLogger(getClass()).error("An exception was swallowed by Blaze.  Here it is:",e);
				throw e;
			}
			LogManager.getLogger(getClass()).warn("We're gonna try this one more time:",e);
			try
			{
				return super.invoke(message);
			}catch(RuntimeException e2)
			{
				LogManager.getLogger(getClass()).error("An exception was swallowed by Blaze.  Here it is:",e2);
				throw e2;
			}
		}catch(RuntimeException e)
		{
			LogManager.getLogger(getClass()).error("An exception was swallowed by Blaze.  Here it is:",e);
			throw e;
		}
	}
}
