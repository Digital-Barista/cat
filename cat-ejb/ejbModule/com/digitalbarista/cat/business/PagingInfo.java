package com.digitalbarista.cat.business;

public class PagingInfo {

	private Integer pageIndex;
	private Integer pageSize;
	private String sortProperty;
	private Boolean sortDirectionAscending;
	
	
	public Integer getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getSortProperty() {
		return sortProperty;
	}
	public void setSortProperty(String sortProperty) {
		this.sortProperty = sortProperty;
	}
	public Boolean getSortDirectionAscending() {
		return sortDirectionAscending;
	}
	public void setSortDirectionAscending(Boolean sortDirectionAscending) {
		this.sortDirectionAscending = sortDirectionAscending;
	}
}
