package com.digitalbarista.cat.ejb.session;

import javax.ejb.Local;

import com.digitalbarista.cat.message.event.CATEvent;

@Local
public interface EventManager {
	public void queueEvent(CATEvent e);
}
