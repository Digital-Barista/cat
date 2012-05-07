package com.digitalbarista.cat.timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CATTimerStartupListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		CATTimer.stop();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		CATTimer.start();
	}

}
