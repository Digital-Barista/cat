package com.digitalbarista.cat.remote;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import flex.messaging.log.AbstractTarget;
import flex.messaging.log.LogEvent;

public class Log4JTarget extends AbstractTarget {

	private static Logger log = LogManager.getLogger("BlazeDS");
	
	@Override
	public void logEvent(LogEvent event) {
		Level l;
		switch(event.level)
		{
			case LogEvent.INFO:
				l=Level.INFO;
				break;
			case LogEvent.WARN:
				l=Level.WARN;
				break;
			case LogEvent.ERROR:
				l=Level.ERROR;
				break;
			case LogEvent.FATAL:
				l=Level.FATAL;
				break;
			default:
				l=Level.DEBUG;
		}
		if(log.isEnabledFor(l))
		{
			if(event.throwable==null)
			{
				log.log(l, event.message);
			}else{
				log.log(l, event.message, event.throwable);
			}
		}
	}

}
