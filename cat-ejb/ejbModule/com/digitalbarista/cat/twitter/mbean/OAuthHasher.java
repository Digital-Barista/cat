package com.digitalbarista.cat.twitter.mbean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class OAuthHasher {

	public static String percentEncode(String base) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(base, "UTF-8")
        // OAuth encodes some characters differently:
        .replace("+", "%20").replace("*", "%2A")
        .replace("%7E", "~");
	}
	
	public static String hashMe(String method, String url, Map<String,String> params, final String consumerSecret, final String tokenSecret)
	{
		try
		{
			List<String> keys = new ArrayList<String>(params.keySet());
			Collections.sort(keys);
			
			StringBuffer sb = new StringBuffer();
			for(String key : keys)
			{
				if(sb.length()>0)
					sb.append("&");
				sb.append(key);
				sb.append("=");
				sb.append(params.get(key));
			}
			String encodedParams = percentEncode(sb.toString());
		
			String base = percentEncode(method) + "&" + percentEncode(url) + "&" +encodedParams;
			String keyString = ((consumerSecret!=null)?percentEncode(consumerSecret):"") + 
								"&"+
								((tokenSecret!=null)?percentEncode(tokenSecret):"");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(keyString.getBytes(),"HmacSHA1"));
			mac.update(base.getBytes());
			return Base64.encode(mac.doFinal());
		}
		catch(UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("Unable to hash parameters.");
	}
}
