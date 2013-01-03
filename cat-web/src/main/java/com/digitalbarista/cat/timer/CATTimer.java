package com.digitalbarista.cat.timer;

import java.util.Date;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import com.digitalbarista.cat.ejb.session.EventTimerManager;

public class CATTimer implements Runnable,Synchronization {

	private static Thread timerThread=null;
	private static boolean running = false;
	private static Object monitor = new Object();
	private static CATTimer instance=null;
	
	private static final String ejbName="ejb/cat/EventTimerManager";
	
	private CATTimer(){}
	
	@Override
	public void run() {
//		if(Thread.currentThread()!=timerThread)
//			throw new IllegalStateException("The CATTimer may only be started from the start() method.  Check your code.");
//		while(running)
//		{
//			try
//			{
//				synchronized(monitor)
//				{
//					InitialContext ic = new InitialContext();
//					EventTimerManager man = (EventTimerManager)ic.lookup(ejbName);
//					if(man==null)
//					{
//						monitor.wait();
//						continue;
//					}
//					man.fireOverdueEvents();
//					Date wakeTime=man.getNextEventTime();
//					if(wakeTime==null)
//					{
//						monitor.wait();
//						continue;
//					}
//					Date now = new Date();
//					if(now.after(wakeTime))
//						continue;
//					monitor.wait(wakeTime.getTime()-now.getTime());
//				}
//			}
//			catch(InterruptedException e)
//			{
//				continue;
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				try
//				{
//					synchronized(monitor)
//					{
//						monitor.wait(60000);
//					}
//				}catch(InterruptedException ex){}
//				continue;
//			}
//		}
	}
	
	public static void stop()
	{
		running=false;
		timerThread=null;
		instance=null;
	}

	public static void start()
	{
		if(timerThread!=null && timerThread.isAlive())
			throw new IllegalStateException("An active timer thread is already running.");
		instance = new CATTimer();
		timerThread=new Thread(instance);
		running=true;
		timerThread.setDaemon(true);
		timerThread.start();
	}
	
	public static void eventScheduled(Transaction tx)
	{
		if(instance==null)
			throw new IllegalStateException("No instance to register for synchronization.");
		try
		{
			if(tx!=null && isActiveStatusCode(tx.getStatus()))
			{
				tx.registerSynchronization(instance);
				return;
			}
		}catch(Exception e){}
		wakeThread();
	}
	
	private static boolean isActiveStatusCode(int statusCode)
	{
		switch(statusCode)
		{
			case Status.STATUS_ACTIVE:
			case Status.STATUS_COMMITTED:
			case Status.STATUS_COMMITTING:
			case Status.STATUS_PREPARED:
			case Status.STATUS_PREPARING:
				return true;
				
			default:
				return false;
		}
	}
	
	private static void wakeThread()
	{
		synchronized(monitor)
		{
			monitor.notifyAll();
		}
	}

	@Override
	public void afterCompletion(int status) {
		switch(status)
		{
			case Status.STATUS_UNKNOWN:
			case Status.STATUS_ROLLING_BACK:
			case Status.STATUS_ROLLEDBACK:
			case Status.STATUS_MARKED_ROLLBACK:
				return;
			default:
				wakeThread();
		}
	}

	public void beforeCompletion() {}
}
