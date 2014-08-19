/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.message.event.connectorfire;

import com.digitalbarista.cat.business.Connector;
import com.digitalbarista.cat.business.Node;
import com.digitalbarista.cat.data.SubscriberDO;
import com.digitalbarista.cat.message.event.CATEvent;

/**
 *
 * @author Falken
 */
public interface ConnectorFireHandler {
	public abstract void handle(Connector conn, Node dest, Integer version, SubscriberDO s, CATEvent e);  
}
