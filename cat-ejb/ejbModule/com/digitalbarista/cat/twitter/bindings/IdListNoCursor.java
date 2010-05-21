package com.digitalbarista.cat.twitter.bindings;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ids")
public class IdListNoCursor {
	private List<Long> ids;
	
	@XmlElement(name="id")
	public List<Long> getIds() {
		return ids;
	}
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

}
