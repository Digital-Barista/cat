package com.digitalbarista.cat.twitter.bindings;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="id_list")
public class IdList {
	private BigInteger nextCursor;
	private BigInteger previousCursor;
	private List<Long> ids;
	
	@XmlElementWrapper(name="ids")
	@XmlElement(name="id")
	public List<Long> getIds() {
		return ids;
	}
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	@XmlElement(name="next_cursor")
	public BigInteger getNextCursor() {
		return nextCursor;
	}
	public void setNextCursor(BigInteger nextCursor) {
		this.nextCursor = nextCursor;
	}

	@XmlElement(name="previous_cursor")
	public BigInteger getPreviousCursor() {
		return previousCursor;
	}
	public void setPreviousCursor(BigInteger previousCursor) {
		this.previousCursor = previousCursor;
	}
}
