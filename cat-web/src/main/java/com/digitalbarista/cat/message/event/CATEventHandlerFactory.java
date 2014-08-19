package com.digitalbarista.cat.message.event;

import javax.annotation.security.RunAs;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RunAs("admin")
public class CATEventHandlerFactory {

  @Autowired
  private ApplicationContext ctx;

	public void processEvent(CATEvent e) {
		getHandler(e.getType()).processEvent(e);
	}
	
	private CATEventHandler getHandler(CATEventType t)
	{
		switch(t)
		{
			case IncomingMessage:
				return ctx.getBean(IncomingMessageEventHandler.class);
			case NodeOperationCompleted:
				return ctx.getBean(NodeOperationCompletedEventHandler.class);
			case ConnectorFired:
				return ctx.getBean(ConnectorFiredEventHandler.class);
			case MessageSendRequested:
				return ctx.getBean(MessageSendRequestEventHandler.class);
      case UserSubscribed:
        return ctx.getBean(UserSubscribedEventHandler.class);
			default:
				throw new IllegalArgumentException("Unknown event type:  No configured factory: "+t.toString());
		}
	}
}
