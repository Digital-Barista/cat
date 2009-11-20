package com.digitalbarista.cat.twitter.mbean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class TwitterAccountRefresher extends TwitterPollWorker<Map<String,String>> {

	protected TwitterAccountRefresher(ApplicationContext ctx) {
		super(ctx,null);
	}

	@Override
	public Map<String, String> call() throws Exception {
		Connection conn=null;
		PreparedStatement stmt=null;;
		ResultSet rs=null;
		try
		{
			conn = getDataSource().getConnection();
			stmt = conn.prepareStatement("select entry_point,credentials from entry_points where entry_type=?");
			stmt.setString(1, "Twitter");
			rs = stmt.executeQuery();
			Map<String,String> ret = new HashMap<String,String>();
			while(rs.next())
				ret.put(rs.getString(1), rs.getString(2));
			return ret;
		}
		finally
		{
			try{rs.close();}catch(Exception e){}
			try{stmt.close();}catch(Exception e){}
			try{conn.close();}catch(Exception e){}
		}
	}

}
