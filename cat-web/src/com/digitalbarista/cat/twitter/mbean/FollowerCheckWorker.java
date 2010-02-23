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

import com.digitalbarista.cat.twitter.bindings.IdListNoCursor;

public class FollowerCheckWorker extends TwitterPollWorker<Set<Long>> {

	public FollowerCheckWorker(ApplicationContext ctx,TwitterAccountPollManager pollManager) {
		super(ctx,pollManager);
	}

	@Override
	public Set<Long> call() throws Exception {
		HttpClient client = null;
		GetMethod get = null;
		
		try
		{						
			JAXBContext context = JAXBContext.newInstance(IdListNoCursor.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			TwitterAccountPollManager ps = getAccountPollManager();
			
			client = new HttpClient();
			
			get = new GetMethod("http://www.twitter.com/followers/ids.xml");
			get.setQueryString("screen_name="+ps.getAccount());
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
			
			int response = client.executeMethod(get);
			if(response!=200)
			{
				updateRateLimitInfo(get, ps);
				throw new OperationFailedException("Could not check for followers.  response="+response);
			}
			
			updateRateLimitInfo(get, ps);
			
			IdListNoCursor idList = (IdListNoCursor)decoder.unmarshal(get.getResponseBodyAsStream());
			Set<Long> ret = new HashSet<Long>();
			if(idList.getIds()!=null)
				ret.addAll(idList.getIds());
			ps.registerFollowerList(ret);			
			return ret;
		}
		catch(Exception e)
		{
			log.error("Can't get friend list for account "+getAccountPollManager().getAccount(),e);
			getAccountPollManager().followerCheckFailed();
			return null;
		}
		finally
		{
			try{get.releaseConnection();}catch(Exception e){}
		}
	}
}
