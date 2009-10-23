package com.digitalbarista.cat.twitter.mbean;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.context.ApplicationContext;

import com.digitalbarista.cat.twitter.bindings.IdList;

public class FriendCheckWorker extends TwitterPollWorker<Set<Long>> {

	private String account;
	
	public FriendCheckWorker(ApplicationContext ctx,String account) {
		super(ctx);
		this.account = account;
	}

	@Override
	public Set<Long> call() throws Exception {
		HttpClient client = null;
		GetMethod get = null;
		
		try
		{
			String credentials = getCredentials(account);
						
			JAXBContext context = JAXBContext.newInstance(IdList.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			PollStats ps = getPollStats(account);
			
			client = new HttpClient();
			
			get = new GetMethod("http://www.twitter.com/friends/ids.xml");
			get.setQueryString("screen_name="+account);
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(account,credentials));
			
			client.executeMethod(get);

			updateRateLimitInfo(get, ps);
			
			IdList idList = (IdList)decoder.unmarshal(get.getResponseBodyAsStream());
			Set<Long> ret = new HashSet<Long>();
			if(idList.getIds()!=null)
				ret.addAll(idList.getIds());
			return ret;
		}
		catch(Exception e)
		{
			log.error("Can't get friend list for account "+account,e);
			return null;
		}
		finally
		{
			try{get.releaseConnection();}catch(Exception e){}
		}
	}
}
