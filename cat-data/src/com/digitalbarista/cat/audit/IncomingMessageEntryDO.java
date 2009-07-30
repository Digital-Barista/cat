package com.digitalbarista.cat.audit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.digitalbarista.cat.data.EntryPointType;

@Table(name="audit_incoming_message")
@Entity

public class IncomingMessageEntryDO implements Serializable {

	
	private Long primaryKey;
	private String incomingAddress;
	private EntryPointType incomingType;
	private Date dateReceived;
	private String subjectOrMessage;
	private String returnAddress;
	private byte[] payload;
	private static final long serialVersionUID = 1L;

	public IncomingMessageEntryDO() {
		super();
	}
	
	@Id    
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="incoming_audit_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}   
	
	@Column(name="incoming_address")
	public String getIncomingAddress() {
		return this.incomingAddress;
	}

	public void setIncomingAddress(String incomingAddress) {
		this.incomingAddress = incomingAddress;
	}   
	
	@Column(name="incoming_type")
	@Enumerated(EnumType.STRING)
	public EntryPointType getIncomingType() {
		return this.incomingType;
	}

	public void setIncomingType(EntryPointType incomingType) {
		this.incomingType = incomingType;
	}
	
	@Column(name="date_received")
	public Date getDateReceived() {
		return this.dateReceived;
	}

	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}   
	
	@Column(name="msg_or_subject")
	public String getSubjectOrMessage() {
		return this.subjectOrMessage;
	}

	public void setSubjectOrMessage(String subjectOrMessage) {
		this.subjectOrMessage = subjectOrMessage;
	}
	
	@Lob
	@Column(name="payload")
	public byte[] getPayload() {
		return this.payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	@Column(name="return_address")
	public String getReturnAddress() {
		return returnAddress;
	}

	public void setReturnAddress(String returnAddress) {
		this.returnAddress = returnAddress;
	}
   
}
