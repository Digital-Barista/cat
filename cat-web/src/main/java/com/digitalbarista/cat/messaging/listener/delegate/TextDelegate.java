package com.digitalbarista.cat.messaging.listener.delegate;

public class TextDelegate {

	private Object lockObj;
	
	public TextDelegate(Object obj)
	{
		lockObj=obj;
	}
	
	public void handleMessage(String message) {
		synchronized(lockObj)
		{
			lockObj.notifyAll();
		}
	}
	
}
