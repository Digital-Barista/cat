package com.digitalbarista.cat.ejb.session;

import java.util.Set;

import javax.ejb.Local;

import com.digitalbarista.cat.data.EntryPointType;

@Local
public interface SubscriptionManager {
	public Set<String> getAllAddresses(Long clientId, EntryPointType type);
	public void subscribeToEntryPoint(Set<String> addresses, String entryPointUID);
}
