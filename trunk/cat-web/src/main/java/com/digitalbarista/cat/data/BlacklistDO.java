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

/**
 * Entity implementation class for Entity: BlacklistDO
 *
 */

@Entity
@Table(name="blacklist")
public class BlacklistDO implements Serializable,DataObject {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="blacklist_id")
	private Integer blacklistId;

	@Column(name="entry_type")
	@Enumerated(EnumType.STRING)
	private EntryPointType entryPointType;
	
	@Column(name="address")
	private String address;
	
	@Column(name="incoming_address")
	private String incomingAddress;
	
	@ManyToOne
	@JoinColumn(name="client_id")
	private ClientDO client;
	
	private static final long serialVersionUID = 1L;

	public BlacklistDO() {
		super();
	}

	public Integer getBlacklistId() {
		return blacklistId;
	}

	public void setBlacklistId(Integer blacklistId) {
		this.blacklistId = blacklistId;
	}

	public EntryPointType getEntryPointType() {
		return entryPointType;
	}

	public void setEntryPointType(EntryPointType entryPointType) {
		this.entryPointType = entryPointType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getIncomingAddress() {
		return incomingAddress;
	}

	public void setIncomingAddress(String incomingAddress) {
		this.incomingAddress = incomingAddress;
	}

	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}   
	
	
   
}
