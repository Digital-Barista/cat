package com.digitalbarista.cat.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.MessageListener;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.digitalbarista.cat.messaging.listener.delegate.TextDelegate;
import com.digitalbarista.cat.messaging.sender.JmsSender;

public class MessageTester implements ApplicationContextAware {

	private TransactionTemplate txTemplate;
	private ApplicationContext applicationContext;
	private Destination startDestination;
	private Destination endDestination;
	private Integer lockInt = new Integer(1);
	
	private ConcurrentHashMap<String,Long> messageMap = new ConcurrentHashMap<String,Long>();
	
	public long fireSingle()
	{
		TextDelegate d = new TextDelegate(lockInt);
		DefaultMessageListenerContainer ctr = (DefaultMessageListenerContainer)applicationContext.getBean("messageContainerTemplate");
		MessageListener ml = new MessageListenerAdapter(d);
		ctr.setMessageListener(ml);
		long ret=-1;
		try
		{
			ctr.start();
			Thread.currentThread().sleep(1000);
			System.err.println("Container started @ "+System.currentTimeMillis());
			
			JmsSender sender = (JmsSender)applicationContext.getBean("jms.receiveQueueSender");
			
			long start=System.currentTimeMillis();
			synchronized(lockInt)
			{
				try
				{
					sender.send("test");
					System.err.println("Message sent @ "+System.currentTimeMillis());
					try
					{
						lockInt.wait();
					}catch(InterruptedException e){}
					System.err.println("Message echo received @ "+System.currentTimeMillis());
					ret = System.currentTimeMillis()-start;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.err.println("Stopping container @ "+System.currentTimeMillis());
			ctr.stop();
		}
		return ret;
	}

	public long fireMulti()
	{
		TextDelegate d = new TextDelegate(messageMap)
		{
			public void handleMessage(String message) {
				if(!messageMap.containsKey(message))
				{
					System.err.println("Junk message, removing.");
					return;
				}
				long val = messageMap.get(message)-System.currentTimeMillis();
				messageMap.replace(message, val);
				System.err.println("Received message "+message+" in "+messageMap.get(message)+" millis.");
			}			
		};
		
		DefaultMessageListenerContainer ctr = (DefaultMessageListenerContainer)applicationContext.getBean("messageContainerTemplate");
		MessageListener ml = new MessageListenerAdapter(d);
		ctr.setMessageListener(ml);
		try
		{
			ctr.start();
			Thread.currentThread().sleep(1000);
			System.err.println("Container started @ "+System.currentTimeMillis());
			
			JmsSender sender = (JmsSender)applicationContext.getBean("jms.receiveQueueSender");
			
			long start=System.currentTimeMillis();
			
			for(int loop=1; loop<=100; loop++)
			{
				messageMap.put("Msg "+loop, System.currentTimeMillis());
				sender.send("Msg "+loop);
				System.err.println("sent message : 'Msg "+loop+"'");
			}
			
			int numReceived=0;
			long totalTime=0;
			List<String> removeKeys=new ArrayList<String>();
			while(numReceived<100)
			{
				for(Map.Entry<String, Long> entry : messageMap.entrySet())
				{
					if(entry.getValue()<=0)
					{
						totalTime-=entry.getValue();
						removeKeys.add(entry.getKey());
						numReceived++;
						System.err.println("Processed message arrival: "+entry.getKey());
					}
				}
				for(String key : removeKeys)
					messageMap.remove(key);
				removeKeys.clear();
				Thread.currentThread().sleep(100);
			}
			return System.currentTimeMillis()-start;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.err.println("Stopping container @ "+System.currentTimeMillis());
			ctr.stop();
		}
		return -1;
	}

	public long fireTransactedMulti()
	{
		TextDelegate d = new TextDelegate(messageMap)
		{
			public void handleMessage(String message) {
				if(!messageMap.containsKey(message))
				{
					System.err.println("Junk message, removing.");
					return;
				}
				long val = messageMap.get(message)-System.currentTimeMillis();
				messageMap.replace(message, val);
				System.err.println("Received message "+message+" in "+messageMap.get(message)+" millis.");
			}			
		};
		
		DefaultMessageListenerContainer ctr = (DefaultMessageListenerContainer)applicationContext.getBean("messageContainerTemplate");
		MessageListener ml = new MessageListenerAdapter(d);
		ctr.setMessageListener(ml);
		try
		{
			ctr.start();
			Thread.currentThread().sleep(1000);
			System.err.println("Container started @ "+System.currentTimeMillis());
			
			final JmsSender sender = (JmsSender)applicationContext.getBean("jms.receiveQueueSender");
			
			long start=System.currentTimeMillis();
			
			txTemplate.execute(new TransactionCallback(){
				@Override
				public Object doInTransaction(TransactionStatus arg0) {
			
					for(int loop=1; loop<=100; loop++)
					{
						messageMap.put("Msg "+loop, System.currentTimeMillis());
						sender.send("Msg "+loop);
						System.err.println("sent message : 'Msg "+loop+"'");
					}
					return null;
					
				}
			});
			
			int numReceived=0;
			long totalTime=0;
			List<String> removeKeys=new ArrayList<String>();
			while(numReceived<100)
			{
				for(Map.Entry<String, Long> entry : messageMap.entrySet())
				{
					if(entry.getValue()<=0)
					{
						totalTime-=entry.getValue();
						removeKeys.add(entry.getKey());
						numReceived++;
						System.err.println("Processed message arrival: "+entry.getKey());
					}
				}
				for(String key : removeKeys)
					messageMap.remove(key);
				removeKeys.clear();
				Thread.currentThread().sleep(100);
			}
			return System.currentTimeMillis()-start;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.err.println("Stopping container @ "+System.currentTimeMillis());
			ctr.stop();
		}
		return -1;
	}

	public Destination getStartDestination() {
		return startDestination;
	}
	public void setStartDestination(Destination startDestination) {
		this.startDestination = startDestination;
	}
	public Destination getEndDestination() {
		return endDestination;
	}
	public void setEndDestination(Destination endDestination) {
		this.endDestination = endDestination;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		this.applicationContext = ctx;
	}
	
	public void setTransactionManager(PlatformTransactionManager txMan)
	{
		txTemplate = new TransactionTemplate(txMan);
	}
}
