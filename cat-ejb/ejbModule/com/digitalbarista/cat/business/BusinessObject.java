package com.digitalbarista.cat.business;

import com.digitalbarista.cat.data.DataObject;

public interface BusinessObject<T extends DataObject>
{
	public void copyFrom(T dataObject);
	public void copyTo(T dataObject);
}
