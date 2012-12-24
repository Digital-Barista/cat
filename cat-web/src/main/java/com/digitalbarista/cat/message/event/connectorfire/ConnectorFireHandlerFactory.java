package com.digitalbarista.cat.message.event.connectorfire;



import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.NodeType;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.message.event.CATEvent;
import javax.naming.OperationNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ConnectorFireHandlerFactory {

  @Autowired
  private ApplicationContext ctx;
  
	private ConnectorFireHandler getHandler(NodeType type)
	{
		switch(type)
		{
			case Entry:
			case OutgoingEntry:
        throw new IllegalStateException("ConnectorDO cannot have a entry node for a destination.");
			
			case Termination:
        throw new RuntimeException(new OperationNotSupportedException("Haven't built this type of node yet."));
				
			case Message:
				return ctx.getBean(MessageNodeFireHandler.class);
				
			case Tagging:
				return new TaggingNodeFireHandler();
				
			case Coupon:
				return new CouponNodeFireHandler();
				
			default:
				throw new IllegalArgumentException("Invalid Node type for connector fired event: "+type.toString());
		}
	}
	
	public void handle(NodeType type, Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e)
  {
    getHandler(type).handle(conn, dest, version, s, e);
  }
}
