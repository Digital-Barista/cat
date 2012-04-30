package com.digitalbarista.cat.mail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MailPollManager{

	private List<MailboxConfig> mailboxes;
	private Map<MailboxConfig,ScheduledFuture> workers=new HashMap<MailboxConfig,ScheduledFuture>();
	private ScheduledThreadPoolExecutor executor;
	private String dest;
	private String cf;
	
	public MailPollManager(List<MailboxConfig> configs,String destination, String connFact)
	{
		mailboxes = configs;
		dest=destination;
		cf=connFact;
		Set<MailboxPollWorker> tempWorkers = new HashSet<MailboxPollWorker>();
		for(MailboxConfig config : configs)
		{
			tempWorkers.add(new MailboxPollWorker(config,dest,cf));
		}
		executor = new ScheduledThreadPoolExecutor(mailboxes.size());
		for(MailboxPollWorker worker : tempWorkers)
		{
			workers.put(worker.getMailbox(), executor.scheduleAtFixedRate(worker, 0, worker.getMailbox().getPollIntervalMillis(),TimeUnit.MILLISECONDS));
		}
	}
	
	public void signalConfigChange()
	{
		Set<MailboxConfig> configs = workers.keySet();
		Set<MailboxConfig> newConfig = new HashSet<MailboxConfig>(mailboxes);
		
		Set<MailboxConfig> diff = new HashSet<MailboxConfig>();
		diff.addAll(configs);
		diff.removeAll(newConfig);
		for(MailboxConfig mailbox : diff)
			workers.remove(mailbox).cancel(false);

		diff.clear();
		diff.addAll(newConfig);
		diff.removeAll(configs);
		MailboxPollWorker worker;
		for(MailboxConfig config : diff)
		{
			worker=new MailboxPollWorker(config,dest,cf);
			workers.put(config,executor.scheduleAtFixedRate(worker, 0, config.getPollIntervalMillis(), TimeUnit.MILLISECONDS));
		}
	}
	
	public void shutdown()
	{
		for(ScheduledFuture future : workers.values())
			future.cancel(true);
	}
}
