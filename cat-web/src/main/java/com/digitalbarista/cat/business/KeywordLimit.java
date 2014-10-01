package com.digitalbarista.cat.business;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.KeywordLimitDO;

@XmlRootElement
public class KeywordLimit implements
		BusinessObject<KeywordLimitDO>, Serializable {

	private Long keywordLimitId;
	private Long clientId;
	private Integer maxKeywords;
	private EntryPointType entryType;
	private static final long serialVersionUID = 1L;
	
	@Override
	public void copyFrom(KeywordLimitDO dataObject) {
		keywordLimitId = dataObject.getKeywordLimitId();
		
		clientId = dataObject.getClient().getPrimaryKey();
		maxKeywords = dataObject.getMaxKeywords();
		entryType = dataObject.getEntryType();
	}

	@Override
	public void copyTo(KeywordLimitDO dataObject) {
		dataObject.setMaxKeywords(maxKeywords);
		dataObject.setEntryType(entryType);
	}


	@XmlAttribute
	public Long getKeywordLimitId() {
		return keywordLimitId;
	}

	public void setKeywordLimitId(Long keywordLimitId) {
		this.keywordLimitId = keywordLimitId;
	}
	
	@XmlAttribute
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	@XmlAttribute
	public Integer getMaxKeywords() {
		return maxKeywords;
	}

	public void setMaxKeywords(Integer maxKeywords) {
		this.maxKeywords = maxKeywords;
	}

	@XmlAttribute
	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	
}
