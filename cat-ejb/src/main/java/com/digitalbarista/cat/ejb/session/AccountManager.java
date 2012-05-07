package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.FacebookApp;

@Local
public interface AccountManager 
{
	public List<FacebookApp> getFacebookApps();
	public FacebookApp save(FacebookApp app);
	public void delete(FacebookApp app);
}
