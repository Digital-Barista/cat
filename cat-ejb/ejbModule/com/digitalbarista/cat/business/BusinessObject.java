package com.digitalbarista.cat.business;

import java.io.Serializable;

import com.digitalbarista.cat.data.DataObject;

public interface BusinessObject<T extends DataObject> extends Serializable
{
	public void copyFrom(T dataObject);
	public void copyTo(T dataObject);
}
