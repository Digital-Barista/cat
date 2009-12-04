package com.digitalbarista.cat.twitter.mbean;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.context.ApplicationContext;

import com.digitalbarista.cat.twitter.bindings.IdList;
import com.digitalbarista.cat.twitter.bindings.Tweeter;
import com.digitalbarista.cat.twitter.mbean.TwitterAccountPollManager.SubscribeAction;
import com.digitalbarista.cat.twitter.mbean.TwitterAccountPollManager.SubscribeType;

public class ModifySubscriptionsWorker extends TwitterPollWorker<String> {

	protected ModifySubscriptionsWorker(ApplicationContext ctx,
			TwitterAccountPollManager pm) {
		super(ctx, pm);
	}

	@Override
	public String call() throws Exception {
		TwitterAccountPollManager pm = getAccountPollManager();
		SubscribeAction sa = pm.getNextSubscribeAction();
		
		if(sa.getAction().equals(SubscribeType.NoAction))
			return "All subscription changes have been completed.";
		
		HttpClient client = null;
		PostMethod post = null;
		
		try
		{
			JAXBContext context = JAXBContext.newInstance(Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			TwitterAccountPollManager ps = getAccountPollManager();
			
			client = new HttpClient();
			
			post= new PostMethod("http://twitter.com/friendships/"+(sa.getAction().equals(SubscribeType.Subscribe)?"create":"destroy")+".xml");
			post.setQueryString(new NameValuePair[]{new NameValuePair("user_id",""+sa.getSubscriberId()),new NameValuePair("follow","true")});
			client.getParams().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
			
			if(client.executeMethod(post)!=200)
			{
				ps.subscribeChangeFailed();
				updateRateLimitInfo(post, ps);
				return "Failed to "+sa.getAction().toString()+" user #"+sa.getSubscriberId();
			}

			updateRateLimitInfo(post, ps);
			
			ps.registerSubscribeChange(sa);
			return "Successfully "+sa.getAction().toString()+"ed user #"+sa.getSubscriberId();
		}
		catch(Exception e)
		{
			log.error("Can't get friend list for account "+getAccountPollManager().getAccount(),e);
			getAccountPollManager().subscribeChangeFailed();
			return null;
		}
		finally
		{
			try{post.releaseConnection();}catch(Exception e){}
		}
		
	}

}
