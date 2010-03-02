package com.digitalbarista.cat.util;

import java.util.ArrayList;
import java.util.List;

public class PagedList<E>
{
	private static final long serialVersionUID = 1L;
	
	private Integer totalResultCount;
	private List<E> results;
	
	public PagedList()
	{
		results = new ArrayList<E>();
	}
	
	public List<E> getResults() {
		return results;
	}

	public void setResults(List<E> results) {
		this.results = results;
	}

	public Integer getTotalResultCount() {
		return totalResultCount;
	}

	public void setTotalResultCount(Integer totalResultCount) {
		this.totalResultCount = totalResultCount;
	}	
}
