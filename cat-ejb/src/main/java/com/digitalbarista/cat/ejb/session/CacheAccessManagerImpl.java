package com.digitalbarista.cat.ejb.session;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.Service;

@Service //Allows this to run as a "singleton" EJB.
@LocalBinding(jndiBinding = "ejb/cat/CacheAccessManager")
public class CacheAccessManagerImpl implements CacheAccessManager {

	private CacheManager manager = new CacheManager(getClass().getResource("/cat-ehcache.xml"));
	
	private Cache getCache(CacheName name)
	{
		return manager.getCache(name.toString());
	}
	
	@Override
	public boolean cacheContainsKey(CacheName name, String key) {
		return getCache(name).isKeyInCache(key);
	}

	@Override
	public Object getCachedObject(CacheName name, String key) {
		Element el = getCache(name).get(key);
		if(el==null)
			return null;
		return el.getObjectValue();
	}

	@Override
	public void cacheObject(CacheName name, String key, Object obj) {
		getCache(name).put(new Element(key,obj));
	}
	
}
