package com.digitalbarista.cat.twitter.mbean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.digitalbarista.cat.twitter.bindings.IdListNoCursor;

public class FollowerCheckWorker extends TwitterPollWorker<Set<Long>> {

	public FollowerCheckWorker(TwitterAccountPollManager pollManager) {
		super(pollManager);
	}

	@Override
	public Set<Long> call() throws Exception {
		DefaultHttpClient client;
		HttpGet get = null;
		
		try
		{						
			JAXBContext context = JAXBContext.newInstance(IdListNoCursor.class);
			Unmarshaller decoder = context.createUnmarshaller();
			
			TwitterAccountPollManager ps = getAccountPollManager();
			
			client = new DefaultHttpClient();
			
			get = new HttpGet("http://api.twitter.com/1/followers/ids.xml");
			get.getParams().setParameter("screen_name", ps.getAccount());

			if(ps.getCredentials().indexOf("|")==-1)
			{
				client.getCredentialsProvider().setCredentials(new AuthScope("api.twitter.com",AuthScope.ANY_PORT), new UsernamePasswordCredentials(ps.getAccount(),ps.getCredentials()));
			} else {
				OAuthConsumer consumer = new CommonsHttpOAuthConsumer(TwitterPollCoordinator.APP_TOKEN,TwitterPollCoordinator.APP_SECRET);
				consumer.setTokenWithSecret(ps.getCredentials().split("\\|")[0], ps.getCredentials().split("\\|")[1]);
				consumer.sign(get);
			}
			
			HttpResponse response = client.execute(get);
			if(response.getStatusLine().getStatusCode()!=200)
			{
				updateRateLimitInfo(response, ps);
				throw new OperationFailedException("Could not check for followers.  response="+response.getStatusLine().getStatusCode());
			}
			
			updateRateLimitInfo(response, ps);
			
			IdListNoCursor idList = (IdListNoCursor)decoder.unmarshal(response.getEntity().getContent());
			Set<Long> ret = new HashSet<Long>();
			if(idList.getIds()!=null)
				ret.addAll(idList.getIds());
			ps.registerFollowerList(ret);			
			return ret;
		}
		catch(Throwable e)
		{
			log.error("Can't get friend list for account "+getAccountPollManager().getAccount(),e);
			getAccountPollManager().followerCheckFailed();
			return null;
		}
	}
}
