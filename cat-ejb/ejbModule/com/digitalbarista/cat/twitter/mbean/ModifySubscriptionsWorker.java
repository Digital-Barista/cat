package com.digitalbarista.cat.twitter.mbean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;

import com.digitalbarista.cat.twitter.bindings.Tweeter;
import com.digitalbarista.cat.twitter.mbean.TwitterAccountPollManager.SubscribeAction;
import com.digitalbarista.cat.twitter.mbean.TwitterAccountPollManager.SubscribeType;

public class ModifySubscriptionsWorker extends TwitterPollWorker<String> {

	protected ModifySubscriptionsWorker(TwitterAccountPollManager pm) {
		super(pm);
	}

	@Override
	public String call() throws Exception {
		TwitterAccountPollManager pm = getAccountPollManager();
		SubscribeAction sa = pm.getNextSubscribeAction();
		
		if(sa.getAction().equals(SubscribeType.NoAction))
			return "All subscription changes have been completed.";
		
		DefaultHttpClient client = null;
		HttpUriRequest post = null;
		
		try
		{
			SecurityAssociation.pushRunAsIdentity(new RunAsIdentity("admin","admin"));
			JAXBContext context = JAXBContext.newInstance(Tweeter.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			TwitterAccountPollManager ps = getAccountPollManager();
			
			client = new DefaultHttpClient();
			
			if(sa.getAction().equals(SubscribeType.Subscribe))
				post=new HttpPost("http://api.twitter.com/1/friendships/create/"+sa.getSubscriberId()+".xml");
			else
				post=new HttpDelete("http://api.twitter.com/1/friendships/destroy/"+sa.getSubscriberId()+".xml");
			post.getParams().setParameter("user_id",""+sa.getSubscriberId());
			if(sa.getAction().equals(SubscribeType.Subscribe))
				post.getParams().setParameter("follow","true");

			if(ps.getCredentials().indexOf("|")==-1)
			{
				client.getCredentialsProvider().setCredentials(new AuthScope("api.twitter.com",AuthScope.ANY_PORT), new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
			} else {
				OAuthConsumer consumer = new CommonsHttpOAuthConsumer(TwitterPollCoordinator.APP_TOKEN,TwitterPollCoordinator.APP_SECRET);
				consumer.setTokenWithSecret(ps.getCredentials().split("\\|")[0], ps.getCredentials().split("\\|")[1]);
				consumer.sign(post);
			}
			
			HttpResponse response = client.execute(post);
			if(response.getStatusLine().getStatusCode()!=200)
			{
				updateRateLimitInfo(response, ps);
				return "Failed to "+sa.getAction().toString()+" user #"+sa.getSubscriberId();
			}

			updateRateLimitInfo(response, ps);
			
			ps.registerSubscribeChange(sa);
			return "Successfully "+sa.getAction().toString()+"ed user #"+sa.getSubscriberId();
		}
		catch(Exception e)
		{
			log.error("Can't "+sa.getAction()+" a user :account="+getAccountPollManager().getAccount(),e);
			getAccountPollManager().subscribeChangeFailed();
			return null;
		}
		finally
		{
			SecurityAssociation.popRunAsIdentity();
		}
		
	}

}
