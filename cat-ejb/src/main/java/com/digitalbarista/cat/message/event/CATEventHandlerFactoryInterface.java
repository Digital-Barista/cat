package com.digitalbarista.cat.message.event;

import javax.ejb.Local;

@Local
public interface CATEventHandlerFactoryInterface {
	public void processEvent(CATEvent e);
}
