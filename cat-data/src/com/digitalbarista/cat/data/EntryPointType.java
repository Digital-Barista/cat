package com.digitalbarista.cat.data;

public enum EntryPointType {
	SMS,
	Email,
	Twitter;
	
	public static EntryPointType getById(Integer id)
	{
		return EntryPointType.values()[id];
	}
}
