package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="keywords")
public class KeywordDO implements Serializable,DataObject {

	private Long primaryKey;
	private EntryPointDO entryPoint;
	private ClientDO client;
	private String keyword;
	private static final long serialVersionUID = 1L;

	public KeywordDO() {
		super();
	}
	
	@Id
	@Column(name="keyword_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@ManyToOne
	@JoinColumn(name="entry_point_id")
	public EntryPointDO getEntryPoint() {
		return this.entryPoint;
	}

	public void setEntryPoint(EntryPointDO entryPoint) {
		this.entryPoint = entryPoint;
	}   

	@ManyToOne
	@JoinColumn(name="client_id")
	public ClientDO getClient() {
		return this.client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}
	
	@Column(name="keyword")
	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
   
}
