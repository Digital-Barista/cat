package com.digitalbarista.cat.data;

import java.io.Serializable;
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

import org.hibernate.annotations.OrderBy;

/**
 * Entity implementation class for Entity: ClientDO
 *
 */

@Entity
@Table(name="client")
public class ClientDO implements Serializable,DataObject {

	private Long primaryKey;
	private String name;
	private String adminAddInMessage;
	private String userAddInMessage;
	private Set<EntryPointDO> entryPoints;
	private Set<KeywordLimitDO> keywordLimits;
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

	@Column(name="admin_add_in_message")
	public String getAdminAddInMessage() {
		return adminAddInMessage;
	}

	public void setAdminAddInMessage(String adminAddInMessage) {
		this.adminAddInMessage = adminAddInMessage;
	}

	@Column(name="user_add_in_message")
	public String getUserAddInMessage() {
		return userAddInMessage;
	}

	public void setUserAddInMessage(String userAddInMessage) {
		this.userAddInMessage = userAddInMessage;
	}

	@ManyToMany(targetEntity=EntryPointDO.class,fetch=FetchType.LAZY)
	@JoinTable(
		name="client_entry_point_link",
		joinColumns=@JoinColumn(name="client_id"),
		inverseJoinColumns=@JoinColumn(name="entry_point_id")
	)
	public Set<EntryPointDO> getEntryPoints() {
		return entryPoints;
	}

	public void setEntryPoints(Set<EntryPointDO> entryPoints) {
		this.entryPoints = entryPoints;
	}


	@OneToMany(fetch=FetchType.LAZY, targetEntity=KeywordLimitDO.class, mappedBy="client")
	@JoinColumn(name="client_id")
	public Set<KeywordLimitDO> getKeywordLimits() {
		return keywordLimits;
	}

	public void setKeywordLimits(Set<KeywordLimitDO> keywordLimits) {
		this.keywordLimits = keywordLimits;
	}
   
}
