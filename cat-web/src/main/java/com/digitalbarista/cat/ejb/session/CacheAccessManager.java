package com.digitalbarista.cat.ejb.session;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.stereotype.Component;

@Component("CacheAccessManager")
public class CacheAccessManager {

	public enum CacheName
	{
		ConnectorCache,
		NodeCache,
		CampaignCache,
		PermissionCache
	};
	
	private static CacheManager manager = new CacheManager(CacheAccessManager.class.getResource("/cat-ehcache.xml"));
	
	private Cache getCache(CacheName name)
	{
		return manager.getCache(name.toString());
	}
	
	public boolean cacheContainsKey(CacheName name, String key) {
		return getCache(name).isKeyInCache(key);
	}

	public Object getCachedObject(CacheName name, String key) {
		Element el = getCache(name).get(key);
		if(el==null)
			return null;
		return el.getObjectValue();
	}

	public void cacheObject(CacheName name, String key, Object obj) {
		getCache(name).put(new Element(key,obj));
	}
	
}
