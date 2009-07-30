package com.digitalbarista.cat.ejb.session;

import java.util.Date;

import com.digitalbarista.cat.message.event.CATEventType;

public interface EventTimerManager {

	public void setTimer(String uid, String target, CATEventType type, Date scheduledDate);
	public Date getNextEventTime();
	public void fireOverdueEvents();
	
}
