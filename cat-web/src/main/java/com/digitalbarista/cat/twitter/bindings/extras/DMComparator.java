package com.digitalbarista.cat.twitter.bindings.extras;

import java.util.Comparator;

import com.digitalbarista.cat.twitter.bindings.DirectMessage;

public class DMComparator implements Comparator<DirectMessage> {

	@Override
	public int compare(DirectMessage dm1, DirectMessage dm2) {
		if(dm1==null && dm2==null)
			return 0;
		if(dm1==null)
			return 1;
		if(dm2==null)
			return -1;
		if(dm1.getId()==dm2.getId())
			return 0;
		return dm1.getId()<dm2.getId()?-1:1;
	}

}
