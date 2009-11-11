package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="reserved_keyword")
public class ReservedKeywordDO implements Serializable,DataObject {

	private Long reservedKeywordId;
	private String keyword;
	private static final long serialVersionUID = 1L;

	public ReservedKeywordDO() {
		super();
	}

	
	@Id
	@Column(name="reserved_keyword_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getReservedKeywordId() {
		return reservedKeywordId;
	}

	public void setReservedKeywordId(Long reservedKeywordId) {
		this.reservedKeywordId = reservedKeywordId;
	}

	@Column(name="keyword")
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
   
}
