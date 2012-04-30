package com.digitalbarista.cat.ejb.session;

import javax.ejb.Local;

@Local
public interface CacheAccessManager {

	public enum CacheName
	{
		ConnectorCache,
		NodeCache,
		CampaignCache,
		PermissionCache
	}
	
	public boolean cacheContainsKey(CacheName name,String key);
	public Object getCachedObject(CacheName name,String key);
	public void cacheObject(CacheName name, String key, Object conn);
	
}
