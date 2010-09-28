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
 * Entity implementation class for Entity: AddInMessageDO
 *
 */

@Entity
@Table(name="add_in_message")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="cat/AddInMessage")
public class AddInMessageDO implements Serializable,DataObject {

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="add_in_message_id")
	private Long addInMessageId;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="client_id")
	private ClientDO client;
	

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="campaign_id")
	private CampaignDO campaign;
	

	@Column(name="entry_type")
	@Enumerated(EnumType.STRING)
	private EntryPointType entryType;
	

	@Column(name="type")
	@Enumerated(EnumType.STRING)
	private AddInMessageType type;
	
	@Column(name="message")
	private String message;
	
	private static final long serialVersionUID = 1L;

	public AddInMessageDO() {
		super();
	}

	public Long getAddInMessageId() {
		return addInMessageId;
	}

	public void setAddInMessageId(Long addInMessageId) {
		this.addInMessageId = addInMessageId;
	}

	public ClientDO getClient() {
		return client;
	}

	public void setClient(ClientDO client) {
		this.client = client;
	}

	public CampaignDO getCampaign() {
		return campaign;
	}

	public void setCampaign(CampaignDO campaign) {
		this.campaign = campaign;
	}

	public EntryPointType getEntryType() {
		return entryType;
	}

	public void setEntryType(EntryPointType entryType) {
		this.entryType = entryType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AddInMessageType getType() {
		return type;
	}

	public void setType(AddInMessageType type) {
		this.type = type;
	}   
   
	
}
