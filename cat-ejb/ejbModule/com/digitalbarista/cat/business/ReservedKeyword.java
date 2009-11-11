package com.digitalbarista.cat.business;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.ReservedKeywordDO;

@XmlRootElement
public class ReservedKeyword implements
		BusinessObject<ReservedKeywordDO>, Serializable {

	private Long reservedKeywordId;
	private String keyword;
	private static final long serialVersionUID = 1L;
	
	@Override
	public void copyFrom(ReservedKeywordDO dataObject) {
		reservedKeywordId = dataObject.getReservedKeywordId();
		keyword = dataObject.getKeyword();
	}

	@Override
	public void copyTo(ReservedKeywordDO dataObject) {
		dataObject.setKeyword(keyword);
	}

	public Long getReservedKeywordId() {
		return reservedKeywordId;
	}

	public void setReservedKeywordId(Long reservedKeywordId) {
		this.reservedKeywordId = reservedKeywordId;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	

}
