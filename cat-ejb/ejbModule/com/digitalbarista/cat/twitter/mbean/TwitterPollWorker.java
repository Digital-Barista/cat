package com.digitalbarista.cat.twitter.mbean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class TwitterPollWorker<T> implements Callable<T> {

	private TwitterAccountPollManager pollManager;
	protected static final String cfName = "java:/JmsXA";
	protected static final String destName = "cat/messaging/Events";
	protected static final String twitterSendDestName = "cat/messaging/TwitterOutgoing";
	private InitialContext ic;
	private DataSource ds;
		
	protected Logger log = LogManager.getLogger(getClass());
	
	protected InitialContext getInitialContext() throws NamingException
	{
		if(ic==null)
			ic = new InitialContext();
		return ic;
	}
	
	protected DataSource getDataSource() throws NamingException
	{
		if(ds==null)
			ds = (DataSource)getInitialContext().lookup("java:/jdbc/campaignAdminDS");
		return ds;
	}
	
	protected String getCredentials(String account) throws OperationFailedException
	{
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		try
		{
			conn = ds.getConnection();
			stmt = conn.prepareStatement("select credentials from entry_points where entry_point=? and entry_type=?");
			stmt.setString(1, account);
			stmt.setString(2, "Twitter");
			rs = stmt.executeQuery();
			rs.next();
			return rs.getString(1);
		}
		catch(Exception e)
		{
			throw new OperationFailedException("Failed to get credentials from the database.",e);
		}
		finally
		{
			try{rs.close();}catch(Exception e){}
			try{stmt.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
	}
	
	protected TwitterAccountPollManager getAccountPollManager()
	{
		return pollManager;
	}
	
	protected void updateRateLimitInfo(HttpMethodBase method, TwitterAccountPollManager ps)
	{
		ps.setLastPollTime(new Date());
		Integer maxQueries = null;
		Integer remainingQueries = null;
		try
		{
			maxQueries = new Integer(method.getResponseHeader("X-experimental-RLS-maxvalue").getValue());
			if(!maxQueries.equals(ps.getMaxQueries()))
				ps.setMaxQueries(maxQueries);
		}catch(Exception e){}
		try
		{
			remainingQueries = new Integer(method.getResponseHeader("X-RateLimit-Remaining").getValue());
			if(!remainingQueries.equals(ps.getRemainingQueries()))
				ps.setRemainingQueries(remainingQueries);
		}catch(Exception e){}
		try
		{
			if(ps.getResetTime()==null)
				ps.setResetTime(new Date());
			ps.getResetTime().setTime(new Long(method.getResponseHeader("X-RateLimit-Reset").getValue())*1000);
		}catch(Exception e){}

	}
	
	protected TwitterPollWorker(TwitterAccountPollManager pm)
	{
		this.pollManager = pm;
	}
}
