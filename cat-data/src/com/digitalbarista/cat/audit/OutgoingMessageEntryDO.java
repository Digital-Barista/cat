package com.digitalbarista.cat.audit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.digitalbarista.cat.data.EntryPointType;


@Entity
@Table(name="audit_outgoing_message")
public class OutgoingMessageEntryDO implements Serializable {

	   
	private Long primaryKey;
	private String nodeUID;
	private Integer nodeVersion;
	private String destination;
	private Date dateSent;
	private EntryPointType messageType;
	private String subjectOrMessage;
	private byte[] payload;
	private static final long serialVersionUID = 1L;

	public OutgoingMessageEntryDO() {
		super();
	}   

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="outgoing_audit_id")
	public Long getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}   
	
	@Column(name="node_uid")
	public String getNodeUID() {
		return this.nodeUID;
	}

	public void setNodeUID(String nodeUID) {
		this.nodeUID = nodeUID;
	}   
	
	@Column(name="node_version")
	public Integer getNodeVersion() {
		return this.nodeVersion;
	}

	public void setNodeVersion(Integer nodeVersion) {
		this.nodeVersion = nodeVersion;
	}   
	
	@Column(name="destination")
	public String getDestination() {
		return this.destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}   
	
	@Column(name="date_sent")
	public Date getDateSent() {
		return this.dateSent;
	}

	public void setDateSent(Date dateSent) {
		this.dateSent = dateSent;
	}   
	
	@Column(name="msg_type")
	public EntryPointType getMessageType() {
		return this.messageType;
	}

	public void setMessageType(EntryPointType messageType) {
		this.messageType = messageType;
	}   
	
	@Column(name="subject_or_message")
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
   
}
