package com.digitalbarista.cat.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Entity implementation class for Entity: ClientInfoDO
 *
 */

@Entity
@Table(name="client_info")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/ClientInfo")
public class ClientInfoDO implements Serializable,DataObject {

	public final static String KEY_MESSAGE_CREDITS = "messageCredits";
	public final static String KEY_CREDIT_PAYMENT_URL = "creditPaymentURL";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="client_info_id")
	private Long clientInfoId;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="client_id")
	private ClientDO client;
	
	@Column(name="entry_type")
	@Enumerated(EnumType.STRING)
	private EntryPointType entryType;
	
	@Column(name="name")
	private String name;

	@Column(name="value")
	private String value;
	
	private static final long serialVersionUID = 1L;

	public ClientInfoDO() {
		super();
	}

	public Long getClientInfoId() {
		return clientInfoId;
	}

	public void setClientInfoId(Long clientInfoId) {
		this.clientInfoId = clientInfoId;
	}

	
	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}

	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
}
