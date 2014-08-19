package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name="keyword_limit")
public class KeywordLimitDO implements Serializable,DataObject {

	private Long keywordLimitId;
	private ClientDO client;
	private Integer maxKeywords;
	private EntryPointType entryType;
	private static final long serialVersionUID = 1L;

	public KeywordLimitDO() {
		super();
	}
	
	@Id
	@Column(name="keyword_limit_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getKeywordLimitId() {
		return this.keywordLimitId;
	}

	public void setKeywordLimitId(Long keywordLimitId) {
		this.keywordLimitId = keywordLimitId;
	}
	
	@ManyToOne
	@JoinColumn(name="client_id")
	public ClientDO getClient() {
		return this.client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}

	@Column(name="max_keywords")
	public Integer getMaxKeywords() {
		return maxKeywords;
	}

	public void setMaxKeywords(Integer maxKeywords) {
		this.maxKeywords = maxKeywords;
	}

	@Column(name="entry_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}
	
   
}
