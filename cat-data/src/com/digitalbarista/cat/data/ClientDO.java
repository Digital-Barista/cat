package com.digitalbarista.cat.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: ClientDO
 *
 */

@Entity
@Table(name="client")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/Client")
public class ClientDO implements Serializable,DataObject {

	private Long primaryKey;
	private String name;
	private String contactName;
	private String contactEmail;
	private String contactPhone;
	private boolean active=true;
	private Set<EntryPointDO> entryPoints;
	private Set<KeywordLimitDO> keywordLimits;
	private Set<AddInMessageDO> addInMessages = new HashSet<AddInMessageDO>();
	private Set<ClientInfoDO> clientInfos = new HashSet<ClientInfoDO>();
	
	private static final long serialVersionUID = 1L;

	public ClientDO() {
		super();
	}   
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="client_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name="name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@OneToMany(fetch=FetchType.LAZY, targetEntity=AddInMessageDO.class, mappedBy="client")
	@JoinColumn(name="client_id")
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/client/addInMessages")
	public Set<AddInMessageDO> getAddInMessages() {
		return addInMessages;
	}

	public void setAddInMessages(Set<AddInMessageDO> addInMessages) {
		this.addInMessages = addInMessages;
	}

	@OneToMany(fetch=FetchType.LAZY, targetEntity=ClientInfoDO.class, mappedBy="client")
	@JoinColumn(name="client_id")
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/client/clientInfos")
	public Set<ClientInfoDO> getClientInfos() {
		return clientInfos;
	}

	public void setClientInfos(Set<ClientInfoDO> clientInfos) {
		this.clientInfos = clientInfos;
	}

	@ManyToMany(targetEntity=EntryPointDO.class,fetch=FetchType.LAZY)
	@JoinTable(
		name="client_entry_point_link",
		joinColumns=@JoinColumn(name="client_id"),
		inverseJoinColumns=@JoinColumn(name="entry_point_id")
	)
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/client/entryPoints")
	public Set<EntryPointDO> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(Set<EntryPointDO> entryPoints) {
		this.entryPoints = entryPoints;
	}


	@OneToMany(fetch=FetchType.LAZY, targetEntity=KeywordLimitDO.class, mappedBy="client")
	@JoinColumn(name="client_id")
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/client/keywordLimits")
	public Set<KeywordLimitDO> getKeywordLimits() {
		return keywordLimits;
	}

	public void setKeywordLimits(Set<KeywordLimitDO> keywordLimits) {
		this.keywordLimits = keywordLimits;
	}

	@Column(name="contact_name")
	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	@Column(name="contact_email")
	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	@Column(name="contact_phone")
	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	@Column(name="active")
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
   
}
